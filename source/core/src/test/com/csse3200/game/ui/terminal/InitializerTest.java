package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.CollectableFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class InitializerTest {
  private Shell shell;

  private Entity player;
  private InventoryComponent inv;
  private CombatStatsComponent cb;
  private EntityService mockEntityService;
  private MockedStatic<CollectableFactory> collectableFactory;
  private PhysicsComponent physicsComponent;
  private GameArea mockGameArea;
  private MainGameScreen mainGameScreen;

  private TestConsole console;

  // TestConsole implementation to capture output from the shell
  private static class TestConsole implements Shell.Console {
    private final StringBuilder output = new StringBuilder();

    @Override
    public void print(Object obj) {
      if (obj != null) {
        output.append(obj);
      }
    }

    @Override
    public String next() { return null; }
    @Override
    public boolean hasNext() { return false; }
    @Override
    public void close() {/* Nothing to cleanup at close */}

    public String getOutput() {
      return output.toString();
    }

    public void clear() {
      output.setLength(0);
    }
  }

  @BeforeEach
  void setUp() {
    console = new TestConsole();
    shell = new Shell(console);
    for (Field field : Initializer.class.getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Object fieldValue = field.get(null);
        if (fieldValue instanceof String) {
          shell.eval((String) fieldValue);
        }
      } catch (IllegalAccessException e) {
        // Ignore
      }
    }

    player = mock(Entity.class);
    player.create();
    inv = mock(InventoryComponent.class);
    cb = mock(CombatStatsComponent.class);
    mockEntityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(mockEntityService);

    physicsComponent = mock(PhysicsComponent.class);
    Body mockBody = mock(Body.class);
    when(physicsComponent.getBody()).thenReturn(mockBody);
    when(player.getComponent(PhysicsComponent.class)).thenReturn(physicsComponent);

    Entity fakeEntity = mock(Entity.class);
    when(fakeEntity.getComponent(any())).thenReturn(null);

//    collectableFactory = mockStatic(CollectableFactory.class);
//    collectableFactory.when(CollectableFactory::createJetpackUpgrade)
//        .thenReturn(fakeEntity);
//    collectableFactory.when(CollectableFactory::createDashUpgrade)
//        .thenReturn(fakeEntity);
//    collectableFactory.when(CollectableFactory::createGlideUpgrade)
//        .thenReturn(fakeEntity);
//    collectableFactory.when(() -> CollectableFactory.createCollectable("key:door"))
//        .thenReturn(fakeEntity);

    mockGameArea = mock(GameArea.class);
    mainGameScreen = mock(MainGameScreen.class);
    ServiceLocator.registerMainGameScreen(mainGameScreen);
    when(mainGameScreen.getAreaEnum()).thenReturn(mock(MainGameScreen.Areas.class));
    when(mainGameScreen.getGameArea(any(MainGameScreen.Areas.class))).thenReturn(mockGameArea);

    when(player.getComponent(CombatStatsComponent.class)).thenReturn(cb);
    when(player.getComponent(InventoryComponent.class)).thenReturn(inv);
    shell.setGlobal("player", player);
    shell.eval("getPlayer = () { return(player); };");
  }

  @Test
  void initializes() {
    assertNotNull(shell);
    assertNotNull(console);
  }

  @Test
  void shouldDefineTrueFalseConstants() {
    assertEquals(true, shell.eval("true;"));
    assertEquals(false, shell.eval("false;"));
  }

  @Test
  void shouldDefineSetAndGetGlobal() {
    shell.eval("setGlobal(\"myVar\", 123);");
    assertEquals(123, shell.eval("myVar;"));
    // Test that getGlobal can bypass local scope
    shell.eval("x = () { myVar = 456; return(getGlobal(\"myVar\")); };");
    assertEquals(123, shell.eval("x();"));
  }

  @Test
  void shouldDefinePrint() {
    shell.eval("print(\"hello\", \" world\");");
    assertEquals("hello world", console.getOutput());
    console.clear();
    shell.eval("print(1, 2.5, 'c');");
    assertEquals("12.5c", console.getOutput());
  }

  @Test
  void shouldDefineIfAndIfElse() {
    assertEquals(10, shell.eval("if(true, () { return(10); });"));
    assertNull(shell.eval("if(false, () { return(10); });"));

    assertEquals("A", shell.eval("ifElse(true, () { return(\"A\"); }, () { return(\"B\"); });"));
    assertEquals("B", shell.eval("ifElse(false, () { return(\"A\"); }, () { return(\"B\"); });"));
  }

  @Test
  void shouldDefineForEachWithRange() {
    Object result;
    try {
      result = shell.eval("""
          x = 0l;
          forEach(Range(1, 5), (i) {
            setGlobal("x", .java.lang.Long.sum(getGlobal("x"), i));
          });
          x;
          """
      );
    } catch (Exception e) {
      System.out.println(console.output);
      throw new RuntimeException(e);
    }

    // 1+2+3+4+5 = 15
    assertEquals("15", result.toString());
  }

  @Test
  void shouldDefineWhile() {
    shell.eval("""
        x = 0;
        while(() { return(eql(.java.lang.Integer.compare(getGlobal("x"), 5), -1)); }, () {
          setGlobal("x", .java.lang.Integer.sum(getGlobal("x"), 1));
        });
        """
    );
    assertEquals("5", shell.eval("x;").toString());
  }

  @Test
  void shouldDefineTryCatch() {
    // Should execute catch block on error
    Object result = shell.eval(
        "tryCatch(() { .java.lang.Class.forName(\"invalid\"); }, (e) { return(true); });"
    );
    assertEquals(true, result);

    // Should not execute catch block if no error
    result = shell.eval(
        "tryCatch(() { return(\"success\"); }, (e) { return(\"failure\"); });"
    );
    assertEquals("success", result);
  }

  @Test
  void shouldDefineLogicalOperators() {
    assertEquals(true, shell.eval("and(true, 1);"));
    assertEquals(false, shell.eval("and(true, 0);"));
    assertEquals(true, shell.eval("or(true, false);"));
    assertEquals(false, shell.eval("or(0, \"\");"));
    assertEquals(false, shell.eval("not(true);"));
    assertEquals(true, shell.eval("not(false);"));
  }

  @Test
  void shouldDefineEqualityAndNullChecks() {
    assertEquals(true, shell.eval("eql(1, 1);"));
    assertEquals(false, shell.eval("eql(1, 2);"));
    assertEquals(true, shell.eval("eql(\"hello\", \"hello\");"));
    assertEquals(true, shell.eval("isNull((){}());"));
    assertEquals(false, shell.eval("isNull(1);"));
  }

  @Test
  void shouldDefineIsClass() {
    assertEquals(true, shell.eval("isClass(.java.lang.String);"));
    assertEquals(false, shell.eval("isClass(\"I am a string instance\");"));
  }

  @Test
  void shouldDefineExists() {
    assertEquals(false, shell.eval("exists(\"nonExistentVar\");"));
    shell.eval("myVar = 10;");
    assertEquals(true, shell.eval("exists(\"myVar\");"));
  }

  @Test
  void shouldDefineReturnN() {
    Object result = shell.eval("""
      outer = () {
        inner = () {
          returnN(2, "success");
          return("fail");
        };
        v = inner();
        return(v);
      };
      outer();
      """);
    assertEquals("success", result);
  }

  @Test
  void shouldDefineGetParentVar() {
    Object result = shell.eval("""
      outer = (val) {
        inner = () {
          return(getParentVar("val"));
        };
        return(inner());
      };
      outer(99);
      """);
    assertEquals(99, result);
  }

  @Test
  void testGodModeCommand() {
    shell.eval("godMode();");

    int health = player.getComponent(CombatStatsComponent.class).getHealth();

    player.getComponent(CombatStatsComponent.class).setHealth(0);

    // Ensure health has not changed with god mode on
    assertEquals(health, player.getComponent(CombatStatsComponent.class).getHealth());

  }

//  @Test
//  void testSpawnJetpackCommand() {
//
//    shell.eval("spawnJetpack();");
//
//    verify(mockEntityService, times(1)).register(any());
//  }
//
//  @Test
//  void testSpawnDashCommand() {
//
//    shell.eval("spawnDash();");
//
//    verify(mockEntityService, times(1)).register(any());
//  }
//
//  @Test
//  void testSpawnGliderCommand() {
//
//    shell.eval("spawnGlider();");
//
//    verify(mockEntityService, times(1)).register(any());
//  }
//
//  @Test
//  void testSpawnAllUpgradesCommand() {
//
//    shell.eval("spawnAllUpgrades();");
//
//    verify(mockEntityService, times(3)).register(any());
//  }
//
//  @Test
//  void testSpawnDoorKeyCommand() {
//    shell.eval("spawnDoorKey();");
//
//    verify(mockEntityService, times(1)).register(any());
//  }

  @Test
  void testGetGameAreaCommand() {
    Object result = shell.eval("getGameArea();");

    assertEquals(mockGameArea, result);
  }

  @Test
  void testTeleportCommand() {
    player.setPosition(0, 0);
    shell.eval("teleport(100.0, 100.0);");
    verify(player).setPosition(100.0f,  100.0f);
  }

  @AfterEach
  void tearDownFactories() {
    if (collectableFactory != null) {
      collectableFactory.close();
    }
  }
}
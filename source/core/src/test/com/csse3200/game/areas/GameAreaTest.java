package com.csse3200.game.areas;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * Things like textures loading properly are incredibly difficult to test within JUnit test.
 * Only tests values and mocks.
 */
@ExtendWith(GameExtension.class)
class GameAreaTest {
  private GameArea gameArea;
  private EntityService entityService;

  @BeforeEach
  void setup() {
    entityService = spy(new EntityService());
    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerRenderService(new RenderService());

    gameArea = spy(new GameArea() {
      @Override protected void loadPrerequisites() {}
      @Override protected void loadEntities() {}
      @Override protected Entity spawnPlayer() { return new Entity(); }
      @Override protected Entity spawnPlayer(List<Component> componentList) { return new Entity(); }
      @Override protected void loadAssets() {}
    });
  }

  @Test
  void shouldSpawnEntities() {
    TerrainFactory factory = mock(TerrainFactory.class);

    GameArea gameArea =
        new GameArea() {
          protected void loadPrerequisites() {}
          protected void loadEntities() {}
          protected Entity spawnPlayer() {return null;}
          protected Entity spawnPlayer(List<Component> componentList) {return null;}
          protected void loadAssets() {}
        };

    ServiceLocator.registerEntityService(new EntityService());
    Entity entity = mock(Entity.class);

    gameArea.spawnEntity(entity);
    verify(entity).create();

    gameArea.dispose();
    verify(entity).dispose();
  }

  @Test
  void resetComponentsTest() {
      TerrainFactory factory = mock(TerrainFactory.class);

      int startHealth = 100;
      int newHealth = 80; // != startHealth
      int baseAttack = 1;
      String testItem = "test";
      int itemCount = 4;

      GameArea gameArea =
              new GameArea() {
                  protected void loadPrerequisites() {}
                  protected void loadEntities() {}
                  protected Entity spawnPlayer() {
                      return new Entity()
                              .addComponent(new CombatStatsComponent(startHealth, baseAttack))
                              .addComponent(new InventoryComponent())
                              ;}
                  protected Entity spawnPlayer(List<Component> componentList) {
                      return new Entity()
                              .addComponent(new CombatStatsComponent(startHealth, baseAttack))
                              .addComponent(new InventoryComponent())
                              ;}
                  protected void loadAssets() {}
              };

      ServiceLocator.registerEntityService(new EntityService());

      // Save components
      gameArea.player = gameArea.spawnPlayer();
      gameArea.saveComponents(gameArea.player.getComponent(CombatStatsComponent.class),
              gameArea.player.getComponent(InventoryComponent.class));

      // Comment out the components that are no longer saved/loaded

//      assertEquals(startHealth, gameArea.combatStats);
      assertEquals(0, gameArea.player.getComponent(InventoryComponent.class).getGrandTotalCount());

      // Reduce player health
//      gameArea.getPlayer().getComponent(CombatStatsComponent.class).setHealth(newHealth);
//      assertEquals(newHealth,
//              gameArea.getPlayer().getComponent(CombatStatsComponent.class).getHealth());

      // Add an item to player inventory
      gameArea.getPlayer().getComponent(InventoryComponent.class)
              .addDirect(InventoryComponent.Bag.UPGRADES, testItem, itemCount);
      assertEquals(itemCount,
              gameArea.player.getComponent(InventoryComponent.class).getGrandTotalCount());

      // Reload gameArea, should load saved components
      gameArea.reset();

      // Test that values have gone back to default
      List<Component> components = gameArea.getComponents();
      for (Component component : components) {
          // CombatStats
          if (component instanceof CombatStatsComponent) {
              CombatStatsComponent statsComponent = (CombatStatsComponent) component;
              assertEquals(startHealth, statsComponent.getHealth());
          }

          // Inventory
          if (component instanceof InventoryComponent) {
              InventoryComponent inventoryComponent = (InventoryComponent) component;
              assertEquals(0, inventoryComponent.getGrandTotalCount());
          }
      }
  }

  @Test
  void shouldRecordDeathLocation() {
    assertEquals(0, gameArea.getDeathLocations().size());
    gameArea.recordDeathLocation(new Vector2(1, 1));
    assertEquals(1, gameArea.getDeathLocations().size());
  }

  @Test
  void shouldSpawnDeathMarkers() {
    gameArea.recordDeathLocation(new Vector2(1, 1));
    gameArea.spawnDeathMarkers();
    verify(gameArea, times(1)).spawnEntity(any(Entity.class));
  }

  @Test
  void shouldDisposeMarkerTexture() throws Exception {
    gameArea.spawnDeathMarkers(); // Create the texture
    Field textureField = GameArea.class.getDeclaredField("deathMarkerTexture");
    textureField.setAccessible(true);
    assertNotEquals(null, textureField.get(gameArea));
    gameArea.dispose();
    assertEquals(null, textureField.get(gameArea));
  }
}

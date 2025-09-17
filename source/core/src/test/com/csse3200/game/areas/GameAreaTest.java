package com.csse3200.game.areas;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

/**
 * Things like textures loading properly are incredibly difficult to test within JUnit test.
 * Only tests values and mocks.
 */
@ExtendWith(GameExtension.class)
class GameAreaTest {
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
              .addItems(InventoryComponent.Bag.UPGRADES, testItem, itemCount);
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
}

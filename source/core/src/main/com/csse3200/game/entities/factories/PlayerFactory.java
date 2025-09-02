package com.csse3200.game.entities.factories;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.StaminaComponent;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.PlayerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory to create a player entity.
 *
 * <p>Predefined player properties are loaded from a config stored as a json file and should have
 * the properties stored in 'PlayerConfig'.
 */
public class PlayerFactory {
  private static final PlayerConfig stats = loadPlayerConfig();

  private static PlayerConfig loadPlayerConfig() {
    PlayerConfig config = FileLoader.readClass(PlayerConfig.class, "configs/player.json");
    if (config == null) {
      throw new IllegalStateException("Failed to load player config from configs/player.json");
    }
    return config;
  }

  /**
   * Create a player entity.
   * @return entity
   */
  public static Entity createPlayer() {
    InputComponent inputComponent =
            ServiceLocator.getInputService().getInputFactory().createForPlayer();

    Entity player =
            new Entity()
                    .addComponent(new TextureRenderComponent("images/box_boy_leaf.png"))
                    .addComponent(new PhysicsComponent())
                    .addComponent(new ColliderComponent())
                    .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                    .addComponent(new PlayerActions())
                    .addComponent(new CombatStatsComponent(stats.health, stats.baseAttack))
                    .addComponent(new InventoryComponent())
                    .addComponent(inputComponent)
                    .addComponent(new PlayerStatsDisplay());

    player.addComponent(new StaminaComponent(100f, 10f, 25f, 20));

    Actor minimapActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("minimap");
    if (minimapActor != null && minimapActor.getUserObject() != null
            && (minimapActor.getUserObject() instanceof MinimapDisplay minimapDisplay)) {
      player.addComponent(new MinimapComponent(minimapDisplay, "images/minimap_player_marker.png"));
    }

    PhysicsUtils.setScaledCollider(player, 0.6f, 0.3f);
    player.getComponent(ColliderComponent.class).setDensity(1.5f);
    player.getComponent(TextureRenderComponent.class).scaleEntity();
    return player;
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}

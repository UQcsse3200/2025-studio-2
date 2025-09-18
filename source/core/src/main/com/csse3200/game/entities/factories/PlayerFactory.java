package com.csse3200.game.entities.factories;
import com.badlogic.gdx.Gdx;


import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.*;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.components.npc.DroneAnimationController;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.PlayerAnimationController;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.PlayerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.*;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.List;

/**
 * Factory to create a player entity.
 *
 * <p>Predefined player properties are loaded from a config stored as a json file and should have
 * the properties stored in 'PlayerConfig'.
 */
public class PlayerFactory {
  private static final PlayerConfig stats = loadPlayerConfig();

  private static final Vector2 HITBOX_OFFSET = new Vector2(0.425f, 0.0f);
  private static final Vector2 HITBOX_SCALE = new Vector2(0.8f, 1.0f);

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
  public static Entity createPlayer(List<Component> componentList) {
    InputComponent inputComponent =
            ServiceLocator.getInputService().getInputFactory().createForPlayer();

    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService().getAsset("images/PLAYER.atlas", TextureAtlas.class));
    animator.addAnimation("CROUCH", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("CROUCHMOVE", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("CROUCHLEFT", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("CROUCHMOVELEFT", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("JUMP", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("JUMPLEFT", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("LEFT", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("RIGHT", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("IDLE", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("IDLELEFT", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("DASH", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("DASHLEFT", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("HURT", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("HURTLEFT", 0.2f, Animation.PlayMode.LOOP);


    Entity player =
            new Entity()
                    .addComponent(new PhysicsComponent())
                    .addComponent(new StandingColliderComponent())
                    .addComponent(new CrouchingColliderComponent())
                    .addComponent(new FootColliderComponent())
                    .addComponent(new ColliderComponent()) // Interactions
                    .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                    .addComponent(new PlayerActions())
                    .addComponent(new CombatStatsComponent(stats.health, stats.baseAttack))
                    .addComponent(new InventoryComponent())
                    .addComponent(inputComponent)
                    .addComponent(new PlayerStatsDisplay())
                    .addComponent(new CameraComponent())
                    .addComponent(new MinimapComponent("images/minimap_player_marker.png"));


    player
            .addComponent(animator)
            .addComponent(new PlayerAnimationController());

    for (Component component : componentList) {
      player.replaceComponent(component);
    }

    // --- Stamina: add component, wire sprint, and TEMP logging ---
    StaminaComponent stamina = new StaminaComponent(100f, 10f, 25f, 20);
    player.addComponent(stamina);

// Wire sprint toggle (expects your input component to emit sprintStart/sprintStop)
    player.getEvents().addListener("sprintStart", () -> {
      if (!stamina.isExhausted() && stamina.getCurrentStamina() > 0) {
        stamina.setSprinting(true);
      }
    });
    player.getEvents().addListener("sprintStop", () -> stamina.setSprinting(false));

// TEMP: Console logs to verify behaviour (remove before merging)
    player.getEvents().addListener("staminaUpdate", (Integer cur, Integer max) -> {
      Gdx.app.log("STAM", cur + "/" + max + (stamina.isExhausted() ? " (EXHAUSTED)" : ""));
    });
    player.getEvents().addListener("exhausted", () -> Gdx.app.log("STAM", "exhausted"));
    player.getEvents().addListener("recovered", () -> Gdx.app.log("STAM", "recovered"));
// --- end stamina block ---

    player.getComponent(ColliderComponent.class).setAsBox(HITBOX_SCALE,
            player.getCenterPosition().add(HITBOX_OFFSET));
    player.getComponent(ColliderComponent.class).setSensor(true);

    player.getComponent(HitboxComponent.class).setAsBox(HITBOX_SCALE,
            player.getCenterPosition().add(HITBOX_OFFSET));

    player.getComponent(StandingColliderComponent.class).setDensity(1.5f);
    player.getComponent(CrouchingColliderComponent.class).setDensity(1.5f);
    float scaleFactor = 2f;
    player.setScale(1f * scaleFactor, (48f/64f) * scaleFactor);
    AnimationRenderComponent arc =
            player.getComponent(AnimationRenderComponent.class);
    arc.startAnimation("IDLE");

    return player;
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}

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
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.*;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.player.PlayerEffectComponent;

import java.util.List;

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

  private static final float FOOT_HITBOX_WIDTH = 0.25f;
  private static final float FOOT_HITBOX_HEIGHT = 0.0001f;
  private static Vector2 FOOT_HITBOX_OFFSET = new Vector2(0, 0f);
  private static final float FOOT_HITBOX_ANGLE = 0;
  /**
   * Create a player entity.
   * @return entity
   */
    public static Entity createPlayer(List<Component> componentList) {
    InputComponent inputComponent =
            ServiceLocator.getInputService().getInputFactory().createForPlayer();

    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService().getAsset("images" +
                            "/PLAYER.atlas", TextureAtlas.class));
    animator.addAnimation("CROUCHING", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("JUMP", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("LEFT", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("RIGHT", 0.1f, Animation.PlayMode.LOOP);

    Entity player =
            new Entity()
                    .addComponent(new TextureRenderComponent("images/box_boy_leaf.png"))
                    .addComponent(new PhysicsComponent())
                    .addComponent(new StandingColliderComponent())
                    .addComponent(new CrouchingColliderComponent())
                    .addComponent(new ColliderComponent()) // temporary fix
                    .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                    .addComponent(new PlayerActions())
                    .addComponent(new CombatStatsComponent(stats.health, stats.baseAttack))
                    .addComponent(new InventoryComponent())
                    .addComponent(inputComponent)
                    .addComponent(new PlayerStatsDisplay())
                    .addComponent(new PlayerEffectComponent())
                    .addComponent(new MinimapComponent("images/minimap_player_marker.png"));


    player
            .addComponent(animator)
            .addComponent(new PlayerAnimationController());

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





    PhysicsUtils.setScaledCollider(player, 0.6f, 0.3f);

    player.getComponent(StandingColliderComponent.class).setDensity(1.5f);
    player.getComponent(CrouchingColliderComponent.class).setDensity(1.5f);
    player.getComponent(TextureRenderComponent.class).scaleEntity();

    // Fixture for Player feet, used to reset jump or handle landing logic
    Body body = player.getComponent(PhysicsComponent.class).getBody();

    PolygonShape footHitbox = new PolygonShape();
    footHitbox.setAsBox(FOOT_HITBOX_WIDTH, FOOT_HITBOX_HEIGHT, FOOT_HITBOX_OFFSET, FOOT_HITBOX_ANGLE);

    FixtureDef footFixtureDef = new FixtureDef();
    footFixtureDef.shape = footHitbox;
    footFixtureDef.isSensor = true;

    Fixture footFixture = body.createFixture(footFixtureDef);
    footFixture.setUserData("foot");

    footHitbox.dispose();

    // replace components with existing counterparts
    for (Component component : componentList) {
      player.replaceComponent(component);
    }

    return player;
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}

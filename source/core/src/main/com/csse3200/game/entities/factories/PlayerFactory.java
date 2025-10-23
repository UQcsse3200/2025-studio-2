package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.achievements.AchievementService;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.StaminaComponent;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.components.player.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.PlayerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.*;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.achievements.AchievementToastUI;

import java.util.List;

/** Factory to create a player entity. */
public final class PlayerFactory {
    private static final PlayerConfig stats = loadPlayerConfig();

    private static final Vector2 HITBOX_OFFSET = new Vector2(0.425f, 0.0f);
    private static final Vector2 HITBOX_SCALE  = new Vector2(0.8f, 1.0f);

    /** Load player stats from JSON once. */
    private static PlayerConfig loadPlayerConfig() {
        PlayerConfig config = FileLoader.readClass(PlayerConfig.class, "configs/player.json");
        if (config == null) {
            throw new IllegalStateException("Failed to load player config from configs/player.json");
        }
        return config;
    }

    /** Create a player entity. */
    public static Entity createPlayer(List<Component> componentList) {
        InputComponent input =
                ServiceLocator.getInputService().getInputFactory().createForPlayer();

        AnimationRenderComponent animator = new AnimationRenderComponent(
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
        animator.addAnimation("DEATH", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("SMOKE", 0.2f, Animation.PlayMode.LOOP);

        PlayerActions playerActions = new PlayerActions();

        Entity player = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new StandingColliderComponent())
                .addComponent(new CrouchingColliderComponent())
                .addComponent(new FootColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
                .addComponent(new ColliderComponent()) // Interactions
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                .addComponent(playerActions)
                .addComponent(new CombatStatsComponent(stats.health, stats.baseAttack))
                .addComponent(new InventoryComponent())
                .addComponent(input)
                .addComponent(new PlayerStatsDisplay())
                .addComponent(new DamageIndicatorUI())
                .addComponent(new CameraComponent())
                .addComponent(new PlayerScreenTransitionComponent())
                .addComponent(new PlayerDeathEffectComponent())
                .addComponent(new MinimapComponent("images/minimap_player_marker.png")
                        .setScaleY(0.7f).setScaleX(0.5f))
                .addComponent(animator)
                .addComponent(new PlayerAnimationController(playerActions));
        // Stamina + sprint wiring
        StaminaComponent stamina = new StaminaComponent(100f, 10f, 25f, 20);
        player.addComponent(stamina);


        player.getEvents().addListener("sprintStart", () -> {
            if (!stamina.isExhausted() && stamina.getCurrentStamina() > 0) {
                stamina.setSprinting(true);
            }
        });
        player.getEvents().addListener("sprintStop", () -> stamina.setSprinting(false));

        // Colliders
        player.getComponent(ColliderComponent.class)
                .setAsBox(HITBOX_SCALE, player.getCenterPosition().add(HITBOX_OFFSET));
        player.getComponent(ColliderComponent.class).setSensor(true);
        player.getComponent(HitboxComponent.class)
                .setAsBox(HITBOX_SCALE, player.getCenterPosition().add(HITBOX_OFFSET));

        // Physics visual size
        player.getComponent(StandingColliderComponent.class).setDensity(1.5f);
        player.getComponent(CrouchingColliderComponent.class).setDensity(1.5f);
        float scaleFactor = 2f;
        player.setScale(scaleFactor, (3f / 4f) * scaleFactor);

        // Start idle animation
        player.getComponent(AnimationRenderComponent.class).startAnimation("IDLE");
        // Attach popup listener for achievements
        AchievementToastUI toast = new AchievementToastUI();
        player.addComponent(toast);
        AchievementService.get().addListener(toast);
        player.addComponent(new com.csse3200.game.components.achievements.AchievementsTrackerComponent());        // Replace with any extra components passed in
        for (Component c : componentList) {
            player.replaceComponent(c);
        }

        return player;
    }

    private PlayerFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}

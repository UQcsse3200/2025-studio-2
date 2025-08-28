package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.StartPositionComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.PatrolTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.BaseEntityConfig;
import com.csse3200.game.entities.configs.EnemyConfigs;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class EnemyFactory {
    private static final EnemyConfigs configs =
            FileLoader.readClass(EnemyConfigs.class, "configs/enemies.json");

    /**
     * Creates a drone entity.
     *
     * @param target entity to chase
     * @param route for enemy patrol
     * @return entity
     */
    public static Entity createDrone(Entity target, Vector2 startPos, Vector2[] route) {
        Entity drone = createBaseEnemy(target);
        BaseEntityConfig config = configs.drone;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        // Future: Implement drone-specific animation
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new PatrolTask(route, 0.5f))
                        .addTask(new ChaseTask(target, 10, 3f, 4f));
        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(aiComponent)
                .addComponent(animator)
                .addComponent(new StartPositionComponent(startPos))
                // TO DO: Swap to DroneAnimationController when added
                .addComponent(new GhostAnimationController());

        drone.getComponent(AnimationRenderComponent.class).scaleEntity();
        return drone;
    }

    /**
     * Creates a generic enemy to be used as a base by specific enemy creation methods.
     * @return enemy
     */
    private static Entity createBaseEnemy(Entity target) {
        /* NOTE: Moved this to createDrone in case we want diff enemy types w/o patrol paths
        AITaskComponent aiComponent =
              new AITaskComponent()
                      .addTask(new WanderTask(new Vector2(2f, 2f), 2f))
                      .addTask(new ChaseTask(target, 10, 3f, 4f));
        */
        Entity enemy =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsMovementComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));
                        //.addComponent(aiComponent);

        PhysicsUtils.setScaledCollider(enemy, 0.9f, 0.4f);
        return enemy;
    }

    private EnemyFactory() {throw new IllegalStateException("Instantiating static util class");}
}

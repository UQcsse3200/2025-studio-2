package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
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

    public static Entity createDrone(Entity target) {
        BaseEntityConfig config = configs.drone;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        // TODO: Implement drone-specific animation
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

        Entity drone = createBaseEnemy(target)
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                // TODO: Swap to DroneAnimationController when added
                .addComponent(new GhostAnimationController());

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        aiComponent
                //TODO: Implement light-activated chase
                .addTask(new ChaseTask(target, 10, 3f, 4f));
        drone.getComponent(AnimationRenderComponent.class).scaleEntity();
        return drone;
    }

    public static Entity createPatrollingDrone(Entity target, Vector2 patrolStart, Vector2[] patrolSteps) {
        Entity drone = createDrone(target);

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        aiComponent
                .addTask(new PatrolTask(patrolStart, patrolSteps, 1f));
        return drone;
    }

    /**
     * Creates a generic enemy to be used as a base by specific enemy creation methods.
     * @return enemy
     */
    private static Entity createBaseEnemy(Entity target) {
        Entity enemy =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsMovementComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f))
                        .addComponent(new AITaskComponent());

        PhysicsUtils.setScaledCollider(enemy, 0.9f, 0.4f);
        return enemy;
    }

    private EnemyFactory() {throw new IllegalStateException("Instantiating static util class");}
}

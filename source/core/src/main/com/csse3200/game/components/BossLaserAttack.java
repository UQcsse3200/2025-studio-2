package com.csse3200.game.components;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.BossLaserFactory;
import com.csse3200.game.entities.factories.LaserFactory;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * BossLaserAttack component handles:
 * - tracking the player
 * - rendering the laser
 * - timed attack bursts
 * - damaging the player
 */
public class BossLaserAttack extends Component {
    private static final float ATTACK_DURATION = 3f; // seaconds laser stays active
    private static final float COOLDOWN = 6f;         // seconds between bursts

    private float attackTimer = 0f;
    private boolean attacking = true;

    private Entity target;// player to track
    private PhysicsEngine physicsEngine;
    private CombatStatsComponent combatStats;


    // Raycast collision masks
    private static final short blockedOccluder = PhysicsLayer.OBSTACLE;
    private static final short playerOccluder = PhysicsLayer.PLAYER;
    private static final short hitMask = (short) ( blockedOccluder | playerOccluder);

    public BossLaserAttack(Entity target) {
        this.target = target;
    }

    @Override
    public void create() {
        physicsEngine = ServiceLocator.getPhysicsService().getPhysics();
        if (physicsEngine == null) throw new IllegalStateException("Physics engine not found");
        combatStats = entity.getComponent(CombatStatsComponent.class);
        attacking = true;
        attackTimer = 0f;
    }

    @Override
    public void update() {
        float delta = ServiceLocator.getTimeSource().getDeltaTime();

        // Update attack timer
        attackTimer += delta;
        if (attacking && attackTimer >= ATTACK_DURATION) {
            attacking = false;
            attackTimer = 0f;
            entity.removeComponent(this);
            return;
        } else if (!attacking && attackTimer >= COOLDOWN) {
            attacking = true;
            attackTimer = 0f;
            spawnBossLaser();
        }

        if (!attacking || (target == null) )return;
        //performLaserRaycast();
    }
    private void spawnBossLaser() {
        Vector2 bossPosition = entity.getCenterPosition().cpy();
        Vector2 playerPosition = target.getCenterPosition().cpy();
        // Calculate the angle from boss to player
        Vector2 direction = playerPosition.sub(bossPosition);
        float angleRadians = direction.angleRad();
        float angleDegrees = direction.angleDeg();
        entity.getEvents().trigger("shootLaserStart");

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                Entity laser = BossLaserFactory.createBossLaser(target,angleDegrees-10f);
                laser.setScale(7f,500f);
                laser.setPosition(bossPosition.x+0.8f, bossPosition.y+0.5f);
                ServiceLocator.getEntityService().register(laser);

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        laser.dispose();
                    }
                }, 1f);
            }
        }, 0.8f);
    }

    /*private void performLaserRaycast() {
        Vector2 bossPosition = entity.getCenterPosition().cpy();
        Vector2 playerPosition = target.getCenterPosition().cpy();
        // Calculate the angle from boss to player
        Vector2 direction = playerPosition.sub(bossPosition);
        float angleRadians = direction.angleRad();
        float angleDegrees = direction.angleDeg();

        RaycastHit hit = new RaycastHit();
        boolean hitSomething = physicsEngine.raycast(bossPosition, playerPosition, hitMask, hit);

        if (hitSomething) {
            short cat = hit.fixture != null ? hit.fixture.getFilterData().categoryBits : blockedOccluder;
            boolean isPlayer = (cat & playerOccluder) != 0;

            if (isPlayer) damagePlayer(hit);
        }
    }*/

    private void damagePlayer(RaycastHit hit) {
        Entity target = ((BodyUserData) hit.fixture.getBody().getUserData()).entity;
        if (target == null) return;

        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
        if (targetStats != null) targetStats.hit(combatStats);
    }
}

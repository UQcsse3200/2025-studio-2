package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.BossLaserFactory;
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
    private static final float MAX_DISTANCE = 50f;
    private static final float ATTACK_DURATION = 1.5f; // seaconds laser stays active
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
        performLaserRaycast();
    }
    private void spawnBossLaser() {
        Entity laser = BossLaserFactory.createBossLaser(target);
        laser.setPosition(entity.getPosition());
        //gameArea.spawnEntity(laser);
    }

    private void performLaserRaycast() {
        Vector2 start = entity.getCenterPosition().cpy();
        Vector2 direction = target.getCenterPosition().cpy().sub(start).nor();
        Vector2 end = start.cpy().mulAdd(direction, MAX_DISTANCE);

        RaycastHit hit = new RaycastHit();
        boolean hitSomething = physicsEngine.raycast(start, end, hitMask, hit);

        if (hitSomething) {
            short cat = hit.fixture != null ? hit.fixture.getFilterData().categoryBits : blockedOccluder;
            boolean isPlayer = (cat & playerOccluder) != 0;

            if (isPlayer) damagePlayer(hit);
        }
    }

    private void damagePlayer(RaycastHit hit) {
        Entity target = ((BodyUserData) hit.fixture.getBody().getUserData()).entity;
        if (target == null) return;

        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
        if (targetStats != null) targetStats.hit(combatStats);
    }
}

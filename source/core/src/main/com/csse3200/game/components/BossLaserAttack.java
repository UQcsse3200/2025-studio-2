package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.BossLaserRenderComponent;
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
    private static final int MAX_REBOUNDS = 8;
    private static final float MAX_DISTANCE = 50f;
    private static final float KNOCKBACK = 10f;

    private static final float ATTACK_DURATION = 1.5f; // seaconds laser stays active
    private static final float COOLDOWN = 6f;         // seconds between bursts

    private final List<Vector2> positions = new ArrayList<>();
    private float dir = 0f;
    private float attackTimer = 0f;
    private boolean attacking = true;

    private Entity target; // player to track
    private PhysicsEngine physicsEngine;
    private CombatStatsComponent combatStats;
    private BossLaserRenderComponent renderComponent;

    // Raycast collision masks
    private static final short reboundOccluder = PhysicsLayer.LASER_REFLECTOR;
    private static final short blockedOccluder = PhysicsLayer.OBSTACLE;
    private static final short playerOccluder = PhysicsLayer.PLAYER;
    private static final short hitMask = (short) (reboundOccluder | blockedOccluder | playerOccluder);

    public BossLaserAttack(Entity target) {
        this.target = target;
    }

    @Override
    public void create() {
        physicsEngine = ServiceLocator.getPhysicsService().getPhysics();
        if (physicsEngine == null) throw new IllegalStateException("Physics engine not found");
        combatStats = entity.getComponent(CombatStatsComponent.class);
        renderComponent = entity.getComponent(BossLaserRenderComponent.class);
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
            positions.clear(); // clear laser when not attacking
            return;
        } else if (!attacking && attackTimer >= COOLDOWN) {
            attacking = true;
            attackTimer = 0f;
        }

        if (!attacking) return;

        // Track player
        if (target != null) {
            Vector2 direction = target.getCenterPosition().cpy().sub(entity.getCenterPosition());
            dir = direction.angleDeg();
        }

        buildLaser();
        updateRender();
    }

    private void buildLaser() {
        positions.clear();
        Vector2 start = entity.getCenterPosition().cpy();
        positions.add(start.cpy());

        Vector2 dirVec = new Vector2(1f, 0f).rotateDeg(dir).nor();
        float remaining = MAX_DISTANCE;
        int rebounds = 0;

        while (rebounds <= MAX_REBOUNDS && remaining > 0f) {
            Vector2 end = start.cpy().mulAdd(dirVec, remaining);
            RaycastHit hit = new RaycastHit();
            boolean hitSomething = physicsEngine.raycast(start, end, hitMask, hit);

            if (!hitSomething) {
                positions.add(end);
                break;
            }

            float travelled = start.dst(hit.point);
            remaining -= travelled;
            positions.add(hit.point.cpy());
            if (remaining <= 0f) break;

            short cat = categoryBitsFromHit(hit);
            boolean isReflector = (cat & reboundOccluder) != 0;
            boolean isPlayer = (cat & playerOccluder) != 0;

            if (isReflector) {
                Vector2 n = hit.normal.cpy().nor();
                dirVec = reflect(dirVec, n).nor();
                start.set(hit.point).mulAdd(dirVec, 1e-4f);
                rebounds++;
            } else {
                if (isPlayer) damagePlayer(hit);
                break;
            }
        }
    }
    private void updateRender() {
        if (renderComponent != null) {
            renderComponent.setLaserPositions(positions);
        }
    }

    private static short categoryBitsFromHit(RaycastHit hit) {
        if (hit.fixture != null) return hit.fixture.getFilterData().categoryBits;
        return blockedOccluder;
    }

    private static Vector2 reflect(Vector2 d, Vector2 n) {
        float dot = d.dot(n);
        return new Vector2(d.x - 2f * dot * n.x, d.y - 2f * dot * n.y);
    }

    private void damagePlayer(RaycastHit hit) {
        Entity target = ((BodyUserData) hit.fixture.getBody().getUserData()).entity;
        if (target == null) return;

        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
        if (targetStats != null) targetStats.hit(combatStats);

        PhysicsComponent physics = target.getComponent(PhysicsComponent.class);
        if (physics != null) {
            Vector2 direction = target.getCenterPosition().cpy().sub(hit.point).nor();
            physics.getBody().applyLinearImpulse(direction.scl(KNOCKBACK), physics.getBody().getWorldCenter(), true);
        }
    }

    public List<Vector2> getPositions() { return positions; }
    @Override
    public void dispose(){}
}

package com.csse3200.game.components.lasers;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

public class LaserEmitterComponent extends Component {
    private static final int MAX_REBOUNDS = 8;
    private static final float MAX_DISTANCE = 50f;
    private static final short reboundOccluder = PhysicsLayer.LASER_REFLECTOR;
    private static final short blockedOccluder = (short) (
            PhysicsLayer.OBSTACLE
            // | PhysicsLayer.DEFAULT
            );
    private static final short hitMask = (short) (reboundOccluder | blockedOccluder);

    private final List<Vector2> positions = new ArrayList<>();
    private float dir = 90f;
    private PhysicsEngine physicsEngine;

    public LaserEmitterComponent() {

    }

    public LaserEmitterComponent(float dir) {
        this.dir = dir;
    }

    @Override
    public void create() {
        physicsEngine = ServiceLocator.getPhysicsService().getPhysics();
        if (physicsEngine == null) {
            throw new IllegalStateException("Physics engine not found");
        }
    }

    @Override
    public void update() {
        positions.clear();
        // add initial point
        Vector2 start = entity.getPosition().cpy();
        positions.add(start.cpy());

        Vector2 dirVec = new Vector2(1f, 0f).rotateDeg(dir).nor();

        float remaining = MAX_DISTANCE;
        int rebounds = 0;

        while (rebounds <= MAX_REBOUNDS && remaining > 0f) {
            Vector2 end = start.cpy().mulAdd(dirVec, remaining);

            RaycastHit hit = new RaycastHit();
            boolean hitSomething = physicsEngine.raycast(start, end, hitMask, hit);

            // if no hit on block and rebound laser reaches max dist hitting nothing
            if (!hitSomething) {
                positions.add(end);
                break;
            }

            // travel to first hit
            float travelled = start.dst(hit.point);
            remaining -= travelled;
            positions.add(hit.point.cpy());
            if (remaining <= 0f) break;

            // classify reflector or blocker
            short cat = categoryBitsFromHit(hit);
            boolean isReflector = (cat & reboundOccluder) != 0;

            if (isReflector) {
                // reflect r = d -2(d.n) n
                Vector2 n = hit.normal.cpy().nor();
                dirVec = reflect(dirVec, n).nor();

                // continue from just past the hit to avoid re-hit
                start.set(hit.point).mulAdd(dirVec, 1e-4f);
                rebounds++;
            } else {
                // is blocker so stop
                break;
            }
        }
    }

    private static short categoryBitsFromHit(RaycastHit hit) {
        if (hit.fixture != null) {
            return hit.fixture.getFilterData().categoryBits;
        }
        // fallback to blocker
        return blockedOccluder;
    }

    private static Vector2 reflect(Vector2 d, Vector2 n) {
        float dot = d.dot(n);
        return new Vector2(
                d.x - 2f * dot * n.x,
                d.y - 2f * dot * n.y
        );
    }

    public List<Vector2> getPositions() {
        return positions;
    }

    @Override
    public void dispose() {
        positions.clear();
    }
}

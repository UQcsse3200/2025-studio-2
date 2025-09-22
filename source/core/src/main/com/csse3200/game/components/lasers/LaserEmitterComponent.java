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
    private static final short blockedOccluder = PhysicsLayer.OBSTACLE;

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
        Vector2 start = entity.getPosition().cpy();
        positions.add(start.cpy());

        Vector2 dirVec = new Vector2(1f, 0f).setAngleDeg(dir).nor();

        float remaining = MAX_DISTANCE;
        int rebounds = 0;

        RaycastHit hitBlock = new RaycastHit();
        RaycastHit hitRebound = new RaycastHit();

        boolean isHitBlock;
        boolean isHitRebound;

        while (rebounds <= MAX_REBOUNDS && remaining > 0f) {
            Vector2 end = start.cpy().mulAdd(dirVec, remaining);

            isHitBlock = physicsEngine.raycast(start, end, blockedOccluder, hitBlock);
            isHitRebound = physicsEngine.raycast(start, end, reboundOccluder, hitRebound);

            // if no hit on block and rebound laser reaches max dist hitting nothing
            if (!isHitBlock && !isHitRebound) {
                positions.add(end);
                break;
            }

            // decide which hit is first if both are hit
            float dBlock2 = isHitBlock ? start.dst2(hitBlock.point) : Float.POSITIVE_INFINITY;
            float dRefl2 = isHitRebound ? start.dst2(hitRebound.point) : Float.POSITIVE_INFINITY;

            boolean takeBlock = dBlock2 < dRefl2;

            if  (takeBlock) {
                positions.add(hitBlock.point.cpy());
                break;
            } else {
                // reflect
                positions.add(hitRebound.point.cpy());

                // remaining distance after reaching the reflector
                float traveled = (float) Math.sqrt(dRefl2);
                remaining -= traveled;
                if (remaining <= 0f) break;

                // reflect dir: r = d - 2(d-n) n
                Vector2 n = hitRebound.normal.cpy().nor();
                dirVec = reflect(dirVec, n);

                // start next segment just past the hit point to avoid re-hitting
                start.set(hitRebound.point).mulAdd(dirVec, 1e-4f);
                rebounds++;
            }
        }
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

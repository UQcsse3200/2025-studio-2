package com.csse3200.game.components.lasers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class LaserShowerComponentTest {
    private LaserShowerComponent makeLaserAt(Vector2 pos, float dirDeg, PhysicsEngine engine) {
        // swap in physics engine for deterministic ray casts
        ServiceLocator.registerPhysicsService(new PhysicsService(engine));

        Entity emitter = new Entity();
        emitter.setPosition(pos);
        LaserShowerComponent laser = new LaserShowerComponent(dirDeg);
        emitter.addComponent(laser);
        emitter.create();
        return laser;
    }

    @BeforeEach
    void setUp() {
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);

        ServiceLocator.registerEntityService(new EntityService());

        RenderService mockRenderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(mockRenderService);
    }

    @Test
    void create_shouldThrowIfPhysicsMissing() {
        // set null physics engine
        ServiceLocator.registerPhysicsService(new PhysicsService(null));
        Entity emitter = new Entity().addComponent(new LaserShowerComponent());
        assertThrows(IllegalStateException.class, emitter::create);
    }

    @Test
    void positionsContainStartAndMaxDistanceEndWhenNothingHit() {
        // create new fake engine will no hits (meaning all ray casts will default to no hits)
        TestPhysicsEngine engine = TestPhysicsEngine.alwaysMiss();

        Vector2 startPos = new Vector2(10f, 4f);
        LaserShowerComponent laser = makeLaserAt(startPos, 0f, engine);

        // run calculations
        laser.fireLaser();
        List<Vector2> points = laser.getPositions();

        // first point is start pos + center offset (0.5, 0.5)
        Vector2 expectStartPos = startPos.cpy().add(0.5f, 0.5f);
        assertEquals(2, points.size());
        assertEquals(expectStartPos.x, points.get(0).x, 1e-4);
        assertEquals(expectStartPos.y, points.get(0).y, 1e-4);

        // MAX_DISTANCE = 50, so end will be: start + 50 * dir (1,0)
        Vector2 expectEndPos = expectStartPos.add(50f, 0f);
        assertEquals(expectEndPos.x, points.get(1).x, 1e-4);
        assertEquals(expectEndPos.y, points.get(1).y, 1e-4);
    }

    @Test
    void stopsAtFirstBlockingHit() {
        // script a single blocking hit at +20 units along the ray, no
        // real fixture is needed, treats all null as a blocker
        Vector2 startPos = new Vector2(1f, 2f);
        Vector2 start = startPos.cpy().add(0.5f, 0.5f);

        Vector2 hitPoint = start.cpy().add(20f, 0f);
        TestPhysicsEngine engine = TestPhysicsEngine.scripted(List.of(
                TestPhysicsEngine.blockingHit(hitPoint)
        ));

        LaserShowerComponent laser = makeLaserAt(startPos, 0f, engine);

        laser.fireLaser();
        List<Vector2> points = laser.getPositions();

        assertEquals(2, points.size());
        assertEquals(start.x, points.get(0).x, 1e-4);
        assertEquals(start.y, points.get(0).y, 1e-4);
        assertEquals(hitPoint.x, points.get(1).x, 1e-4);
        assertEquals(hitPoint.y, points.get(1).y, 1e-4);
    }

    @Test
    void laserDirectionAffectsRay() {
        TestPhysicsEngine engine = TestPhysicsEngine.alwaysMiss();

        Vector2 startPos =  new Vector2(15f, 5f);
        LaserShowerComponent laser = makeLaserAt(startPos, 90f, engine);

        laser.fireLaser();
        List<Vector2> points = laser.getPositions();

        Vector2 start = startPos.cpy().add(0.5f, 0.5f);
        Vector2 end = start.cpy().add(0f, 50f);

        assertEquals(2, points.size());
        assertEquals(start.x, points.get(0).x, 1e-4);
        assertEquals(start.y, points.get(0).y, 1e-4);
        assertEquals(end.x, points.get(1).x, 1e-4);
        assertEquals(end.y, points.get(1).y, 1e-4);
    }

    @Test
    void reflectsOffVerticalMirror_reversesX() {
        // start at (5,5), aim +X, first hit at +20 on vertical wall with normal (-1,0)
        // After reflection, direction becomes -X, with 30 units remaining. ends 10 units left of start
        Vector2 entityPos = new Vector2(5f, 5f);
        Vector2 start = entityPos.cpy().add(0.5f, 0.5f);
        Vector2 firstHit = start.cpy().add(20f, 0);
        Vector2 normal = new Vector2(-1f, 0f); // normal pointing left (wall facing +X)

        TestPhysicsEngine engine = TestPhysicsEngine.scripted(List.of(
                TestPhysicsEngine.reflectorHit(firstHit, normal),
                TestPhysicsEngine.miss()
        ));

        LaserShowerComponent laser = makeLaserAt(entityPos, 0f, engine);
        laser.fireLaser();
        List<Vector2> points = laser.getPositions();

        assertEquals(3, points.size());
        // start
        assertEquals(start.x, points.get(0).x, 1e-4);
        assertEquals(start.y, points.get(0).y, 1e-4);
        // first hit
        assertEquals(firstHit.x, points.get(1).x, 1e-4);
        assertEquals(firstHit.y, points.get(1).y, 1e-4);
        // final end: 30 units left from first hit
        Vector2 end = firstHit.cpy().add(-30f, 0f);
        assertEquals(end.x, points.get(2).x, 1e-4);
        assertEquals(end.y, points.get(2).y, 1e-4);
    }

    @Test
    void reflectsUpFromFortyFiveDegMirror() {
        // aim +X, hit a 45 deg mirror whose normal is (-sqrt(2)/2, sqrt(2)/2).
        // reflection of (1,0) about that normal, to (0,1) (straight up)

        Vector2 entityPos = new Vector2(0f, 0f);
        Vector2 start = entityPos.cpy().add(0.5f, 0.5f);
        Vector2 firstHit = start.cpy().add(10f, 0);

        Vector2 normal = new Vector2((float) Math.sqrt(0.5f) * -1f, (float) Math.sqrt(0.5f));

        TestPhysicsEngine engine = TestPhysicsEngine.scripted(List.of(
                TestPhysicsEngine.reflectorHit(firstHit, normal),
                TestPhysicsEngine.miss()
        ));

        LaserShowerComponent laser = makeLaserAt(entityPos, 0f, engine);
        laser.fireLaser();
        List<Vector2> points = laser.getPositions();
        assertEquals(3, points.size());
        // start
        assertEquals(start.x, points.get(0).x, 1e-4);
        assertEquals(start.y, points.get(0).y, 1e-4);
        // first hit
        assertEquals(firstHit.x, points.get(1).x, 1e-4);
        assertEquals(firstHit.y, points.get(1).y, 1e-4);
        // remaining distance: 50 - 10 = 40 straight up
        Vector2 end = firstHit.cpy().add(0f, 40f);
        assertEquals(end.x, points.get(2).x, 1e-4);
        assertEquals(end.y, points.get(2).y, 1e-4);
    }

    @Test
    void twoReflections_chainAcrossLShape() {
        /*
        * path:
        *   1) start at (2,7)
        *   2) hit first reflector 15 units to the right of start. reflector is reflecting
        *      laser straight up, normal of (-sqrt(2)/2, sqrt(2)/2)
        *   3) hit second reflector after 10 units directly above first reflector. reflects
        *      laser to the left, normal of (-sqrt(2)/2, -sqrt(2)/2)
        *   4) laser travels for a further 25 units hitting nothing
        *
        * path kinda looks like this i guess:
        *
        *    ----------\
        *             |
        *             |
        *      -------/
        *
        * */
        Vector2 entityPos = new Vector2(2f, 7f);
        Vector2 start = entityPos.cpy().add(0.5f, 0.5f);
        Vector2 hit1 = start.cpy().add(15f, 0f);
        Vector2 hit2 = hit1.cpy().add(0f, 10f);
        Vector2 norm1 = new Vector2((float) Math.sqrt(0.5f) * -1f, (float) Math.sqrt(0.5f));
        Vector2 norm2 = new Vector2((float) Math.sqrt(0.5f) * -1f, (float) Math.sqrt(0.5f) * -1f);

        TestPhysicsEngine engine = TestPhysicsEngine.scripted(List.of(
                TestPhysicsEngine.reflectorHit(hit1, norm1),
                TestPhysicsEngine.reflectorHit(hit2, norm2),
                TestPhysicsEngine.miss()
        ));

        LaserShowerComponent laser = makeLaserAt(entityPos, 0f, engine);
        laser.fireLaser();

        List<Vector2> points = laser.getPositions();
        assertEquals(4, points.size());

        // start
        assertEquals(start.x, points.get(0).x, 1e-4);
        assertEquals(start.y, points.get(0).y, 1e-4);
        // hit reflector 1
        assertEquals(hit1.x, points.get(1).x, 1e-4);
        assertEquals(hit1.y, points.get(1).y, 1e-4);
        // hit reflector 2
        assertEquals(hit2.x, points.get(2).x, 1e-4);
        assertEquals(hit2.y, points.get(2).y, 1e-4);
        // final, travels 25 units left of hit 2
        Vector2 end = hit2.cpy().add(-25f, 0f);
        assertEquals(end.x, points.get(3).x, 1e-3); // higher offset tolerance set because IDK my math is bad...
        assertEquals(end.y, points.get(3).y, 1e-3); // actually it's because of after each hit the laser is
                                                         // nudged slightly to avoid rehits
    }


    // ======= fake physics engine class :3

    /**
     * Evil PhysicsEngine stub that does a FIFO scripting of results.
     * Each result can be: miss, blocking (null fixture), or reflective (fixture with
     * LASER_REFLECTOR physics layer). It builds minimal box2D bodies/fixtures in an internal World
     * when it's needed.
     */
    private static class TestPhysicsEngine extends PhysicsEngine {
        private final Queue<Result> results = new ArrayDeque<>();

        record Result(boolean hit, Vector2 point, Vector2 normal, Fixture fixture) {}

        static TestPhysicsEngine scripted(List<Result> plan) {
            TestPhysicsEngine engine = new TestPhysicsEngine();
            engine.results.addAll(plan);
            return engine;
        }

        static TestPhysicsEngine alwaysMiss() {
            return scripted(List.of());
        }

        static Result miss() {
            return new Result(false, null, null, null);
        }

        static Result blockingHit(Vector2 point) {
            // the normal isn't used for blockers but a normal value is returned
            return new Result(true, point.cpy(), new Vector2(0f, 1f), null);
        }

        static Result reflectorHit(Vector2 point, Vector2 normal) {
            // build a tiny static body with a fixture with LASER_REFLECTOR bits
            World w =  new World(new Vector2(0, 0), true);
            BodyDef bd = new BodyDef();
            bd.type = BodyDef.BodyType.StaticBody;
            bd.position.set(point);
            Body b =  w.createBody(bd);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.1f, 0.1f);
            FixtureDef fd = new FixtureDef();
            fd.shape = shape;
            fd.filter.categoryBits = PhysicsLayer.LASER_REFLECTOR;
            fd.filter.maskBits = -1;
            Fixture fx = b.createFixture(fd);
            shape.dispose();

            return new Result(true, point.cpy(), normal.cpy(), fx);
        }

        @Override
        public boolean raycast(Vector2 from, Vector2 to, short mask, RaycastHit out) {
            Result r = results.poll();
            if (r == null || !r.hit) {
                // default to miss
                return false;
            }

            out.point = r.point.cpy();
            out.normal = r.normal != null ? r.normal.cpy() : new Vector2(0f, 1f);
            out.fixture = r.fixture;
            return true;
        }

        // no ops
        @Override public void update() {}
        @Override public void dispose() {}
    }
}
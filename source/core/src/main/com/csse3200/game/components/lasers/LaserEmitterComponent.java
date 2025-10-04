package com.csse3200.game.components.lasers;


import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * This component constructs a list of points where a laser beam would collide with objects.
 * If the "beam" were to collide with a collider that has the {@code PhysicsLayer.LASER_REFLECTOR} layer
 * than it calculates the reflected angle based on the normal of the surface and incoming angle.
 * <p>
 * The laser beam is limited to a max number of rebounds defined by {@code MAX_REBOUNDS} as well as a max distance
 * defined by {@code MAX_DISTANCE}. If either of these limits are hit than the laser stops prematurely.
 * <p>
 * Additionally the laser will attempt to deal a set amount of damage to any entity with the {@code PhysicsLayer.PLAYER}
 * layer, this can be extended to try to deal damage to other targets with different physics layers too.
 * <p>
 * The list of points in world space is then exposed globally by a getter method which can be accessed by the
 * dedicated laser renderer to render lines between the points.
 */
public class LaserEmitterComponent extends Component {
    private static final int MAX_REBOUNDS = 8;
    private static final float MAX_DISTANCE = 50f;
    private static final float KNOCKBACK = 10f;
    private static final String LASER_SOUND = "sounds/laserShower.mp3";


    private static final short reboundOccluder = PhysicsLayer.LASER_REFLECTOR;
    private static final short blockedOccluder = (short) (
            PhysicsLayer.OBSTACLE
            // | PhysicsLayer.DEFAULT
            );
    private static final short playerOccluder = PhysicsLayer.PLAYER;
    private static final short hitMask = (short) (reboundOccluder | blockedOccluder |  playerOccluder);

    private final List<Vector2> positions = new ArrayList<>();
    private float dir = 90f;
    private PhysicsEngine physicsEngine;
    private CombatStatsComponent combatStats;
    private static final float FIRE_COOLDOWN = 0.3f; // seconds between shots
    private float timeSinceLastShot = 0f;
    private boolean laserActive = false;


    private List<Entity> lastReflectorsHit = new ArrayList<>();

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
        combatStats = entity.getComponent(CombatStatsComponent.class);
        entity.getEvents().addListener("shootLaser", () -> laserActive = true);
        entity.getEvents().addListener("laserOff", this::stopLaser);
    }

    public void fireLaser() {
        /*
        * within this update a few calculations are done to construct out
        * list of collisions our laser makes.
        *
        * firstly we start by getting the initial position of the laser which
        * is offset by a set value. then from there we raycast until hitting any collider
        * within out mask. after it's determined what type of collider layer is hit, if it's
        * an obstacle than the laser stops. if the collider is a reflector then the angle of
        * reflection is calculated using the impact angle and the normal vector of the surface
        * hit. the process is repeated until we run out of rebounds or length.
        * */
        Sound laserSound = ServiceLocator.getResourceService().getAsset(LASER_SOUND, Sound.class);
        if (laserSound != null) {
            long soundId = laserSound .play(1.0f);
            fadeOutSound(laserSound , soundId);
        }
        
        positions.clear();
        // add initial point
        Vector2 start = entity.getPosition().cpy().add(0.5f, 0.5f); // offset to centre
        positions.add(start.cpy());

        Vector2 dirVec = new Vector2(1f, 0f).rotateDeg(dir).nor();

        float remaining = MAX_DISTANCE;
        int rebounds = 0;
        List<Entity> reflectorsHit = new ArrayList<>();

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
            boolean isPlayer = (cat & playerOccluder) != 0;

            if (isReflector) {
                // reflect r = d -2(d.n) n
                Vector2 n = hit.normal.cpy().nor();
                dirVec = reflect(dirVec, n).nor();

                // continue from just past the hit to avoid re-hit
                start.set(hit.point).mulAdd(dirVec, 1e-4f);
                rebounds++;

                // add hit entity to reflectors hit list
                Entity e = ((BodyUserData) hit.fixture.getBody().getUserData()).entity;
                if (e != null) {
                    reflectorsHit.add(e);
                }
            } else {
                if (isPlayer) {
                    damagePlayer(hit);
                }
                // is blocker so stop
                break;
            }
        }

        // trigger updates to reflectors (for texture changes)
        for (Entity e : reflectorsHit) {
            if (!lastReflectorsHit.contains(e)) {
                e.getEvents().trigger("laserHit", true);
            }
            lastReflectorsHit.remove(e);
        }
        for (Entity e : lastReflectorsHit) {
            if (!reflectorsHit.contains(e)) {
                e.getEvents().trigger("laserOff", false);
            }
        }
        lastReflectorsHit = reflectorsHit;
    }

    private void fadeOutSound(Sound sound, long soundId) {
        final int steps = 10;
        final float interval = (float) 1.0 / steps;

        for (int i = 0; i < steps; i++) {
            final float volume = 1.0f - (i / (float) steps);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    sound.setVolume(soundId, volume);
                    if (volume <= 0f) sound.stop(soundId);
                }
            }, i * interval);
        }
    }
    /**
     * A null safe wrapper for getting the category bits from a hit collider.
     *
     * @param hit the hit collider from a raycast
     * @return the category bits of the collider
     */
    private static short categoryBitsFromHit(RaycastHit hit) {
        if (hit.fixture != null) {
            return hit.fixture.getFilterData().categoryBits;
        }
        // fallback to blocker
        return blockedOccluder;
    }
    @Override
    public void update() {
        if (timeSinceLastShot > 0) {
            timeSinceLastShot -= ServiceLocator.getTimeSource().getDeltaTime();
        }

        // Only fire if laser is active AND cooldown expired
        if (laserActive && timeSinceLastShot <= 0f) {
            fireLaser();
            timeSinceLastShot = FIRE_COOLDOWN; // reset cooldown
        }
    }



    /**
     * Calculates the reflected angle based off the impact vector {@code d}, and
     * the normal vector of the hit surface {@code n}.
     *
     * @param d impact direction hit vector
     * @param n surface normal vector
     * @return reflected vector
     */
    private static Vector2 reflect(Vector2 d, Vector2 n) {
        float dot = d.dot(n);
        return new Vector2(
                d.x - 2f * dot * n.x,
                d.y - 2f * dot * n.y
        );
    }

    /**
     * A helper method that attempts to apply damage to a target based on a hit collider.
     * <p>
     * This code is essentially just taken from the {@code TouchAttackComponent}
     *
     * @param hit the raycast hit result
     */
    private void damagePlayer(RaycastHit hit) {
        Entity target = ((BodyUserData) hit.fixture.getBody().getUserData()).entity;
        if (target == null) return;

        // attack target
        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
        if  (targetStats != null) {
            targetStats.hit(combatStats);
        }

        // apply knockback
        PhysicsComponent physics = target.getComponent(PhysicsComponent.class);
        if (physics != null) {
            Body targetBody = physics.getBody();
            Vector2 direction = target.getCenterPosition().cpy().sub(hit.point).nor();
            Vector2 impulse = direction.setLength(KNOCKBACK);
            targetBody.applyLinearImpulse(impulse, targetBody.getWorldCenter(), true);
        }
    }

    /**
     * Gets the list of collision positions generated from the laser emitter
     * which can be used to render lines between the points.
     *
     * @return the list of collision points
     */
    public List<Vector2> getPositions() {
        return positions;
    }
    /**
     * Stops the laser (clears positions and turns it off).
     */
    private void stopLaser() {
        laserActive = false;
        positions.clear();

        // Turn off any reflector highlights
        for (Entity e : lastReflectorsHit) {
            e.getEvents().trigger("laserOff", false);
        }
        lastReflectorsHit.clear();
    }


    @Override
    public void dispose() {
        positions.clear();
    }
}

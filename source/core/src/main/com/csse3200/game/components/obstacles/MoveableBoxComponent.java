package com.csse3200.game.components.obstacles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * This component is used to do the interactions between the player and the box entity. It is responsible for both
 * interpreting the interaction when the player presses teh interact button and also for moving the host entity.
 * It does this by taking the mouse pos in the world and slowly moving the box towards the mouse (within a max dist),
 * the box also rotates based on the direction of where the mouse is.
 */
public class MoveableBoxComponent extends Component {
    private static final float INTERACT_RANGE = 1.5f;
    private static final float CARRY_RANGE = 1f;

    private static final float CARRY_GAIN = 18f; // how aggressively it homes
    private static final float CARRY_MAX_SPEED = 10f; // homing speed cap
    private static final float BASE_GRAVITY_SCALE = 0.75f;
    private static final float BASE_LINEAR_DAMPING = 1.5f;

    private Entity player;
    private boolean seenPlayer = false;
    private boolean pickedUp = false;
    private PhysicsComponent boxPhysics;
    private ColliderComponent boxCollider;
    private TextureRenderComponent boxTexture;

    private Camera camera;
    private final Vector2 tmp = new Vector2();
    private final Vector2 dir = new Vector2();
    private final Vector3 mouseTmp = new Vector3();

    private boolean appliedFilter = false;

    private boolean savedFixedRotation;
    private boolean savedBullet;

    /**
     * Sets the internal camera variable.
     * This camera variable is then used when doing mouse pointer calculations
     *
     * @param camera the world camera
     * @return this component
     */
    public MoveableBoxComponent setCamera(Camera camera) {
        this.camera = camera;
        return this;
    }

    @Override
    public void create() {
        boxPhysics = entity.getComponent(PhysicsComponent.class);
        boxCollider = entity.getComponent(ColliderComponent.class);
        boxTexture = entity.getComponent(TextureRenderComponent.class);

        if (boxPhysics == null) {
            throw new IllegalStateException("Physics component is null");
        }

        if (boxCollider == null) {
            throw new IllegalStateException("Collider component is null");
        }

        if (boxTexture == null) {
            throw new IllegalStateException("Texture component is null");
        }

        boxPhysics.getBody().setGravityScale(BASE_GRAVITY_SCALE);
    }

    /**
     * Responsible for the initial interaction on the box.
     * This has to be used to locate the player and then store it locally,
     * it also sets up the required listener.
     *
     * @param collider the player collider
     */
    public void setPlayerInRange(ColliderComponent collider) {
        if (collider == null) {
            return;
        }

        if (!seenPlayer) {
            seenPlayer = true;
            player = collider.getEntity();
            player.getEvents().addListener("interact", this::onPlayerInteract);
        }
    }

    /**
     * Toggle the physics filter so the box no longer interacts with the player
     */
    private void toggleFilter() {
        Filter f = boxCollider.getFixture().getFilterData();
        if (pickedUp) {
            f.maskBits = (short)(f.maskBits & (short) (~PhysicsLayer.PLAYER));
            System.out.println("Filter toggled off");
        } else  {
            f.maskBits = (short) (f.maskBits | PhysicsLayer.PLAYER);
            System.out.println("Filter toggled on");
        }
        boxCollider.getFixture().setFilterData(f);
        boxCollider.getFixture().refilter();
        boxCollider.getFixture().getBody().setAwake(true);
    }

    /**
     * What is run when a player presses the interact key.
     * Only toggles the box lift if player is within the correct range.
     */
    private void onPlayerInteract() {
        System.out.println("Box Interaction");
        if (player == null) {
            return;
        }

        float distance = player.getCenterPosition().dst(entity.getCenterPosition());

        if (distance <= INTERACT_RANGE) {
            toggleLift();
        }
    }

    /**
     * Toggle the box between the lifted and normal states.
     * This includes toggling the physics filter, changing the gravity and
     * various other physics related variables.
     */
    private void toggleLift() {
        pickedUp = !pickedUp;
        Body body = boxPhysics.getBody();

        if (pickedUp) {
            // save
            savedFixedRotation = body.isFixedRotation();
            savedBullet = body.isBullet();

            body.setGravityScale(0f);
            body.setFixedRotation(true);
            body.setBullet(true);
            body.setLinearDamping(20f);
            body.setAngularVelocity(0f);
            body.setLinearVelocity(0f, 0f);
        } else {
            // restore
            body.setGravityScale(BASE_GRAVITY_SCALE);
            body.setFixedRotation(savedFixedRotation);
            body.setBullet(savedBullet);
            body.setLinearDamping(BASE_LINEAR_DAMPING);
            //boxPhysics.getBody().setTransform(boxPhysics.getBody().getPosition(), 0);
            boxTexture.setRotation(0f);
        }
        toggleFilter();
    }

    @Override
    public void update() {
        // this has to be run here just once as the fixture body isn't initialised until
        // after the create() method has been called.
        if (!appliedFilter) {
            if (boxCollider == null) return;
            if (boxCollider.getFixture() == null) return;

            Filter f = boxCollider.getFixture().getFilterData();
            f.categoryBits = (short) PhysicsLayer.LASER_REFLECTOR;
            f.maskBits = (short) (PhysicsLayer.OBSTACLE | PhysicsLayer.PLAYER | PhysicsLayer.NPC);
            boxCollider.getFixture().setFilterData(f);
            boxCollider.getFixture().getBody().setAwake(true);
            appliedFilter = true;
            System.out.println("Filter applied");
        }

        // run following method if box is currently picked up
        if (pickedUp) {
            followPlayer();
        }
    }

    /**
     * The core method driving the following functionality. It gets the mouse
     * position in the world and then calculates a target pos for the box to float at
     * which is scaled off of the {@code CARRY_RANGE}. It then applies a velocity to the box
     * to push it towards the correct position.
     */
    private void followPlayer() {
        if (player == null || camera == null) return;

        // player world pos
        Vector2 playerPos = new Vector2();
        playerPos.set(player.getCenterPosition()); // offset because of weird entity pos stuff

        // mouse in world space
        mouseTmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mouseTmp);

        // direction player -> mouse
        dir.set(mouseTmp.x, mouseTmp.y).sub(playerPos);
        if (dir.isZero(1e-4f)) dir.set(1f, 0f);
        dir.nor();

        // target = player + dir * CARRY_RANGE
        tmp.set(playerPos).mulAdd(dir, CARRY_RANGE);

        // face the mouse
        float angle = MathUtils.atan2(dir.y, dir.x);
        boxTexture.setRotation((float) Math.toDegrees(angle));

        // get delta away from target
        var body = boxPhysics.getBody();
        Vector2 center = body.getWorldCenter();
        Vector2 delta = tmp.cpy().sub(center);

        // velocity proportional to delta with max speed
        float dist = delta.len2();
        Vector2 vel = (dist < 1e-4f) ? delta.setZero()
                                     : delta.scl(CARRY_GAIN);
        if (vel.len2() > CARRY_MAX_SPEED * CARRY_MAX_SPEED) {
            vel.setLength(CARRY_MAX_SPEED);
        }

        // apply correct velocity and don't rotate at all
        body.setAngularVelocity(0f);
        body.setLinearVelocity(vel);

    }

    @Deprecated
    private void followPlayerOld() {
        if (player == null || camera == null) return;

        // player world pos
        Vector2 playerPos = new Vector2();
        playerPos.set(player.getCenterPosition()).sub(0.3f, 0.5f); // offset because of weird entity pos stuff

        // mouse in world space
        mouseTmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mouseTmp);

        // direction player -> mouse
        dir.set(mouseTmp.x, mouseTmp.y).sub(playerPos);
        if (dir.isZero(1e-4f)) dir.set(1f, 0f);
        dir.nor();

        // target = player + dir * CARRY_RANGE
        tmp.set(playerPos).mulAdd(dir, CARRY_RANGE);

        // face the mouse
        float angle = MathUtils.atan2(dir.y, dir.x);

        boxPhysics.getBody().setLinearVelocity(0, 0);
        boxPhysics.getBody().setAngularVelocity(0);
        //boxPhysics.getBody().setTransform(tmp, angle);
        entity.setPosition(tmp);
        boxTexture.setRotation((float) Math.toDegrees(angle));
    }
}

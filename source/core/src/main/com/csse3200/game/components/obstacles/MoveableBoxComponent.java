package com.csse3200.game.components.obstacles;

import box2dLight.ConeLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * This component is used to do the interactions between the player and the box entity. It is responsible for both
 * interpreting the interaction when the player presses teh interact button and also for moving the host entity.
 * It does this by taking the mouse pos in the world and slowly moving the box towards the mouse (within a max dist),
 * the box also rotates based on the direction of where the mouse is.
 */
public class MoveableBoxComponent extends Component {
    private static final float INTERACT_RANGE = 1.5f;
    private static final float CARRY_RANGE = 1f;
    private static final long INTERACT_COOLDOWN_MS = 200;
    private long nextAllowedToggleMs = 0L;

    private static final float CARRY_GAIN = 18f; // how aggressively it homes
    private static final float CARRY_MAX_SPEED = 10f; // homing speed cap
    private static final float MAX_ANG_SPEED = 10f;
    private float BASE_GRAVITY_SCALE = 0.75f;
    private static final float BASE_LINEAR_DAMPING = 1.5f;

    private Entity player;
    private KeyboardPlayerInputComponent inputComp;
    private boolean seenPlayer = false;
    private boolean pickedUp = false;
    private PhysicsComponent boxPhysics;
    private ColliderComponent boxCollider;
    private TextureRenderComponent boxTexture;
    private ConeLightComponent boxLight;
    private short physicsLayer = PhysicsLayer.OBSTACLE;

    private Camera camera;
    private final Vector2 dir = new Vector2();
    private final Vector3 mouseTmp = new Vector3();

    private boolean appliedFilter = false;

    private boolean savedFixedRotation;
    private boolean savedBullet;
    private final Vector2 initPos = new Vector2();

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

    public void setPhysicsLayer(short physicsLayer) {
        this.physicsLayer = physicsLayer;
    }

    public void setBaseGravityScale(float gravityScale) {
        this.BASE_GRAVITY_SCALE = gravityScale;
    }

    @Override
    public void create() {
        boxPhysics = entity.getComponent(PhysicsComponent.class);
        boxCollider = entity.getComponent(ColliderComponent.class);
        boxTexture = entity.getComponent(TextureRenderComponent.class);
        boxLight = entity.getComponent(ConeLightComponent.class);

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
        boxTexture.setOrigin(0f, 0f);
        initPos.set(entity.getPosition());

        // setup listener
        if (boxLight != null) {
            entity.getEvents().addListener("laserHit", this::toggleOn);
            entity.getEvents().addListener("laserOff", this::toggleOn);
        }
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
            inputComp = player.getComponent(KeyboardPlayerInputComponent.class);
            player.getEvents().addListener("interact", this::onPlayerInteract);
            player.getEvents().addListener("death", () -> {
                pickedUp = true;
                toggleLift();
            });
        }
    }

    /**
     * Toggle the physics filter so the box no longer interacts with the player
     */
    private void toggleFilter() {
        Filter f = boxCollider.getFixture().getFilterData();
        if (pickedUp) {
            f.maskBits = (short)(f.maskBits & (short) (~PhysicsLayer.PLAYER));
        } else  {
            f.maskBits = (short) (f.maskBits | PhysicsLayer.PLAYER);
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
        if (player == null || inputComp == null) {
            return;
        }

        // cooldown
        if (ServiceLocator.getTimeSource().getTime() < nextAllowedToggleMs) return;

        float distance = player.getCenterPosition().dst(entity.getCenterPosition());

        // if we're currently picked up allow dropping regardless of distance
        // but only if it is the one the player is holding
        if (pickedUp) {
            if (inputComp.isHoldingBox() && inputComp.getHeldBox().equals(entity)) {
                toggleLift();
                nextAllowedToggleMs = ServiceLocator.getTimeSource().getTime() + INTERACT_COOLDOWN_MS;
            }
            return;
        }

        // not picked up yet, only pick up if player isnt holding any box
        if (!inputComp.isHoldingBox() && distance <= INTERACT_RANGE) {
            toggleLift();
            nextAllowedToggleMs = ServiceLocator.getTimeSource().getTime() + INTERACT_COOLDOWN_MS;
        }
    }

    private void resetToInitPos() {
        pickedUp = false;
        Body body =  boxPhysics.getBody();
        body.setTransform(initPos, 0f);
        entity.setPosition(initPos, false); // this might not be needed but oh well
        boxTexture.setRotation(0f);
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
            // claim ownership on the player
            if (inputComp != null) {
                inputComp.setHoldingBox(true);
                inputComp.setHeldBox(entity);
            }

            // save
            savedFixedRotation = body.isFixedRotation();
            savedBullet = body.isBullet();

            body.setGravityScale(0f);
            body.setFixedRotation(false);
            body.setBullet(true);
            body.setLinearDamping(20f);
            body.setAngularVelocity(0f);
            body.setLinearVelocity(0f, 0f);
        } else {
            // release ownership
            if (inputComp != null) {
                inputComp.setHoldingBox(false);
                inputComp.setHeldBox(null);
            }

            body.setAngularVelocity(0f);
            //body.setLinearVelocity(0f, 0f);

            resetPos(body);

            // restore
            body.setGravityScale(BASE_GRAVITY_SCALE);
            body.setFixedRotation(savedFixedRotation);
            body.setBullet(savedBullet);
            body.setLinearDamping(BASE_LINEAR_DAMPING);
            //boxPhysics.getBody().setTransform(boxPhysics.getBody().getPosition(), 0);
        }
        toggleFilter();
    }

    /**
     * Very cursed helper method that attempts to reset the boxes
     * position angle to the nearest 90 degrees.
     * <p>
     * However because it's all rotated about 0,0 the box can "teleport" sometimes
     * this is what the helper method tries to fix...
     *
     * @param body physics body of the box
     */
    private void resetPos(Body body) {
        // idk weird rounding stuff, im too tired to think about it rn so this works...
        // this resets the angle of the box to the nearest 90 deg when dropped
        float angRounded = Math.round(body.getAngle() / (MathUtils.PI / 2f)) * (MathUtils.PI / 2f);

        Vector2 pos =  body.getPosition().cpy();
        Vector2 offset = new  Vector2();
        float angle = normAng(body.getAngle() * MathUtils.radiansToDegrees);
        int x = (int) (angle / 45);
        // PLEASE JUST DONT READ THIS...
        // idk what even happening now
        switch (x) {
            case 0:
                offset.x = -0.15f * ((angle % 45) / 45f);
                break;
            case 1:
                offset.x = 0.15f * (1 - (angle % 45) / 45f);
                break;
            case 2:
                offset.y = -0.15f * (1 - (angle % 45) / 45f);
                break;
            case 3:
                offset.y = 0.15f * ((angle % 45) / 45f);
                break;
            default:
                offset = Vector2.Zero;
                break;
        }
        pos.add(offset);
        // apply the new cursed offest
        body.setTransform(pos, angRounded);
        entity.setPosition(pos, false); // this might not be needed but oh well
        boxTexture.setRotation(angRounded * MathUtils.radiansToDegrees);
    }

    private float normAng(float a) {
        float x = a % 360;
        if (x < 0) {
            x += 360;
        }
        return x;
    }

    @Override
    public void update() {
        // this has to be run here just once as the fixture body isn't initialised until
        // after the create() method has been called.
        if (!appliedFilter) {
            if (boxCollider == null) return;
            if (boxCollider.getFixture() == null) return;

            Filter f = boxCollider.getFixture().getFilterData();
            f.categoryBits = (short) physicsLayer;
            f.maskBits = (short) (PhysicsLayer.OBSTACLE
                                | PhysicsLayer.PLAYER
                                | PhysicsLayer.NPC
                                | PhysicsLayer.LASER_REFLECTOR
                                | PhysicsLayer.LASER_DETECTOR);
            boxCollider.getFixture().setFilterData(f);
            boxCollider.getFixture().getBody().setAwake(true);
            appliedFilter = true;

            // set initial box state if mirror box
            if (boxLight != null) {
                toggleOn(false);
            }
        }

        // check out of bounds
        if (entity.getPosition().y <= -5f) {
            resetToInitPos();
        }

        // Removes box from pressure plate when player dies
        if (player != null && player.getPosition().y <= -3f) {
            resetToInitPos();
        }

        // run following method if box is currently picked up
        if (pickedUp) {
            followPlayer();
        }
        lightFollow();
    }

    private void toggleOn(boolean on) {
        if (on) {
            boxTexture.setTexture("images/mirror-cube-on.png");
            if (boxLight != null) {
                boxLight.setActive(true);
            }
        } else {
            boxTexture.setTexture("images/mirror-cube-off.png");
            if (boxLight != null) {
                boxLight.setActive(false);
            }
        }
    }

    private void lightFollow() {
        if (boxLight == null) return;
        ConeLight light = boxLight.getLight();
        if (light == null) return;

        Vector2 pos = boxPhysics.getBody().getWorldCenter();
        light.setPosition(pos.x, pos.y);
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
        playerPos.set(player.getCenterPosition());

        // mouse in world space
        mouseTmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mouseTmp);

        // direction player -> mouse
        dir.set(mouseTmp.x, mouseTmp.y).sub(playerPos);
        if (dir.isZero(1e-4f)) dir.set(1f, 0f);
        dir.nor();

        // target = player + dir * CARRY_RANGE // also a small offset :3
        Vector2 target = new Vector2(playerPos).mulAdd(dir, CARRY_RANGE).sub(0.1f, 0.125f);

        // target angle
        float targetAngle = MathUtils.atan2(dir.y, dir.x);

        Body body = boxPhysics.getBody();

        // get delta away from target
        Vector2 center = body.getWorldCenter();
        Vector2 delta = target.cpy().sub(center);

        // velocity proportional to delta with max speed
        float dist = delta.len2();
        Vector2 vel = (dist < 1e-4f) ? delta.setZero()
                                     : delta.scl(CARRY_GAIN);

        if (vel.len2() > CARRY_MAX_SPEED * CARRY_MAX_SPEED) {
            vel.setLength(CARRY_MAX_SPEED);
        }

        // angular velocity to target angle
        float currentAng = body.getAngle();
        float deltaAng = MathUtils.atan2(MathUtils.sin(targetAngle - currentAng),
                                         MathUtils.cos(targetAngle - currentAng));
        //float angVel = 10f * deltaAng - 2f * body.getAngularVelocity();
        float angVel = (Math.abs(deltaAng) < 1e-2f) ? 0f : deltaAng * 10f;
        angVel = MathUtils.clamp(angVel, -MAX_ANG_SPEED, MAX_ANG_SPEED);

        // apply correct velocity and don't rotate at all
        body.setAngularVelocity(angVel);
        body.setLinearVelocity(vel);

        entity.setPosition(body.getPosition(), false);
        boxTexture.setRotation(body.getAngle() * MathUtils.radiansToDegrees);

    }
}

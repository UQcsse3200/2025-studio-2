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

public class MoveableBoxV2Component extends Component {
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
    private ColliderComponent playerCollider;
    private TextureRenderComponent boxTexture;
    private boolean playerInRange;

    private Camera camera;
    private final Vector2 tmp = new Vector2();
    private final Vector2 dir = new Vector2();
    private final Vector3 mouseTmp = new Vector3();

    private boolean appliedFilter = false;

    private boolean savedFixedRotation;
    private boolean savedBullet;

    public MoveableBoxV2Component setCamera(Camera camera) {
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

    public void setPlayerInRange(ColliderComponent collider) {
        if (collider == null) {
            return;
        }

        playerInRange = true;
        playerCollider = collider;

        if (!seenPlayer) {
            seenPlayer = true;
            player = playerCollider.getEntity();
            player.getEvents().addListener("interact", this::onPlayerInteract);
        }
    }

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
        if (!appliedFilter) {
            if (boxCollider == null) return;
            if (boxCollider.getFixture() == null) return;

            Filter f = boxCollider.getFixture().getFilterData();
            f.categoryBits = (short) PhysicsLayer.OBSTACLE;
            f.maskBits = (short) (PhysicsLayer.OBSTACLE | PhysicsLayer.PLAYER | PhysicsLayer.NPC);
            boxCollider.getFixture().setFilterData(f);
            boxCollider.getFixture().getBody().setAwake(true);
            appliedFilter = true;
            System.out.println("Filter applied");
        }

        if (pickedUp) {
            followPlayer();
        }
    }

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

        var body = boxPhysics.getBody();
        Vector2 center = body.getWorldCenter();
        Vector2 delta = tmp.cpy().sub(center);

        // velocity proportional to error with max speed
        float dist = delta.len2();
        Vector2 vel = (dist < 1e-4f) ? delta.setZero()
                                     : delta.scl(CARRY_GAIN);
        if (vel.len2() > CARRY_MAX_SPEED * CARRY_MAX_SPEED) {
            vel.setLength(CARRY_MAX_SPEED);
        }

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

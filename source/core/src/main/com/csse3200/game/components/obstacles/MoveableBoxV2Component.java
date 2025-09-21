package com.csse3200.game.components.obstacles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
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
    private final Vector3 mouseTep = new Vector3();

    private boolean appliedFilter = false;

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
        if (pickedUp) {
            boxPhysics.getBody().setLinearVelocity(0,0);
            boxPhysics.getBody().setAngularVelocity(0);
            boxPhysics.setBodyType(BodyDef.BodyType.KinematicBody);
        } else {
            boxPhysics.setBodyType(BodyDef.BodyType.DynamicBody);
            boxPhysics.getBody().setTransform(boxPhysics.getBody().getPosition(), 0);
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
        playerPos.set(player.getCenterPosition()).sub(0.3f, 0.5f); // offset because of weird entity pos stuff

        // mouse in world space
        mouseTep.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mouseTep);

        // direction player -> mouse
        dir.set(mouseTep.x, mouseTep.y).sub(playerPos);
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

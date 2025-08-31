package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

public class BoxComponent extends Component {

//    private static final float INTERACT_RANGE = 1.5f;
    private static final float LIFT_RANGE = 1f;

    private ColliderComponent playerCollider = null;
    private boolean playerInRange = false;
//    private boolean isPulled = false;
    private boolean isLifted = false;
    private boolean addToPlayer = false;
    private PhysicsComponent boxPhysics;

    @Override
    public void create() {
        boxPhysics = entity.getComponent(PhysicsComponent.class);
    }

    public void setPlayerInRange(ColliderComponent collider) {
        if (collider == null) {
            resetPlayerState();
            return;
        }

        playerInRange = true;
        playerCollider = collider;

        if (!addToPlayer) {
            Entity player = playerCollider.getEntity();
            player.getEvents().addListener("interact", this::onPlayerInteract);
            player.getEvents().addListener("walk", this::onPlayerWalk);
            addToPlayer = true;
        }
    }

    private void resetPlayerState() {
        playerInRange = false;
        playerCollider = null;
        isLifted = false;
    }

    private Entity getPlayer() {
        return (!playerInRange || playerCollider == null) ? null : playerCollider.getEntity();
    }

    private PlayerActions getPlayerActions() {
        Entity player = getPlayer();
        return (player == null) ? null : player.getComponent(PlayerActions.class);
    }

    private void onPlayerInteract() {
        Entity player = getPlayer();
        if (player == null) {
            return;
        }

        float distance = player.getPosition().dst(entity.getPosition());

        if (distance <= LIFT_RANGE) {
            toggleLift();
//        } else if (distance <= INTERACT_RANGE) {
//            togglePull();
        }
    }

    private void toggleLift() {
        isLifted = !isLifted;
        if (isLifted) {
            boxPhysics.setBodyType(BodyDef.BodyType.KinematicBody);
        } else {
            boxPhysics.setBodyType(BodyDef.BodyType.DynamicBody);
            boxPhysics.getBody().setLinearVelocity(0,0);
            boxPhysics.getBody().setAngularVelocity(0);
        }
    }
//
//    private void togglePull() {
//        isPulled = !isPulled;
//    }

    @Override
    public void update() {
        if (isLifted) {
            moveWithPlayer();
        }
    }

    public void onPlayerWalk(Vector2 direction) {
        if (!playerInRange || playerCollider == null) {
            return;
        }
//        if (isPulled) {
//            applyPull();
//        }
        if (isLifted) {
            moveWithPlayer();
        }
    }

//    private void applyPull() {
//        PlayerActions actions = getPlayerActions();
//
//        if (actions == null) {
//            return;
//        }
//
//        Vector2 impulse = new Vector2(actions.getWalkDirection()).scl(-1f).scl(boxPhysics.getBody().getMass());
//        boxPhysics.getBody().applyLinearImpulse(impulse, boxPhysics.getBody().getWorldCenter(), true);
//    }

    private void moveWithPlayer() {
        Entity player = getPlayer();
        if (player == null) {
            return;
        }

        Vector2 liftPosition = player.getPosition().cpy().add(0, 0.5f);
        boxPhysics.getBody().setTransform(liftPosition, 0);
//        entity.setPosition(player.getPosition().cpy().add(0, 1f));
    }

    public void throwBox(float forceMultiplier) {
        if (!isLifted) {
            return;
        }

        PlayerActions actions = getPlayerActions();
        Entity player = getPlayer();
        if (actions == null || player == null) {
            return;
        }

        Vector2 throwDir = new Vector2(actions.getWalkDirection()).nor().scl(forceMultiplier);

        boxPhysics.setBodyType(BodyDef.BodyType.DynamicBody);
        boxPhysics.getBody().applyLinearImpulse(throwDir.scl(boxPhysics.getBody().getMass()), boxPhysics.getBody().getWorldCenter(), true);
        isLifted = false;
    }

}

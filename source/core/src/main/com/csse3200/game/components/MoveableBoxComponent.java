package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

/**
 * Component for a box that can be moved by the player.
 * <p>
 * Handles player interactions with a box, including lifting, carrying and dropping.
 */
public class MoveableBoxComponent extends Component {

    /** Maximum distance for the player to lift the box */
    private static final float LIFT_RANGE = 1f;

    private ColliderComponent playerCollider = null;
    private boolean playerInRange = false;
    private boolean isLifted = false;
    private boolean addToPlayer = false;
    private PhysicsComponent boxPhysics;

    /**
     * Default constructor to remove gradle warnings about missing constructor in this class.
     */
    public MoveableBoxComponent() {
        // Intentionally blank
    }

    /**
     * Retrieves the PhysicsComponent of the box when the component is created.
     */
    @Override
    public void create() {
        boxPhysics = entity.getComponent(PhysicsComponent.class);
    }

    /**
     * Sets the player collider when the player is range of the box.
     * Adds event listeners to the player for interacting and walking.
     *
     * @param collider  The ColliderComponent of the player
     */
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

    /**
     * Resets the state of the box in relation to the player
     */
    private void resetPlayerState() {
        playerInRange = false;
        playerCollider = null;
        isLifted = false;
    }

    /**
     * Gets the player entity currently in range of the box
     *
     * @return
     */
    private Entity getPlayer() {
        return (!playerInRange || playerCollider == null) ? null : playerCollider.getEntity();
    }

    /**
     * Checks if the player is currently in range of the box.
     *
     * @return true if the player is in range, false otherwise
     */
    public boolean isPlayerInRange() {
        return playerInRange;
    }

    /**
     * Checks if the box is currently lifted by the player.
     *
     * @return true if the box is lifted, false otherwise
     */
    public boolean isLifted() {
        return isLifted;
    }

    /**
     * Checks if the event listeners have been added to the player.
     *
     * @return true if listeners are added, false otherwise
     */
    public boolean isAddToPlayer() {
        return addToPlayer;
    }

    /**
     * Retrieves this box's PhysicsComponent.
     *
     * @return the box's PhysicsComponent
     */
    public PhysicsComponent getBoxPhysics() {
        return boxPhysics;
    }

    /**
     * Retrieves the ColliderComponent of the player currently interacting with the box
     *
     * @return  the player's ColliderComponent, or null if no player is in range
     */
    public ColliderComponent getPlayerCollider() {
        return playerCollider;
    }

    /**
     * Retrieves the PlayerActions component of the player currently in range.
     *
     * @return  The PlayerActions component, or null if none
     */
    private PlayerActions getPlayerActions() {
        Entity player = getPlayer();
        return (player == null) ? null : player.getComponent(PlayerActions.class);
    }

    /**
     * Handles the player attempting to lift a box if it is in range.
     */
    private void onPlayerInteract() {
        Entity player = getPlayer();
        if (player == null) {
            return;
        }

        float distance = player.getPosition().dst(entity.getPosition());

        if (distance <= LIFT_RANGE) {
            toggleLift();
        }
    }

    /**
     * Toggles the lifted state of the box.
     */
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

    /**
     * Updates the box every frame.  If the player is lifting the box, the box continues to move
     * with the player.
     */
    @Override
    public void update() {
        if (isLifted) {
            moveWithPlayer();
        }
    }

    /**
     * Called when the player moves.  Updates the box position if it is lifted.
     *
     * @param direction  The direction the player is walking (currently unused)
     */
    public void onPlayerWalk(Vector2 direction) {
        if (!playerInRange || playerCollider == null) {
            return;
        }
        if (isLifted) {
            moveWithPlayer();
        }
    }

    /**
     * moves the lifted box to stay in position with the player
     */
    private void moveWithPlayer() {
        Entity player = getPlayer();
        if (player == null) {
            return;
        }

        Vector2 liftPosition = player.getPosition().cpy().add(0, 0.5f);
        boxPhysics.getBody().setTransform(liftPosition, 0);
    }
}

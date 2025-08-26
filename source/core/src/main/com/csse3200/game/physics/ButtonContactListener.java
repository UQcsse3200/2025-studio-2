package com.csse3200.game.physics;

import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

/**
 * ContactListener for detecting collisions between the player entity and the button entity
 * Triggers the push event when contact is made
 */
public class ButtonContactListener implements ContactListener {

    /**
     * Called when two features begin to touch and checks if a player has collided with a button
     * If so, button keeps track that player is in range
     *
     * @param contact object representing the collision
     */
    @Override
    public void beginContact(Contact contact) {
        Entity a = getEntityFromFixture(contact.getFixtureA());
        Entity b = getEntityFromFixture(contact.getFixtureB());

        if (a == null || b == null) return;

        //set player in range
        setPlayerInRange(a, b, true);
        setPlayerInRange(b, a, true);
    }

    /**
     * Retrieves the entity associated with a fixture
     *
     * @param fixture physics fixture involved in the collision
     *
     * @return associated entity, null if none found
     */
    private Entity getEntityFromFixture(Fixture fixture) {
        if (fixture == null || fixture.getBody() == null || fixture.getBody().getUserData() == null) {
            return null;
        }
        BodyUserData data = (BodyUserData) fixture.getBody().getUserData();
        return data.entity;
    }

    /**
     * Sets whether player is in range of a button for an interaction.
     * This is triggered on collision begin and end
     *
     * @param button entity for button component
     * @param other entity for player component
     * @param inRange true if player in collision, false if they leave collision
     */
    private void setPlayerInRange(Entity button, Entity other, boolean inRange) {
        ButtonComponent buttonComponent = button.getComponent(ButtonComponent.class);
        PlayerActions player = other.getComponent(PlayerActions.class);

        if(buttonComponent != null && player != null) {
            ColliderComponent collider = inRange ? other.getComponent(ColliderComponent.class) : null;
            buttonComponent.setPlayerInRange(collider);
        }
    }

    /**
     * Called when two features end contact
     * If player moves away from button (i.e. no longer colliding), button stops tracking the player
     *  as being in range for an interaction
     *
     * @param contact object representing the collision
     */
    @Override
    public void endContact(Contact contact) {
        Entity a = getEntityFromFixture(contact.getFixtureA());
        Entity b = getEntityFromFixture(contact.getFixtureB());

        if (a == null || b == null) return;

        //check both direction of a collision
        setPlayerInRange(a, b, false);
        setPlayerInRange(b, a, false);

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}

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
     *
     * @param contact object representing the collision
     */
    @Override
    public void beginContact(Contact contact) {
        Entity a = getEntityFromFixture(contact.getFixtureA());
        Entity b = getEntityFromFixture(contact.getFixtureB());

        if (a == null || b == null) return;

        //check both direction of a collision
        triggerPushIfPlayerHitsButton(a, b);
        triggerPushIfPlayerHitsButton(b, a);
    }

    /**
     * Triggers the push event if a player is the other entity in the collision
     *
     * @param button button entity
     * @param other player entity
     */
    private void triggerPushIfPlayerHitsButton(Entity button, Entity other) {
        if (button.getComponent(ButtonComponent.class) != null &&
                other.getComponent(PlayerActions.class) != null) {
            button.getEvents().trigger("push", other.getComponent(ColliderComponent.class));
        }
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

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}

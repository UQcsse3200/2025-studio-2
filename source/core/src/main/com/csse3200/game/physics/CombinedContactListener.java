package com.csse3200.game.physics;

import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

/**
 * Combined contact listener that handles both general collision events (for tooltips, collectables, etc.)
 * and specific button collision logic.
 * 
 * This listener:
 * 1. Triggers general "collisionStart" and "collisionEnd" events on all entities (for tooltip system)
 * 2. Handles button-specific collision logic (for button interactions)
 */
public class CombinedContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        // First, trigger general collision events (for tooltips, collectables, etc.)
        triggerEventOn(contact.getFixtureA(), "collisionStart", contact.getFixtureB());
        triggerEventOn(contact.getFixtureB(), "collisionStart", contact.getFixtureA());
        
        // Then handle button-specific logic
        handleButtonCollision(contact, true);
    }

    @Override
    public void endContact(Contact contact) {
        // First, trigger general collision events (for tooltips, collectables, etc.)
        triggerEventOn(contact.getFixtureA(), "collisionEnd", contact.getFixtureB());
        triggerEventOn(contact.getFixtureB(), "collisionEnd", contact.getFixtureA());
        
        // Then handle button-specific logic
        handleButtonCollision(contact, false);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Nothing to do before resolving contact
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // Nothing to do after resolving contact
    }

    /**
     * Triggers events on entities involved in collisions.
     * This is copied from PhysicsContactListener to ensure compatibility.
     */
    private void triggerEventOn(Fixture fixture, String evt, Fixture otherFixture) {
        BodyUserData userData = (BodyUserData) fixture.getBody().getUserData();
        if (userData != null && userData.entity != null) {
            userData.entity.getEvents().trigger(evt, fixture, otherFixture);
        }
    }

    /**
     * Handles button-specific collision logic.
     * This is copied from ButtonContactListener to ensure compatibility.
     */
    private void handleButtonCollision(Contact contact, boolean inRange) {
        Entity a = getEntityFromFixture(contact.getFixtureA());
        Entity b = getEntityFromFixture(contact.getFixtureB());

        if (a == null || b == null) return;

        // Set player in range for both possible button-player combinations
        setPlayerInRange(a, b, inRange);
        setPlayerInRange(b, a, inRange);
    }

    /**
     * Retrieves the entity associated with a fixture.
     * This is copied from ButtonContactListener to ensure compatibility.
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
     * This is copied from ButtonContactListener to ensure compatibility.
     */
    private void setPlayerInRange(Entity button, Entity other, boolean inRange) {
        ButtonComponent buttonComponent = button.getComponent(ButtonComponent.class);
        PlayerActions player = other.getComponent(PlayerActions.class);

        if (buttonComponent != null && player != null) {
            ColliderComponent collider = inRange ? other.getComponent(ColliderComponent.class) : null;
            buttonComponent.setPlayerInRange(collider);
        }
    }
}

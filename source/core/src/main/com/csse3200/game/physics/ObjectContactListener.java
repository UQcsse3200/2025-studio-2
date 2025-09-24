package com.csse3200.game.physics;

import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.*;
import com.csse3200.game.components.obstacles.MoveableBoxComponent;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.PressurePlateComponent; // <— import added
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

/**
 * Combined contact listener that handles both general collision events (for tooltips, collectables, etc.)
 * and specific object collision logic.
 *
 * This listener:
 * 1. Triggers general "collisionStart" and "collisionEnd" events on all entities (for tooltip system)
 * 2. Handles object-specific collision logic (for button interactions)
 *
 * ContactListener for detecting collisions between the player entity and specified objects in the
 * game such as buttons, boxes etc.
 * Triggers the push event when contact is made
 */
public class ObjectContactListener implements ContactListener {

    /**
     * Called when two features begin to touch and checks if a player has collided with an object
     * If so, the game object keeps track that player is in range
     * Also triggers general collision events for tooltips and other systems
     *
     * @param contact object representing the collision
     */
    @Override
    public void beginContact(Contact contact) {
        // First, trigger general collision events (for tooltips, collectables, etc.)
        triggerEventOn(contact.getFixtureA(), "collisionStart", contact.getFixtureB());
        triggerEventOn(contact.getFixtureB(), "collisionStart", contact.getFixtureA());

        Entity a = getEntityFromFixture(contact.getFixtureA());
        Entity b = getEntityFromFixture(contact.getFixtureB());

        if (a == null || b == null) return;

        a.getEvents().trigger("onCollisionStart", b);
        b.getEvents().trigger("onCollisionStart", a);

        // Set player in range
        setPlayerInRangeOfButton(a, b, true);
        setPlayerInRangeOfButton(b, a, true);

        setPlayerInRangeOfBox(a, b, true);
        setPlayerInRangeOfBox(b, a, true);

        setPlayerOnPressurePlate(a, b, true);
        setPlayerOnPressurePlate(b, a, true);

        setPlayerInRangeOfTrap(a, b);
        setPlayerInRangeOfTrap(b, a);

        setPlayerInRangeOfDeathZone(a, b);
        setPlayerInRangeOfDeathZone(b, a);
    }

    /**
     * Retrieves the entity associated with a fixture
     *
     * @param fixture physics fixture involved in the collision
     *
     * @return associated entity, null if none found
     */
    private Entity getEntityFromFixture(Fixture fixture) {
        if (fixture == null
                || fixture.getBody() == null
                || fixture.getBody().getUserData() == null) {
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
    private void setPlayerInRangeOfButton(Entity button, Entity other, boolean inRange) {
        ButtonComponent buttonComponent = button.getComponent(ButtonComponent.class);
        PlayerActions player = other.getComponent(PlayerActions.class);

        if(buttonComponent != null && player != null) {
            ColliderComponent collider = inRange
                    ? other.getComponent(ColliderComponent.class)
                    : null;
            buttonComponent.setPlayerInRange(collider);
        }
    }

    /**
     * Sets whether player is in range of a box for an interaction.
     * This is triggered on collision begin and end
     *
     * @param box entity for box component
     * @param other entity for player component
     * @param inRange true if player in collision, false if they leave collision
     */
    private void setPlayerInRangeOfBox(Entity box, Entity other, boolean inRange) {
        MoveableBoxComponent moveableBoxComponent = box.getComponent(MoveableBoxComponent.class);
        PlayerActions player = other.getComponent(PlayerActions.class);

        if (moveableBoxComponent != null && player != null) {
            ColliderComponent collider = inRange
                    ? other.getComponent(ColliderComponent.class)
                    : null;
            moveableBoxComponent.setPlayerInRange(collider);
        }
    }


     /*
     * Triggers events on entities involved in collisions.
     * This is used for tooltip system and other general collision events.
     */
    private void triggerEventOn(Fixture fixture, String evt, Fixture otherFixture) {
        BodyUserData userData = (BodyUserData) fixture.getBody().getUserData();
        if (userData != null && userData.entity != null) {
            userData.entity.getEvents().trigger(evt, fixture, otherFixture);
        }
    }

    /**
     * Called when two features end contact
     * If player moves away from an object (i.e. no longer colliding), object stops tracking the
     * player as being in range for an interaction
     *
     * @param contact object representing the collision
     */
    @Override
    public void endContact(Contact contact) {
        // First, trigger general collision events (for tooltips, collectables, etc.)
        triggerEventOn(contact.getFixtureA(), "collisionEnd", contact.getFixtureB());
        triggerEventOn(contact.getFixtureB(), "collisionEnd", contact.getFixtureA());

        Entity a = getEntityFromFixture(contact.getFixtureA());
        Entity b = getEntityFromFixture(contact.getFixtureB());

        if (a == null || b == null) return;

        // Check both direction of a collision
        setPlayerInRangeOfButton(a, b, false);
        setPlayerInRangeOfButton(b, a, false);

        setPlayerOnPressurePlate(a, b, false);
        setPlayerOnPressurePlate(b, a, false);

        setPlayerInRangeOfBox(a, b, false);
        setPlayerInRangeOfBox(b, a, false);
    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }

    /**
     * Check if the colliding entities consist of a trap and a player, and if so calls the
     * trap's damage function.
     * In future, this method may be extended to apply to enemies or the player.
     * @param colliding the colliding entity, expected to be a player.
     * @param trap the entity on which to call TrapComponent.damage
     */
    private void setPlayerInRangeOfTrap(Entity colliding, Entity trap) {
        PlayerActions player = colliding.getComponent(PlayerActions.class);
        TrapComponent trapComponent = trap.getComponent(TrapComponent.class);

        if (trapComponent != null && player != null) {
            ColliderComponent collider = colliding.getComponent(ColliderComponent.class);
            trapComponent.damage(collider);
        }
    }

    private void setPlayerInRangeOfDeathZone(Entity colliding, Entity deathZone) {
        PlayerActions player = colliding.getComponent(PlayerActions.class);
        DeathZoneComponent deathZoneComponent = deathZone.getComponent(DeathZoneComponent.class);

        if (deathZoneComponent != null && player != null) {
            ColliderComponent collider = colliding.getComponent(ColliderComponent.class);
            deathZoneComponent.damage(collider);
        }
    }

    private void setPlayerOnPressurePlate(Entity plate, Entity other, boolean inRange) {
        PressurePlateComponent plateComp = plate.getComponent(PressurePlateComponent.class);
        PlayerActions player = other.getComponent(PlayerActions.class);
        if (plateComp != null && player != null) {
            ColliderComponent collider = inRange
                    ? other.getComponent(ColliderComponent.class)
                    : null;
            plateComp.setPlayerOnPlate(collider);
        }
    }
}

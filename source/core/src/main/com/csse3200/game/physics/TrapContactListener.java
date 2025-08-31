package com.csse3200.game.physics;

import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

public class TrapContactListener implements ContactListener {

    /**
     * Called when two entities begin to touch and checks if they are a Player and Trap.
     * In future, this method may be extended to apply to enemies or the player.
     * If so, button calls its damage function on the colliding entity.
     * @param contact object representing the collision.
     */
    @Override
    public void beginContact(Contact contact) {
        Entity a = getEntityFromFixture(contact.getFixtureA());
        Entity b = getEntityFromFixture(contact.getFixtureB());

        if (a == null || b == null) return;

        // Check both directions for a collision
        checkCollision(a, b);
        checkCollision(b, a);
    }

    /**
     * Check if the colliding entities consist of a trap and a player.
     * In future, this method may be extended to apply to enemies or the player.
     * @param colliding the colliding entity, expected to be a player.
     * @param trap the entity on which to call TrapComponent.damage
     */
    private void checkCollision(Entity colliding, Entity trap) {
        PlayerActions player = colliding.getComponent(PlayerActions.class);
        TrapComponent trapComponent = trap.getComponent(TrapComponent.class);

        if(trapComponent != null && player != null) {
            ColliderComponent collider = colliding.getComponent(ColliderComponent.class);
            trapComponent.damage(collider);
        }
    }


    /**
     * Retrieves the entity associated with a fixture. Method identical to ButtonContactListener.getEntityFromFixture
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

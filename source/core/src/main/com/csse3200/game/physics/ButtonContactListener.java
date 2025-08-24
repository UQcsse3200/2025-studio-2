package com.csse3200.game.physics;

import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

public class ButtonContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Entity a = getEntityFromFixture(contact.getFixtureA());
        Entity b = getEntityFromFixture(contact.getFixtureB());

        if (a == null || b == null) return;

        triggerPushIfPlayerHitsButton(a, b);
        triggerPushIfPlayerHitsButton(b, a);
    }

    private void triggerPushIfPlayerHitsButton(Entity button, Entity other) {
        if (button.getComponent(ButtonComponent.class) != null &&
                other.getComponent(PlayerActions.class) != null) {
            button.getEvents().trigger("push", other.getComponent(ColliderComponent.class));
        }
    }

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

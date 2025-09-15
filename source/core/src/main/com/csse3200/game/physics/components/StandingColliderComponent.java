package com.csse3200.game.physics.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class StandingColliderComponent extends ColliderComponent {
    private Fixture fixture;
    private static Vector2 OFFSET = new Vector2(0.9f, 0.5f);

    @Override
    public void create() {
        Body body = entity.getComponent(PhysicsComponent.class).getBody();

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 0.5f, OFFSET, 0f);

        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.density = 1.5f;
        def.isSensor = false; // active by default

        fixture = body.createFixture(def);
        fixture.setUserData("standing");

        shape.dispose();
    }

    public Fixture getFixtureRef() {
        return fixture;
    }
}

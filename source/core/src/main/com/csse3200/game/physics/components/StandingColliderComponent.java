package com.csse3200.game.physics.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.physics.PhysicsLayer;

public class StandingColliderComponent extends ColliderComponent {
    private Fixture fixture;
    private static final Vector2 OFFSET = new Vector2(0.9f, 0.5f);
    private static final float BOX_WIDTH = 0.3f;
    private static final float BOX_HEIGHT = 0.45f;

    @Override
    public void create() {
        Body body = entity.getComponent(PhysicsComponent.class).getBody();

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(BOX_WIDTH, BOX_HEIGHT, OFFSET, 0f);

        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.density = 1.5f;
        def.isSensor = false; // active by default
        def.filter.categoryBits = PhysicsLayer.PLAYER;

        fixture = body.createFixture(def);
        fixture.setUserData("standing");

        shape.dispose();
    }

    /**
     * Allows the player's collision fixture to be retrieved
     */
    public Fixture getFixtureRef() {
        return fixture;
    }
}

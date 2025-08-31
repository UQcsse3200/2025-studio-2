package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.physics.components.PhysicsComponent;

public class AutonomousBoxComponent extends Component {

    private PhysicsComponent physics;
    private float leftX;
    private float rightX;
    private int direction = 1;
    private float speed = 2f;

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
    }

    public PhysicsComponent getPhysics() {
        return physics;
    }

    @Override
    public void update() {
        if (physics == null) return;

        float x = physics.getBody().getPosition().x;
        float y = physics.getBody().getPosition().y;

        // Move horizontally
        x += direction * speed * Gdx.graphics.getDeltaTime();

        // Reverse direction
        if (x >= rightX) {
            direction = -1;
        }
        if (x <= leftX) {
            direction = 1;
        }

        physics.getBody().setTransform(x,y,0);
    }

    public void setBounds(float leftX, float rightX) {
        this.leftX = leftX;
        this.rightX = rightX;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}

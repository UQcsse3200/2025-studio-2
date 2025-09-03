package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.physics.components.PhysicsComponent;

/**
 * Component for a box that moves autonomously along a specified path at a specified speed.
 */
public class AutonomousBoxComponent extends Component {

    private PhysicsComponent physics;
    private float leftX;
    private float rightX;
    private int direction = 1;
    private float speed = 2f;

    /**
     * Default constructor to remove gradle warnings about missing constructor in this class.
     */
    public AutonomousBoxComponent() {
        // Intentionally blank
    }

    /**
     * Initialises the box's physics reference
     */
    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
    }

    /**
     * Retrieves the PhysicsComponent after creation
     *
     * @return  the physics component
     */
    public PhysicsComponent getPhysics() {
        return physics;
    }

    /**
     * Updates the box position every frame.
     * Moves horizontally and reverses direction at the boundaries
     */
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

    /**
     * Sets the horizontal movement bounds for the box.
     *
     * @param leftX  the left boundary
     * @param rightX  the right boundary
     */
    public void setBounds(float leftX, float rightX) {
        this.leftX = leftX;
        this.rightX = rightX;
    }

    /**
     * Sets the movement speed
     *
     * @param speed the speed in units per second
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Retrieves the left boundary of the autonomous box's horizontal path
     *
     * @return the left X coordinate
     */
    public float getLeftX() {
        return leftX;
    }

    /**
     * Retrieves the right boundary of the autonomous box's horizontal path
     *
     * @return the right X coordinate
     */
    public float getRightX() {
        return rightX;
    }

    /**
     * Gets the movement speed of the autonomous box
     *
     * @return the box's speed
     */
    public float getSpeed() {
        return speed;
    }
}

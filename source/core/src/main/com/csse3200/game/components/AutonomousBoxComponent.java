package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * Component for a box that moves autonomously along a specified path at a specified speed.
 */
public class AutonomousBoxComponent extends Component {

    private PhysicsComponent physics;

    private float minMoveX;
    private float maxMoveX;
    private float minMoveY;
    private float maxMoveY;
    private int directionX = 1;
    private int directionY = 1;
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
     * Updates the box's position every frame based on its movement bounds and speed.
     * <p>
     * This box moves horizontally and/or vertically.  When it reaches a boundary, the
     * direction is reversed. <br>
     * This method is called automatically once per frame.
     */
    @Override
    public void update() {
        if (physics == null) return;

        float deltaTime = Gdx.graphics.getDeltaTime();
        float x = physics.getBody().getPosition().x;
        float y = physics.getBody().getPosition().y;

        x = updateHorizontalPosition(x, deltaTime);
        y = updateVerticalPosition(y, deltaTime);

        physics.getBody().setTransform(x, y, 0);

        // Flips image left when moving horizontally right to left
        AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator != null) {
            animator.setFlipX(directionX < 0);
        }
    }

    /**
     * Calculates the new horizontal position of the box for this frame.
     * <p>
     * If the box reaches the minimum or maximum X bounds, the horizontal direction is reversed.
     *
     * @param x the current horizontal position of the box
     * @param deltaTime the time elapsed since the last frame
     * @return the updated horizontal position
     */
    private float updateHorizontalPosition(float x, float deltaTime) {
        if (minMoveX == maxMoveX){
            return x;
        }

        x += directionX * speed * deltaTime;

        if (x >= maxMoveX){
            directionX = -1;
        } else if (x <= minMoveX){
            directionX = 1;
        }

        return x;
    }

    /**
     * Calculates the new vertical position of the box for this frame.
     * <p>
     * If the box reaches the minimum or maximum Y bounds, the vertical direction is reversed.
     *
     * @param y the current vertical position of the box
     * @param deltaTime  the time elapsed since the last frame
     * @return  the updated vertical position
     */
    private float updateVerticalPosition(float y, float deltaTime) {
        if (minMoveY == maxMoveY){
            return y;
        }

        y += directionY * speed * deltaTime;

        if (y >= maxMoveY){
            y = maxMoveY;
            directionY = -1;
        } else if (y <= minMoveY){
            y = minMoveY;
            directionY = 1;
        }

        return y;
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
     * Sets the horizontal movement bounds for the box.
     *
     * @param minMoveX  the left boundary
     * @param maxMoveX  the right boundary
     */
    public void setBounds(float minMoveX, float maxMoveX, float minMoveY, float maxMoveY) {
        this.minMoveX = minMoveX;
        this.maxMoveX = maxMoveX;
        this.minMoveY = minMoveY;
        this.maxMoveY = maxMoveY;
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
        return minMoveX;
    }

    /**
     * Retrieves the right boundary of the autonomous box's horizontal path
     *
     * @return the right X coordinate
     */
    public float getRightX() {
        return maxMoveX;
    }

    /**
     * Retrieves the top boundary of the autonomous box's vertical path
     *
     * @return  the top Y coordinate
     */
    public float getTopY() {
        return maxMoveY;
    }

    /**
     * Retrieves the bottom boundary of the autonomous box's vertical path
     *
     * @return the bottom Y coordinate
     */
    public float getBottomY() {
        return minMoveY;
    }

    /**
     * Retrieves the current horizontal movement direction of the box
     * A value of 1 indicates movement to the right, and -1 indicates movement to the left
     *
     * @return the current horizontal direction
     */
    public int getDirectionX() {
        return directionX;
    }

    /**
     * Retrieves the current vertical movement direction of the box
     * A value of 1 indicates upward movement, and -1 indicates downward movement
     *
     * @return the current vertical direction
     */
    public int getDirectionY() {
        return directionY;
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

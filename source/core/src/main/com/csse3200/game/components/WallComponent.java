package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * WallComponent represents a static, vertical wall in the game world.
 * Walls:
 * - Block horizontal movement
 * - Are not considered "ground" (player cannot stand on them)
 * - Can have custom textures and scaling
 * - Are future-proofed for wall sliding/jumping mechanics
 */
public class WallComponent extends Component {

    private float width;
    private float height;
    private String texturePath;
    private boolean slippery; // For future wall sliding mechanics

    /**
     * Creates a WallComponent with specified dimensions and texture.
     *
     * @param width       Width of the wall in world units
     * @param height      Height of the wall in world units
     * @param texturePath Path to the wall texture asset
     */
    public WallComponent(float width, float height, String texturePath) {
        this.width = width;
        this.height = height;
        this.texturePath = texturePath;
        this.slippery = false;
    }

    @Override
    public void create() {
        super.create();

        // Create the collider for the wall
        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        if (collider == null) {
            collider = new ColliderComponent();
            entity.addComponent(collider);
        }

        // Set collider shape as a vertical rectangle
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f, height / 2f, new Vector2(0, 0), 0f);

        Fixture fixture = physicsComponent.getBody().createFixture(shape, 0f);
        fixture.setFriction(slippery ? 0.1f : 0.8f); // Low friction if slippery
        fixture.setUserData("wall");

        // Set collision filtering so it's not treated as ground
        collider.setLayer(PhysicsLayer.OBSTACLE);

        shape.dispose();

        // Add texture rendering
        TextureRenderComponent texture = entity.getComponent(TextureRenderComponent.class);
        if (texture == null) {
            texture = new TextureRenderComponent(texturePath);
            entity.addComponent(texture);
        }
        texture.setScale(new Vector2(width, height));
    }

    /**
     * Enables or disables slippery behavior (for wall sliding).
     *
     * @param slippery true if wall should have low friction
     */
    public void setSlippery(boolean slippery) {
        this.slippery = slippery;
    }

    /**
     * Returns whether the wall is slippery.
     */
    public boolean isSlippery() {
        return slippery;
    }

    /**
     * Factory method to create a wall entity.
     *
     * @param x           World X position (center)
     * @param y           World Y position (center)
     * @param width       Width in world units
     * @param height      Height in world units
     * @param texturePath Path to texture asset
     * @return Configured wall entity
     */
    public static Entity createWallEntity(float x, float y, float width, float height, String texturePath) {
        Entity wall = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new WallComponent(width, height, texturePath));

        wall.setPosition(x, y);
        return wall;
    }
}

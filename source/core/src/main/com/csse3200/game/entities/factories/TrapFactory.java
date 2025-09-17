package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory to create trap entities (objects that damage the player (and possibly enemies)).
 *
 * <p>Each trap entity type should have a creation method that returns a corresponding entity.
 */
public class TrapFactory {


    /**
     * Creates a basic Spike trap with configurable rotation and length of 1 unit.
     * <p>
     * The Spike trap displays in 16-pixel high x 16-pixel wide units of sprite spikes_sprite.png.
     * It is immovable and cannot be destroyed. On collision with player (or enemy), it damages the colliding
     * entity by 1, and resets their position to a safe location nearby.
     * Spikes are NOT classified as an obstacle the player can hide behind, because they are very short
     * In a future update, the width will configurable based on a provided length parameter.
     *
     * @param safeSpot Spot to teleport the player once they take damage
     * @param rotation The rotation (anti-clockwise) in degrees. Must be in {0, 90, 180, 270}.
     * @return the Spike trap Entity created.
     */
    public static Entity createSpikes(Vector2 safeSpot, float rotation) {
        Entity spikes = new Entity();

        // Set up texture
        String texture = "images/spikes_sprite.png";
        TextureRenderComponent textureComponent = new TextureRenderComponent(texture);
        textureComponent.setRotation(rotation);
        textureComponent.setLayer(0);
        spikes.addComponent(textureComponent);

        // Add physics component
        spikes.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));

        // Set up damage collider
        int direction = (int) (rotation/90);
        ColliderComponent collider = new ColliderComponent();
        spikes.addComponent(collider);
        setupDamageCollider(collider, direction);

        TrapComponent trapComponent = new TrapComponent(safeSpot, direction);
        spikes.addComponent(trapComponent);

        return spikes;
    }

    private static void setupDamageCollider(ColliderComponent collider, int direction) {
        Vector2 center = collider.getEntity().getScale();
        center.x *= 0.8f;
        PhysicsComponent.AlignX alignX = PhysicsComponent.AlignX.CENTER;
        PhysicsComponent.AlignY alignY = PhysicsComponent.AlignY.CENTER;

        switch (direction) {
            case 0: // Facing up
                center.x *= 0.7f;
                center.y *= 0.5f;
                alignY = PhysicsComponent.AlignY.BOTTOM;
                break;
            case 1: // Facing left
                center.x *= 0.5f;
                alignX = PhysicsComponent.AlignX.RIGHT;
                break;
            case 3: // Facing right
                center.x *= 0.5f;
                alignX = PhysicsComponent.AlignX.LEFT;
                break;
            default: // Facing down
                center.y *= 0.5f;
                alignY = PhysicsComponent.AlignY.TOP;
                break;
        }

        collider.setAsBoxAligned(center, alignX, alignY);
    }
}

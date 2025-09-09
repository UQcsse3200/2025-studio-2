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
     *
     * In a future update, the width will configurable based on a provided length parameter,
     * and the trap will be able to be rotated to face up (default), right, down, or left, as a visual
     * effect so it can be used on a variety of platforms and/or walls.
     *
     * @param safeSpot Spot to teleport the player once they take damage
     * @param rotation The rotation (anti-clockwise) in degrees. Must be in {0, 90, 180, 360}. MUST be >= 0 <= 360.
     * @return the Spike trap Entity created.
     */
    public static Entity createSpikes(Vector2 safeSpot, float rotation) {
        return createSpikes(safeSpot, rotation, 1f);
    }

    /**
     * Creates a basic Spike trap with configurable length and rotation.
     * <p>
     * The Spike trap displays in 16-pixel high x 16-pixel wide units of sprite spikes_sprite.png.
     * It is immovable and cannot be destroyed. On collision with player (or enemy), it damages the colliding
     * entity by 1, and resets their position to a safe location nearby.
     * Spikes are NOT classified as an obstacle the player can hide behind, because they are very short
     *
     * In a future update, the width will configurable based on a provided length parameter,
     * and the trap will be able to be rotated to face up (default), right, down, or left, as a visual
     * effect so it can be used on a variety of platforms and/or walls.
     *
     * @param safeSpot Spot to teleport the player once they take damage
     * @param rotation The rotation (anti-clockwise) in degrees. Must be in {0, 90, 180, 360}. MUST be >= 0 <= 360.
     * @param length The length the trap should be.
     * @return the Spike trap Entity created.
     */
    public static Entity createSpikes(Vector2 safeSpot, float rotation, float length)  {
        Entity spikes = new Entity();
        spikes.scaleWidth(length);
        String texture = "images/spikes_sprite.png";
        TextureRenderComponent textureComponent = new TextureRenderComponent(texture);
        textureComponent.setRotation(rotation);
        spikes.addComponent(textureComponent);

        // Add physics and collider components
        spikes.addComponent(new PhysicsComponent()
                        .setBodyType(BodyDef.BodyType.StaticBody));
        ColliderComponent collider = new ColliderComponent();
        spikes.addComponent(collider);

        // Fix collider to appropriate size
        Vector2 center = collider.getEntity().getScale().scl(0.25f);
        center.x *= 0.7f;
        collider.setAsBoxAligned(center, PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.BOTTOM);

        TrapComponent trapComponent = new TrapComponent(safeSpot, (int) (rotation/90));
        spikes.addComponent(trapComponent);

        return spikes;
    }
}

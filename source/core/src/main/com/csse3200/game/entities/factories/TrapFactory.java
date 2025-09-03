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
     * @param position The start position of the trap
     * @param safeSpot Spot to teleport the player once they take damage
     * @return the Spike trap Entity created.
     */
    public static Entity createSpikes(GridPoint2 position, GridPoint2 safeSpot) {//int length, int rotateClockwise) {
        Entity spikes = new Entity();
        String texture = "images/spikes_sprite.png";
        spikes.addComponent(new TextureRenderComponent(texture));

        // Add physics and collider components
        spikes.addComponent(new PhysicsComponent()
                        .setBodyType(BodyDef.BodyType.StaticBody));
        ColliderComponent collider = new ColliderComponent();
        spikes.addComponent(collider);


        // Fix collider to appropriate size
        Vector2 center = collider.getEntity().getScale().scl(0.5f);
        collider.setAsBoxAligned(center, PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.BOTTOM);

        // Add trap component - commented out as position-reset functionality is currently not working.
//        position.x -= 1;
//        Vector2 safePosition = terrain.tileToWorldPosition(position);
//        float tileSize = terrain.getTileSize();
//        safePosition.x += (int) ((tileSize / 2) - collider.getEntity().getCenterPosition().x);
//        safePosition.y += (int) ((tileSize / 2) - collider.getEntity().getCenterPosition().y);

        TrapComponent trapComponent = new TrapComponent();
        spikes.addComponent(trapComponent);

        return spikes;
    }
}

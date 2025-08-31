package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
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
     * The Spike trap displays in 16-pixel high x 16-pixel wide units of sprite spikes_sprite.png,
//     * and the width is configurable based on the provided length parameter.
//     * It can be rotated to face up (default), right, down, or left, as a visual effect so the trap
//     * can be used on a variety of platforms and/or walls.
     * It is immovable and cannot be destroyed. On collision with player (or enemy), it damages the colliding
     * entity by 1, and resets their position to a safe location nearby.
     * Spikes are NOT classified as an obstacle the player can hide behind, because they are very short
     *
//     * @param length The number of units wide the trap should be. (eventually. doesn't work yet.)
//     * @param rotateClockwise An integer in the range [0, 3] representing the number of 90 degree rotations
//     *                       clockwise for the trap, as follows:
//     *                        0 = Upward-facing spikes
//     *                        1 = Right-facing spikes
//     *                        2 = Downward-facing spikes
//     *                        3 = Left-facing spikes
//     *                        ^^ Eventually. doesn't work yet.
     * @param terrain The terrain in which the trap is being created
     * @param position The start position of the trap
     * @return the Spike trap Entity created.
     */
    public static Entity createSpikes(TerrainComponent terrain, GridPoint2 position) {//int length, int rotateClockwise) {
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

        // Add trap component
        position.x -= 1;
        Vector2 safePosition = terrain.tileToWorldPosition(position);
        float tileSize = terrain.getTileSize();
        safePosition.x += (int) ((tileSize / 2) - collider.getEntity().getCenterPosition().x);
        safePosition.y += (int) ((tileSize / 2) - collider.getEntity().getCenterPosition().y);

        TrapComponent trapComponent = new TrapComponent(safePosition);
        spikes.addComponent(trapComponent);

        return spikes;
    }
}

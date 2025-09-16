package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.DeathZoneComponent;
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
public class DeathZoneFactory {

    /**
     * Creates a basic death zone with configurable length.
     * <p>
     * It is immovable and cannot be destroyed. On collision with player (or enemy), it fully damages the colliding
     * entity, effectively killing them and resetting their position.
     *
     * @param position The start position of the death zone
     * @param resetPos Spot to teleport the player once they take damage
     * @return the Spike trap Entity created.
     */
    public static Entity createDeathZone(GridPoint2 position, Vector2 resetPos) {//int length, int rotateClockwise) {
        Entity deathZone = new Entity();
        String texture = "images/gate.png";
        //deathZone.addComponent(new TextureRenderComponent(texture)); <-- Uncomment to make them temporarily visible

        // Add physics and collider components
        deathZone.addComponent(new PhysicsComponent()
                        .setBodyType(BodyDef.BodyType.StaticBody));
        ColliderComponent collider = new ColliderComponent();
        deathZone.addComponent(collider);

        // Fix collider to appropriate size
        //deathZone.setScale(3.1f,1); <-- Uncomment for smaller death zones
        deathZone.setScale(90,0.5f);
        collider.setAsBoxAligned(deathZone.getScale().scl(0.8f),
                PhysicsComponent.AlignX.CENTER,
                PhysicsComponent.AlignY.BOTTOM);

        DeathZoneComponent deathZoneComponent = new DeathZoneComponent(resetPos);
        deathZone.addComponent(deathZoneComponent);

        return deathZone;
    }
}

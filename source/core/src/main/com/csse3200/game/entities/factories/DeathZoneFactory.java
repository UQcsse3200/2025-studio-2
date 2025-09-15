package com.csse3200.game.entities.factories;


import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.DeathZoneComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

/**
 * Factory to create DeathZone entities (objects that kill the player (and possibly enemies)).
 *
 */
public class DeathZoneFactory {

    /**
     * Creates a basic death zone with configurable length.
     * <p>
     * It is immovable and cannot be destroyed. On collision with player (or enemy), it fully damages the colliding
     * entity, effectively killing them and resetting their position.
     *
     * @return the DeathZone Entity created.
     */
    public static Entity createDeathZone() {
        Entity deathZone = new Entity();
        //String texture = "images/gate.png";                          <--
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

        DeathZoneComponent deathZoneComponent = new DeathZoneComponent();
        deathZone.addComponent(deathZoneComponent);

        return deathZone;
    }

    /**
     * Factory Initialisation
     */
    private DeathZoneFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}

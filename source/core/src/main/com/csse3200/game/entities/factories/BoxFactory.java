package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class BoxFactory {

    /**
     * Creates a static (immovable) box entity.
     * <p>
     * The box currently displays as a light grey square, scaled to half a game unit.
     * Its texture is currently a placeholder and can be replaced with a pixel image.
     * <p>
     * Its static body type makes it immovable.  Other physical game objects can collide with it,
     * but it does not move, react to collisions and cannot be destroyed.
     * @return A new static box Entity
     */
    public static Entity createStaticBox() {
        // Placeholder texture (light grey) rendered via Pixmap (8 bits each per RGBA)
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf("D3D3D3"));
        pixmap.fill();

        // Convert Pixmap box to Texture
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        Entity staticBox = new Entity()
                .addComponent(new TextureRenderComponent(texture))
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent());

        // Scaled to half a world unit
        staticBox.setScale(0.5f, 0.5f);
        return staticBox;
    }
}

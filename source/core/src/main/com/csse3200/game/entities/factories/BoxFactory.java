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

    public static Entity createStaticBox() {

        // Placeholder grey box rendered via Pixmap
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);  // 8 bits per RGBA inputs
        pixmap.setColor(Color.valueOf("D3D3D3"));  // light grey in hex, converted to LibGDX Color float
        pixmap.fill();

        // Convert Pixmap box to Texture
        Texture texture = new Texture(pixmap);  // Replace with new Texture("path/filename.png") for 2d pixel art
        pixmap.dispose();

        // Static physics component
        PhysicsComponent staticBody = new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody);

        Entity box = new Entity()
                .addComponent(new TextureRenderComponent(texture))  // swap out when implementing pixel art
                .addComponent(staticBody)
                .addComponent(new ColliderComponent());

        box.setScale(0.5f, 0.5f);
        return box;
    }
}

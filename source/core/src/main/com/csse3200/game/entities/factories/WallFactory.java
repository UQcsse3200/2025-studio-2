package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory for creating static vertical wall entities.
 * Walls:
 * - Are static (do not move)
 * - Block movement
 * - Are not considered "ground" (player shouldn't stand on them)
 * - Use distinct textures from platforms
 */
public class WallFactory {
    /**
     * Create a vertical wall at world position (x, y) with given size (width, height).
     * Texture is scaled to fit the specified size.
     *
     * @param x World X (bottom-left)
     * @param y World Y (bottom-left)
     * @param width Wall width in world units
     * @param height Wall height in world units
     * @param texturePath Asset path, e.g. "images/walls/stone_wall.png"
     * @return Configured wall entity
     */
    public static Entity createWall(float x, float y, float width, float height, String texturePath) {
        Entity wall =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/wall.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        wall.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        wall.getComponent(TextureRenderComponent.class).scaleEntity();

        wall.setPosition(x + width / 2f, y + height / 2f);
        return wall;
    }

    public static Entity createTiledWall(float x, float y, int tilesX, int tilesY, float tileWorldSize, String texturePath) {
        float width = tilesX * tileWorldSize;
        float height = tilesY * tileWorldSize;

        Entity tiledWall =
                new Entity()
                .addComponent(new TextureRenderComponent("images/tile.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        tiledWall.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        tiledWall.getComponent(TextureRenderComponent.class).scaleEntity();
        tiledWall.setPosition(x + width / 2f, y + height / 2f);
        return tiledWall;
    }
}

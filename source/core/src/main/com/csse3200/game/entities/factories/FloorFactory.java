package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TiledFloorComponent;
import com.csse3200.game.rendering.TiledGroundFloorComponent;

/**
 * Factory to create Floor entities using individual PNG textures.
 *
 * <p>Each Floor entity type should have a creation method that returns a corresponding entity.
 */
public class FloorFactory {
    // Load individual PNG files for each tile position
    private static final Texture tile000Texture = new Texture("images/cavelevel/tile000.png"); // Top-left corner
    private static final Texture tile001Texture = new Texture("images/cavelevel/tile001.png"); // Top edge
    private static final Texture tile002Texture = new Texture("images/cavelevel/tile002.png"); // Top-right corner
    private static final Texture tile014Texture = new Texture("images/cavelevel/tile014.png"); // Left edge
    private static final Texture tile015Texture = new Texture("images/cavelevel/tile015.png"); // Middle tile
    private static final Texture tile016Texture = new Texture("images/cavelevel/tile016.png"); // Right edge
    private static final Texture tile028Texture = new Texture("images/cavelevel/tile028.png"); // Bottom-left corner
    private static final Texture tile029Texture = new Texture("images/cavelevel/tile029.png"); // Bottom edge
    private static final Texture tile030Texture = new Texture("images/cavelevel/tile030.png"); // Bottom-right corner

    // Create TextureRegions from the loaded textures
    private static final TextureRegion topLeftCorner = new TextureRegion(tile000Texture);
    private static final TextureRegion topEdge = new TextureRegion(tile001Texture);
    private static final TextureRegion topRightCorner = new TextureRegion(tile002Texture);
    private static final TextureRegion leftEdge = new TextureRegion(tile014Texture);
    private static final TextureRegion middleTile = new TextureRegion(tile015Texture);
    private static final TextureRegion rightEdge = new TextureRegion(tile016Texture);
    private static final TextureRegion bottomLeftCorner = new TextureRegion(tile028Texture);
    private static final TextureRegion bottomEdge = new TextureRegion(tile029Texture);
    private static final TextureRegion bottomRightCorner = new TextureRegion(tile030Texture);

    private FloorFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    /**
     * Creates a static tiled floor entity.
     *
     * @return entity
     */
    public static Entity createStaticFloor() {
        Entity floor = new Entity()
            .addComponent(new TiledFloorComponent(
                topLeftCorner, topRightCorner, bottomLeftCorner, bottomRightCorner,
                topEdge, bottomEdge, leftEdge, rightEdge, middleTile))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        floor.addComponent(new MinimapComponent("images/floor-map-1.png"));
        floor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        return floor;
    }

    /**
     * Creates a ground floor that extends to the bottom of the screen.
     */
    public static Entity createGroundFloor() {
        Entity floor = new Entity()
            .addComponent(new TiledGroundFloorComponent(
                topLeftCorner, topRightCorner, topEdge, leftEdge, rightEdge, middleTile))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        floor.addComponent(new MinimapComponent("images/floor-map-1.png"));
        floor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        return floor;
    }

    /**
     * Creates a decorative floor without physics collision.
     *
     * @return entity
     */
    public static Entity createDecorativeFloor() {
        Entity floor = new Entity()
            .addComponent(new TiledFloorComponent(
                topLeftCorner, topRightCorner, bottomLeftCorner, bottomRightCorner,
                topEdge, bottomEdge, leftEdge, rightEdge, middleTile));
        floor.addComponent(new MinimapComponent("images/floor-map-1.png"));
        return floor;
    }
}
package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.ladders.AnchorLadderComponent;
import com.csse3200.game.components.ladders.LadderComponent;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
/**
 * Factory to create Platform entities.
 *
 * <p>Each Platform entity type should have a creation method that returns a corresponding entity.
 */
public class LadderFactory {

    private LadderFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    /**
     * Creates a static ladder entity.
     * @return entity
     */
    public static Entity createStaticLadder() {
        Entity ladder =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/ladder.png").setLayer(0))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE))
                        .addComponent(new LadderComponent());
        ladder.addComponent(new MinimapComponent("images/ladder-map.png"));
        ladder.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        ladder.getComponent(TextureRenderComponent.class).scaleEntity();

        return ladder;
    }

    /**
     * Creates a ladder base entity. Just a ladder entity with a different texture.
     * @return entity
     */
    public static Entity createLadderBase(String id, int height, int offset) {
        Entity ladder = new Entity();
        ladder.addComponent(new AnchorLadderComponent(id, height, offset));

        return ladder;
    }
}
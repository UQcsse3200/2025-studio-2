package com.csse3200.game.components;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Pressure plate that ONLY responds to moveable boxes.
 * Emits "plateToggled" (Boolean) when pressed/unpressed.
 */
public class BoxPressurePlateComponent extends Component {
    private String unpressedTex;
    private String pressedTex;

    private TextureRenderComponent renderer;
    private boolean pressed = false;
    private int overlappingBoxes = 0; // supports multiple boxes safely

    @Override
    public void create() {
        renderer = entity.getComponent(TextureRenderComponent.class);
        // Listen to physics contact events from ObjectContactListener
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
        updateTexture();
    }

    /** Set sprite textures for unpressed/pressed states. */
    public void setTextures(String unpressedPath, String pressedPath) {
        this.unpressedTex = unpressedPath;
        this.pressedTex = pressedPath;
        updateTexture();
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        Entity otherEntity = bodyToEntity(other);
        if (otherEntity == null) return;

        // Only count collisions from moveable boxes
        if (otherEntity.getComponent(MoveableBoxComponent.class) != null) {
            overlappingBoxes++;
            if (!pressed) {
                pressed = true;
                updateTexture();
                entity.getEvents().trigger("plateToggled", true);
            }
        }
    }

    private void onCollisionEnd(Fixture me, Fixture other) {
        Entity otherEntity = bodyToEntity(other);
        if (otherEntity == null) return;

        if (otherEntity.getComponent(MoveableBoxComponent.class) != null) {
            overlappingBoxes = Math.max(0, overlappingBoxes - 1);
            if (pressed && overlappingBoxes == 0) {
                pressed = false;
                updateTexture();
                entity.getEvents().trigger("plateToggled", false);
            }
        }
    }

    private Entity bodyToEntity(Fixture fixture) {
        Object ud = fixture.getBody().getUserData();
        return (ud instanceof Entity) ? (Entity) ud : null;
    }

    private void updateTexture() {
        if (renderer == null) return;
        if (pressed && pressedTex != null) {
            renderer.setTexture(pressedTex);
        } else if (!pressed && unpressedTex != null) {
            renderer.setTexture(unpressedTex);
        }
    }
}
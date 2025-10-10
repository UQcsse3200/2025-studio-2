package com.csse3200.game.components.collectables;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Abstract component for collectable items in the game world.
 * Handles collision detection with the player and invokes {@link #onCollect(Entity)}
 * when picked up. Subclasses define what happens on collection
 * (e.g., adding to inventory, increasing score).
 */

public abstract class CollectableComponent extends Component {
    private boolean collected = false;
    ConeLightComponent cone;
    ColliderComponent collider;
    TextureRenderComponent texture;
    AnimationRenderComponent animation;
    boolean isVisible = true;

    /**
     * Registers a listener for {@code "onCollisionStart"} events to trigger collection logic.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("onCollisionStart", this::onCollisionStart);
    }

    /**
     * Handles collision start events with other entities.
     * @param player the other entity in the collision, expected to be the Player
     */
    private void onCollisionStart(Entity player) {
        HitboxComponent cc = player.getComponent(HitboxComponent.class);
        if (cc == null || (cc.getLayer() != PhysicsLayer.PLAYER) || collected) return;

        collected = onCollect(player);
        if (collected) {
            TextureRenderComponent renderComponent = entity.getComponent(TextureRenderComponent.class);
            if (renderComponent != null) {
                RenderService renderService = ServiceLocator.getRenderService();
                renderService.unregister(renderComponent);
                if (entity.getComponent(MinimapComponent.class)!= null) {
                    Image marker = new Image(ServiceLocator.getResourceService().getAsset("images/minimap_forest_area.png", Texture.class));
                    entity.getComponent(MinimapComponent.class).setMarker(marker);
                }
            }
            entity.setEnabled(false);
        }
    }

    /**
     * Called when this collectable is picked up by the player.
     *
     * @param collector the player entity collecting this item
     * @return true if collection succeeded (and the item should be removed), false otherwise
     */
    protected abstract boolean onCollect(Entity collector);

    /**
     * Toggles the visibility state of this entity and updates its components accordingly.
     * <p>
     * When hidden:
     * <ul>
     *   <li>Animations are disabled.</li>
     *   <li>The texture is unregistered from the render service.</li>
     *   <li>Lights (cone) are deactivated.</li>
     *   <li>Collider layer is set to {@code PhysicsLayer.NONE}.</li>
     * </ul>
     * When visible:
     * <ul>
     *   <li>Animations are enabled.</li>
     *   <li>The texture is registered with the render service.</li>
     *   <li>Lights (cone) are activated.</li>
     *   <li>Collider layer is set to {@code PhysicsLayer.COLLECTABLE}.</li>
     * </ul>
     *
     * @param visible the visibility of the entity
     */
    public void toggleVisibility(boolean visible) {
        this.isVisible = visible;

        animation = entity.getComponent(AnimationRenderComponent.class);
        cone = entity.getComponent(ConeLightComponent.class);
        collider = entity.getComponent(ColliderComponent.class);
        texture = entity.getComponent(TextureRenderComponent.class);

        if (collected) return;
        if (animation != null) animation.setEnabled(visible);
        if (texture != null) texture.setEnabled(visible);
        if (cone != null) cone.setActive(visible);

        if (collider != null) {
            collider.setLayer(visible ? PhysicsLayer.COLLECTABLE : PhysicsLayer.NONE);
            collider.setSensor(visible);
        }
    }

    public boolean isVisible() {
        return this.isVisible;
    }
}

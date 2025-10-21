package com.csse3200.game.components.collectables;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component for collectable items.
 *
 * <p>
 * On collision with the player, this component delegates handling to the
 * player's {@link InventoryComponent}. The behaviour of the item is determined
 * by its {@link CollectablesConfig} and any registered effect handlers.
 * </p>
 */
public class CollectableComponent extends Component {
    private final String itemId;
    private boolean collected = false;

    ConeLightComponent cone;
    ColliderComponent collider;
    TextureRenderComponent texture;
    AnimationRenderComponent animation;


    public CollectableComponent(String itemId) {
        this.itemId = itemId;
    }

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

        collected = collect(player);
        if (collected) {
            // retrieve render service and visual components
            RenderService renderService = ServiceLocator.getRenderService();
            AnimationRenderComponent animation = entity.getComponent(AnimationRenderComponent.class);
            ConeLightComponent cone = entity.getComponent(ConeLightComponent.class);

            // remove atlas or static texture from game area
            if (animation != null) {
                renderService.unregister(animation);
                cone.dispose();
            } else {
                TextureRenderComponent texture = entity.getComponent(TextureRenderComponent.class);
                if (texture != null) {
                    renderService.unregister(texture);
                    if (entity.getComponent(MinimapComponent.class)!= null) {
                        Image marker = new Image(ServiceLocator.getResourceService().getAsset("images/minimap_forest_area.png", Texture.class));
                        entity.getComponent(MinimapComponent.class).setMarker(marker);
                    }
                    cone.dispose();
                }
            }
            entity.setEnabled(false);
        }

    }

    /**
     * Attempts to collect this for the player.
     *
     * <p>When called, a pickup sound is played and the item's identifier is
     * passed to the player's {@link InventoryComponent} for handling. The
     * inventory is responsible for deciding whether to store the item or
     * apply its effects.</p>
     *
     * @param player the player entity attempting to collect the item
     * @return {@code true} if the player had an {@link InventoryComponent} and
     *         the item was forwarded for handling; {@code false} otherwise
     */
    protected boolean collect(Entity player) {
        if (player == null) return false;
        playPickupSfx();

        var inventory = player.getComponent(InventoryComponent.class);
        if (inventory != null) {
            inventory.addItem(itemId);
            return true;
        }
        return false;
    }

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

    /**
     * Plays the pickup sound effect when a collectable is obtained.
     *
     * <p>This method assumes that the pickup sound asset has already been loaded into
     * the {@link ResourceService} during level or game initialization.</p>
     */
    private void playPickupSfx() {
        ResourceService rs = ServiceLocator.getResourceService();
        Sound pickupSound = rs.getAsset("sounds/pickupsound.mp3", Sound.class);
        pickupSound.play(UserSettings.get().masterVolume);
    }

}

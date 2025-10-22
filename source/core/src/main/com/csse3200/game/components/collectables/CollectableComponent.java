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
import java.util.HashMap;
import java.util.Map;

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

    private final Map<String, Map<String, String>> effectParams = new HashMap<>();

    ConeLightComponent cone;
    ColliderComponent collider;
    TextureRenderComponent texture;
    AnimationRenderComponent animation;

    /**
     * Creates a CollectableComponent for the given item.
     *
     * @param itemId the unique identifier of the collectable item this component represents
     */
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

        // remove atlas or static texture from game area

        if (collected) {
            RenderService renderService = ServiceLocator.getRenderService();
            toggleVisibility(false);
            if (animation != null) {
                renderService.unregister(animation);
            } else if (texture != null) {
                renderService.unregister(texture);
            } else if (cone != null) {
                cone.dispose();
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
     * apply its effects. If injected effect params are present,
     * they are passed to inventory.</p>
     *
     * @param player the player entity attempting to collect the item
     * @return {@code true} if the player had an {@link InventoryComponent} and
     *         the item was forwarded for handling; {@code false} otherwise
     */
    protected boolean collect(Entity player) {
        if (player == null) return false;
        playPickupSfx();

        var inventory = player.getComponent(InventoryComponent.class);
        if (inventory == null) return false;

        if (effectParams.isEmpty()) {
            inventory.addItem(itemId);
        } else {
            inventory.addItem(itemId, effectParams);
        }
        return true;
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

    /**
     * Sets or overrides a parameter for a given effect type on this instance.
     * <p>
     * If the effect type has no existing parameter map, a new one is created.
     * Passing a {@code null} effect type or key will result in no action.
     *
     * @param effectType the type of effect, used as the lookup key for the parameter map
     * @param key the parameter name to set or overwrite
     * @param value the parameter value to associate with the key
     */
    public void setEffectParam(String effectType, String key, String value) {
        if (effectType == null || key == null) return;
        effectParams.computeIfAbsent(effectType, k -> new HashMap<>()).put(key, value);
    }
}

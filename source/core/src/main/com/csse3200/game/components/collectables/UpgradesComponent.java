package com.csse3200.game.components.collectables;

import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class UpgradesComponent extends CollectableComponent {

    public final String upgradeId;
    private boolean collected = false;
    ConeLightComponent cone;
    ColliderComponent collider;
    TextureRenderComponent texture;
    AnimationRenderComponent animation;
    boolean isVisible = true;

    public UpgradesComponent(String upgradeId) {
        this.upgradeId = upgradeId;
    }

    protected boolean onCollect(Entity player) {
        if (player == null) return false;

        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        if (inventory != null) {
            if (!inventory.hasItem(InventoryComponent.Bag.UPGRADES, upgradeId)) {
                inventory.addItem(InventoryComponent.Bag.UPGRADES, upgradeId);
                inventory.removeItem(InventoryComponent.Bag.OBJECTIVES, upgradeId);
            }
            collected = true;
            return true;
        }

        return false;
    }

    public String getUpgradeId() {return upgradeId;}


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

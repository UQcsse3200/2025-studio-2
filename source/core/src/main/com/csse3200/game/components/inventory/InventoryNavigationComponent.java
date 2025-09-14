package com.csse3200.game.components.inventory;

import com.badlogic.gdx.Input;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.ui.inventoryscreen.InventoryTab;

/**
 * Input component for handling keyboard navigation within the inventory screen.
 * Handles arrow key input to move selection around the inventory grid.
 */
public class InventoryNavigationComponent extends InputComponent {
    private InventoryTab inventoryTab;

    public InventoryNavigationComponent(InventoryTab inventoryTab) {
        this.inventoryTab = inventoryTab;
    }

    /**
     * Triggers player events on specific keycodes.
     *
     * @param keycode key pressed
     * @return whether the input was processed
     * @see InputComponent#keyDown(int)
     */
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.UP:
                inventoryTab.moveSelectionUp();
                entity.getEvents().trigger("refreshInventoryGrid");
                return true;
            case Input.Keys.DOWN:
                inventoryTab.moveSelectionDown();
                entity.getEvents().trigger("refreshInventoryGrid");
                return true;
            case Input.Keys.LEFT:
                inventoryTab.moveSelectionLeft();
                entity.getEvents().trigger("refreshInventoryGrid");
                return true;
            case Input.Keys.RIGHT:
                inventoryTab.moveSelectionRight();
                entity.getEvents().trigger("refreshInventoryGrid");
                return true;
            case Input.Keys.ENTER:
            case Input.Keys.SPACE:
                // Future: Handle item selection/use
                entity.getEvents().trigger("selectInventoryItem", inventoryTab.getSelectedSlotIndex());
                return true;
            default:
                return false;
        }
    }

    /**
     * Sets the inventory tab that this component controls
     */
    public void setInventoryTab(InventoryTab inventoryTab) {
        this.inventoryTab = inventoryTab;
    }
}

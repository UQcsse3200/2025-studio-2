package com.csse3200.game.components.inventory;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.ui.inventoryscreen.InventoryTab;

/**
 * Input component for handling keyboard navigation within the inventory screen.
 * Handles arrow key input to move selection around the inventory grid and manages
 * all selection state and tooltip display.
 */
public class InventoryNavigationComponent extends InputComponent {
    private InventoryTab inventoryTab;
    
    // Selection state
    private int selectedRow = 0;
    private int selectedCol = 0;
    private boolean navigationEnabled = false;
    
    private static final int GRID_ROWS = 4;
    private static final int GRID_COLS = 4;

    public InventoryNavigationComponent(InventoryTab inventoryTab) {
        this.inventoryTab = inventoryTab;
    }

    /**
     * Enables navigation and shows initial tooltip
     */
    public void enableNavigation() {
        this.navigationEnabled = true;
        selectedRow = 0;
        selectedCol = 0;
        // Refresh the grid to show the initial selection highlight
        entity.getEvents().trigger("refreshInventoryGrid");
        showTooltipForSelectedSlot();
    }

    /**
     * Disables navigation and hides tooltip
     */
    public void disableNavigation() {
        this.navigationEnabled = false;
        TooltipSystem.TooltipManager.hideTooltip();
    }

    /**
     * Gets the currently selected slot index (0-based, row-major order)
     */
    public int getSelectedSlotIndex() {
        return selectedRow * GRID_COLS + selectedCol;
    }

    /**
     * Gets the current selection coordinates for the UI to highlight
     */
    public int getSelectedRow() { return selectedRow; }
    public int getSelectedCol() { return selectedCol; }
    public boolean isNavigationEnabled() { return navigationEnabled; }

    /**
     * Triggers player events on specific keycodes.
     *
     * @param keycode key pressed
     * @return whether the input was processed
     * @see InputComponent#keyDown(int)
     */
    @Override
    public boolean keyDown(int keycode) {
        if (!navigationEnabled) {
            return false;
        }
        
        switch (keycode) {
            case Input.Keys.UP:
                if (selectedRow > 0) {
                    selectedRow--;
                    entity.getEvents().trigger("refreshInventoryGrid");
                    showTooltipForSelectedSlot();
                }
                return true;
            case Input.Keys.DOWN:
                if (selectedRow < GRID_ROWS - 1) {
                    selectedRow++;
                    entity.getEvents().trigger("refreshInventoryGrid");
                    showTooltipForSelectedSlot();
                }
                return true;
            case Input.Keys.LEFT:
                if (selectedCol > 0) {
                    selectedCol--;
                    entity.getEvents().trigger("refreshInventoryGrid");
                    showTooltipForSelectedSlot();
                }
                return true;
            case Input.Keys.RIGHT:
                if (selectedCol < GRID_COLS - 1) {
                    selectedCol++;
                    entity.getEvents().trigger("refreshInventoryGrid");
                    showTooltipForSelectedSlot();
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * Shows a tooltip for the currently selected inventory slot (only if it contains an item)
     */
    private void showTooltipForSelectedSlot() {
        // Check if the selected slot has an item
        String itemId = inventoryTab.getItemAt(getSelectedSlotIndex());
        
        if (itemId != null) {
            // Only show tooltip if there's an item in the slot
            String itemDescription = inventoryTab.getItemDescriptionAt(getSelectedSlotIndex());
            TooltipSystem.TooltipManager.showTooltip(itemDescription, TooltipSystem.TooltipStyle.DEFAULT);
        } else {
            // Hide tooltip for empty slots
            TooltipSystem.TooltipManager.hideTooltip();
        }
    }

    /**
     * Sets the inventory tab that this component controls
     */
    public void setInventoryTab(InventoryTab inventoryTab) {
        this.inventoryTab = inventoryTab;
    }
}

package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.scenes.scene2d.Stage;

public interface InventoryTabInterface {
    // Add actors/resources for this tab to the stage
    default void load(Stage stage) {}
    // Remove actors/resources from this tab
    default void unload(Stage stage) {}
}
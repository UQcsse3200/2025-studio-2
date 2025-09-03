package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public interface InventoryTabInterface {
    /** Builds and returns the UI for this tab. */
    Actor build(Skin skin);
}
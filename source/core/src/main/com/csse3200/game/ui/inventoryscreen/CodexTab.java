package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.csse3200.game.screens.MainGameScreen;

public class CodexTab implements InventoryTabInterface {

    private final MainGameScreen screen;

    public CodexTab(MainGameScreen screen) {
        this.screen = screen;
    }

    @Override
    public Actor build(Skin skin) {
        return null;
    }
}

package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

public class UpgradesTab implements InventoryTabInterface {
    private final Texture bgTex = new Texture(Gdx.files.internal("inventory-screen/upgrades-selected.png"));

    // base dimensions of upgrades-selected.png
    private static final float BASE_W = 770f;
    private static final float BASE_H = 768f;
    private static final float BASE_ASPECT = BASE_W / BASE_H;

    @Override
    public Actor build(Skin skin) {
        float screenH = Gdx.graphics.getHeight();
        float canvasH = screenH * (2f / 3f);
        float canvasW = canvasH * BASE_ASPECT;

        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
        bg.setScaling(Scaling.stretch);
        bg.setSize(canvasW, canvasH);

        Container<Image> centered = new Container<>(bg);
        centered.size(canvasW, canvasH);
        centered.align(Align.center);
        return centered;
    }
}
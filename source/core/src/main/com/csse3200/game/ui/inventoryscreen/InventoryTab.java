package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;

public class InventoryTab implements InventoryTabInterface {

    private final Entity player;
    // Background texture
    private final Texture bgTex  = new Texture(Gdx.files.internal("inventory-screen/inventory-selected.png"));
    // empty item
    private final Texture slotTx = new Texture(Gdx.files.internal("inventory-screen/empty_item.png"));
    // Used to maintain aspect ratio when resizing the canvas.
    private static final float BASE_W = 770f;
    private static final float BASE_H = 768f;
    private static final float BASE_ASPECT = BASE_W / BASE_H;

    // Defines where the item grid sits inside the background
    private static final float GRID_X_PCT = 0.415f; // left edge of grid
    private static final float GRID_Y_PCT = 0.182f; // bottom edge of grid
    private static final float GRID_W_PCT = 0.508f;
    private static final float GRID_H_PCT = 0.62f;

    // Nudge the grid a tiny bit (in base-art pixels; negative X = left, negative Y = down)
    private static final float GRID_OFFSET_X_BASE = -18f;
    private static final float GRID_OFFSET_Y_BASE = -30f;

    // Grid layout
    private static final int   GRID_ROWS = 4;
    private static final int   GRID_COLS = 4;
    private static final float GRID_PAD_BASE = 10f;

    public InventoryTab(Entity player) {
        this.player = player;
    }

    @Override
    public Actor build(Skin skin) {
        // The canvas is the whole inventory window including the background.
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float canvasH = screenH * (2f / 3f);        // inventory window = 2/3 of screen height
        float canvasW = canvasH * BASE_ASPECT;      // keep background art aspect ratio

        Stack stack = new Stack(); // allows layering background + grid
        stack.setSize(canvasW, canvasH);

        // Background image, stretched to exactly match canvas size
        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
        bg.setScaling(Scaling.stretch);
        bg.setSize(canvasW, canvasH);
        stack.add(bg);

        // Overlay layer to hold the grid
        Table overlay = new Table();
        overlay.setSize(canvasW, canvasH);
        overlay.setLayoutEnabled(false);

        // Scale factor to convert base art pixels to current canvas pixels
        float scale = canvasH / BASE_H;

        // Grid positioning within background
        float gx = GRID_X_PCT * canvasW + GRID_OFFSET_X_BASE * scale;
        float gy = GRID_Y_PCT * canvasH + GRID_OFFSET_Y_BASE * scale;
        float gw = GRID_W_PCT * canvasW;
        float gh = GRID_H_PCT * canvasH;

        Table grid = new Table();
        grid.setSize(gw, gh);
        grid.setPosition(gx, gy); // place grid inside overlay

        float pad   = GRID_PAD_BASE * scale;

        float slotW = (gw - pad * (GRID_COLS - 1)) / GRID_COLS;
        float slotH = (gh - pad * (GRID_ROWS - 1)) / GRID_ROWS;
        float slotSize = Math.min(slotW, slotH);

        grid.defaults().size(slotSize, slotSize).pad(pad);

        // Fill inventory from your InventoryComponent
        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        int totalSlots = GRID_ROWS * GRID_COLS;
        int filled = 0;
        if (inv != null) {
            // fill first N slots where N = total item count (capped by totalSlots)
            filled = Math.min(inv.getTotalItemCount(), totalSlots);
        }

        for (int i = 0; i < totalSlots; i++) {
            Actor cell;
            if (i < filled) {
                // Item exists but no icons yet -> blank spacer to preserve layout
                // when implementing use a stack (maybe) to use assests/inventory-screen/item-slot-border.png
                Container<Actor> blank = new Container<>();
                blank.size(slotSize, slotSize);
                cell = blank;
            } else {
                // Empty slot art
                Image empty = new Image(new TextureRegionDrawable(new TextureRegion(slotTx)));
                empty.setScaling(Scaling.fit);
                cell = empty;
            }
            grid.add(cell);
            if ((i + 1) % GRID_COLS == 0) grid.row();
        }

        overlay.addActor(grid);
        stack.add(overlay);

        // Center the fixed-size canvas (no expand/fill)
        Container<Stack> centered = new Container<>(stack);
        centered.size(canvasW, canvasH);
        centered.align(Align.center);
        return centered;
    }
}
package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.PixelPerfectPlacer;
import com.csse3200.game.ui.PixelPerfectPlacer.Rect;

public class InventoryTab implements InventoryTabInterface {

  private final Entity player;
  // Background texture
  private final Texture bgTex = new Texture(Gdx.files.internal("inventory-screen/inventory-selected.png"));
  // empty item
  private final Texture emptySlotTexture = new Texture(Gdx.files.internal("inventory-screen/empty_item.png"));

  private static final Rect GRID_PX = new Rect(371, 247, 586, 661);

  private static final int GRID_ROWS = 4;
  private static final int GRID_COLS = 4;
  private static final float SLOT_PADDING = 10f; // Padding between slots in Scene2D units.

  public InventoryTab(Entity player) {
    this.player = player;
  }

  @Override
  public Actor build(Skin skin) {
    PixelPerfectPlacer placer = new PixelPerfectPlacer(bgTex);
    Table gridTable = new Table();
    placer.addOverlay(gridTable, GRID_PX);
    populateGrid(gridTable);

    float screenH = Gdx.graphics.getHeight();
    float canvasH = screenH * (2f / 3f);
    float baseAspect = (float) bgTex.getWidth() / bgTex.getHeight();
    float canvasW = canvasH * baseAspect;

    Container<PixelPerfectPlacer> centered = new Container<>(placer);
    centered.size(canvasW, canvasH);
    centered.align(Align.center);

    return centered;
  }

  private void populateGrid(Table gridTable) {
    gridTable.defaults().pad(SLOT_PADDING);

    InventoryComponent inv = player.getComponent(InventoryComponent.class);
    int totalSlots = GRID_ROWS * GRID_COLS;
    int filledSlots = (inv != null) ? Math.min(inv.getTotalItemCount(), totalSlots) : 0;

    for (int i = 0; i < totalSlots; i++) {
      Actor slotActor;
      if (i < filledSlots) { // Filled slot!
        slotActor = new Container<>();
      } else { // Empty
        Image emptySlotImage = new Image(emptySlotTexture);
        emptySlotImage.setScaling(Scaling.fit);
        slotActor = emptySlotImage;
      }

      gridTable.add(slotActor).expand().fill();
      if ((i + 1) % GRID_COLS == 0) {
        gridTable.row();
      }
    }
  }
}
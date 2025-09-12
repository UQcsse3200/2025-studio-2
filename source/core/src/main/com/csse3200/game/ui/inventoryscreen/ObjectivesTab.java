package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.ui.PixelPerfectPlacer;
import com.csse3200.game.ui.PixelPerfectPlacer.Rect;


public class UpgradesTab implements InventoryTabInterface {
  private static final Rect CLOSE_BUTTON_POS = new Rect(971, 16, 39, 39);

  private static final float BASE_W = 770f;
  private static final float BASE_H = 768f;
  private static final float BASE_ASPECT = BASE_W / BASE_H;

  @Override
  public Actor build(Skin skin) {
    float screenH = Gdx.graphics.getHeight();
    float canvasH = screenH * (2f / 3f);
    float canvasW = canvasH * BASE_ASPECT;

    PixelPerfectPlacer placer = new PixelPerfectPlacer(
        new Texture(Gdx.files.internal("inventory-screen/upgrades-selected.png"))
    );
    placer.setSize(canvasW, canvasH);

    Button closeButton = new Button(new Button.ButtonStyle());
    // Button closeButton = new Button(skin); // Makes this button visible

    closeButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        Gdx.app.exit();
      }
    });

    placer.addOverlay(closeButton, CLOSE_BUTTON_POS);

    final Table root = new Table();
    root.add(placer).center().size(canvasW, canvasH);

    return root;
  }
}

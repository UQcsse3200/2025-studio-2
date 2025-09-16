package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.ui.PixelPerfectPlacer;
import com.csse3200.game.ui.PixelPerfectPlacer.Rect;

/**
 * Objectives tab shown inside the pause menu
 */
public class ObjectivesTab implements InventoryTabInterface {
  // Pixel-accurate position and size for the invisible close button
  private static final Rect CLOSE_BUTTON_POS = new Rect(971, 16, 39, 39);

  // Base artwork height/width in pixels used to compute aspect ratio for scaling
  private static final float BASE_W = 770f;
  private static final float BASE_H = 768f;
  // Aspect ratio of the objectives background
  private static final float BASE_ASPECT = BASE_W / BASE_H;

  private static final int TAB_Y = 130;
  private static final int TAB_H = 72;

  private static final Rect TAB_INVENTORY = new Rect(32,  TAB_Y, 284, TAB_H);
  private static final Rect TAB_UPGRADES  = new Rect(319, TAB_Y, 300, TAB_H);
  private static final Rect TAB_OBJECTIVE = new Rect(623, TAB_Y, 258, TAB_H);

  private final MainGameScreen screen;

  /**
   * Creates an Objectives tab bound to the given main game screen.
   *
   * @param gameScreen main game screen used to unpause and hide the pause menu
   */
  public ObjectivesTab (MainGameScreen gameScreen) {
    this.screen = gameScreen;
  }

  /**
   * Builds the UI actor tree for the objectives tab.
   * The background is placed using a pixel-perfect placer and scaled to two thirds
   * of the current screen height while preserving aspect ratio. An invisible button
   * is overlaid at a fixed pixel rectangle to act as the close hotspot
   *
   * @param skin UI skin used for widget construction
   * @return a root table containing the objectives content
   */
  @Override
  public Actor build(Skin skin) {
    float screenH = Gdx.graphics.getHeight();
    float canvasH = screenH * (2f / 3f);
    float canvasW = canvasH * BASE_ASPECT;

    PixelPerfectPlacer placer = new PixelPerfectPlacer(
        new Texture(Gdx.files.internal("inventory-screen/objectives-selected.png"))
    );
    Button closeButton = new Button(new Button.ButtonStyle());
    closeButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (screen != null) {
          if (screen.isPaused()) {
            screen.togglePaused(); // unpause
          }
          // Update pause menu visibility to reflect paused=false (this hides it)
          screen.togglePauseMenu(PauseMenuDisplay.Tab.INVENTORY);
        } else {
          // No screen available: do nothing (or log)
          Gdx.app.log("InventoryTab", "MainGameScreen was null; close ignored.");
        }
      }
    });
    placer.addOverlay(closeButton, CLOSE_BUTTON_POS);

    addTabHotspot(placer, TAB_INVENTORY, PauseMenuDisplay.Tab.INVENTORY);
    addTabHotspot(placer, TAB_UPGRADES,  PauseMenuDisplay.Tab.UPGRADES);

    final Table root = new Table();
    root.add(placer).center().size(canvasW, canvasH);

    return root;
  }

  /**
   * Adds an invisible, pixel-accurate clickable hotspot that switches to a pause menu tab
   *
   *
   * Creates a Button with no visuals to act as a hotzone
   * Positions and sizes the hotzone using PixelPerfectPlacer so it aligns with the
   * background art and scales correctly with the canvas
   * On click, calls screen.togglePauseMenu(targetTab) to switch tabs without
   * changing the current paused state
   *
   * @param placer    the PixelPerfectPlacer instance that places the overlays to the tab background
   * @param rect      the hotspot rectangle in background image pixels (top-left origin; width/height in pixels)
   * @param targetTab the pause-menu tab to show when the hotspot is clicked
   */
  private void addTabHotspot(PixelPerfectPlacer placer, Rect rect, PauseMenuDisplay.Tab targetTab) {
    Button b = new Button(new Button.ButtonStyle()); // invisible hotzone
    b.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent event, Actor actor) {
        if (screen != null) {
          screen.togglePauseMenu(targetTab); // switch tab, keep paused state
        }
      }
    });
    placer.addOverlay(b, rect);
  }
}

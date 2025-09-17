package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.ui.PixelPerfectPlacer;
import com.csse3200.game.ui.PixelPerfectPlacer.Rect;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Objectives tab UI.
 *
 * Shows each collected objective as a full-width banner image stacked vertically.
 * No grid is used. Each objective instance occupies one row.
 *
 * Layout (pixel-accurate on the background art):
 *   Background: inventory-screen/objectives-selected.png
 *   First banner top-left: (x=46, y=252)
 *   Banner height: 74 px
 *   Vertical gap: 12 px
 *   Close hotspot: (971, 16, 39, 39) — same hit area as Inventory
 *   Tab hotspots (invisible): Inventory and Upgrades are clickable, Settings untouched
 *
 *
 * Objective id mapping (OBJECTIVES bag -> PNG):
 * dash -> inventory-screen/objectives/dash.png
 * door -> inventory-screen/objectives/findDoor.png
 * glider -> inventory-screen/objectives/glider.png
 * jetpack -> inventory-screen/objectives/jetpack.png
 * keycard -> inventory-screen/objectives/keycard.png
 * tutorial -> inventory-screen/objectives/crouch.png
 */
public class ObjectivesTab implements InventoryTabInterface {

  private final MainGameScreen screen;
  private final Entity player;

  // Background
  private final Texture bgTex = new Texture(Gdx.files.internal("inventory-screen/objectives-selected.png"));

  // Tab hotspot rects (same positions you used in Inventory)
  private static final int TAB_Y = 130;
  private static final int TAB_H = 72;
  private static final Rect TAB_INVENTORY = new Rect(32,  TAB_Y, 284, TAB_H);
  private static final Rect TAB_UPGRADES  = new Rect(319, TAB_Y, 300, TAB_H);
  private static final Rect CLOSE_BUTTON_POS = new Rect(971, 16, 39, 39);

  // Objective list placement (top-left origin on the background)
  private static final int START_X = 4;
  private static final int START_Y = 260; // top-left of first banner
  private static final int ROW_H = 74; // each banner is 74 px high
  private static final int V_GAP = 12; // 12 px vertical spacing between banners

  private static final float OBJ_SCALE = 1.5f; // 50% bigger

  // Objective banner textures by id
  private final Map<String, Texture> objectiveTex = new HashMap<>();

  public ObjectivesTab(Entity player, MainGameScreen screen) {
    this.screen = screen;
    this.player = player;

    // Load per-objective banner textures
    objectiveTex.put("dash", new Texture(Gdx.files.internal("images/objectives/dash.png")));
    objectiveTex.put("door", new Texture(Gdx.files.internal("images/objectives/findDoor.png")));
    objectiveTex.put("glider", new Texture(Gdx.files.internal("images/objectives/glider.png")));
    objectiveTex.put("jetpack", new Texture(Gdx.files.internal("images/objectives/jetpack.png")));
    objectiveTex.put("keycard", new Texture(Gdx.files.internal("images/objectives/keycard.png")));
    objectiveTex.put("tutorial", new Texture(Gdx.files.internal("images/objectives/crouch.png")));
  }

  /**
   * Builds the objectives tab as a centered canvas using PixelPerfectPlacer.
   *
   * @param skin scene2d skin (not used for visuals here, but required by the interface)
   * @return root actor to add to the stage
   */
  @Override
  public Actor build(Skin skin) {
    PixelPerfectPlacer placer = new PixelPerfectPlacer(bgTex);

    // Invisible close/hide hotspot (same logic as Inventory)
    Button closeButton = new Button(new Button.ButtonStyle());
    closeButton.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent event, Actor actor) {
        if (screen != null) {
          if (screen.isPaused()) screen.togglePaused();
          screen.togglePauseMenu(PauseMenuDisplay.Tab.INVENTORY);
        }
      }
    });
    placer.addOverlay(closeButton, CLOSE_BUTTON_POS);

    // Invisible tab hotspots
    addTabHotspot(placer, TAB_INVENTORY, PauseMenuDisplay.Tab.INVENTORY);
    addTabHotspot(placer, TAB_UPGRADES,  PauseMenuDisplay.Tab.UPGRADES);

    // Lay out collected objectives vertically
    layoutObjectives(placer);

    // Center the whole canvas at 2/3 screen height
    float screenH = Gdx.graphics.getHeight();
    float canvasH = screenH * (2f / 3f);
    float baseAspect = (float) bgTex.getWidth() / bgTex.getHeight();
    float canvasW = canvasH * baseAspect;

    Container<PixelPerfectPlacer> centered = new Container<>(placer);
    centered.size(canvasW, canvasH);
    centered.align(Align.center);
    return centered;
  }

  /**
   * Reads OBJECTIVES bag and places one banner per item, stacked top-to-bottom.
   * If an id has no matching texture, it is skipped (no labels are created).
   *
   * @param placer the PixelPerfectPlacer instance that places the overlays to the tab background
   */
  private void layoutObjectives(PixelPerfectPlacer placer) {
    InventoryComponent inv = player.getComponent(InventoryComponent.class);
    Map<String, Integer> bag = (inv != null) ? inv.getObjectives() : java.util.Collections.emptyMap();
    // Flatten multiset
    java.util.List<String> instances = new ArrayList<>();
    for (Map.Entry<String, Integer> e : bag.entrySet()) {
      String id = e.getKey();
      for (int i = 0, cnt = Math.max(0, e.getValue()); i < cnt; i++) instances.add(id);
    }

    // Scaled geometry
    final int rowH = Math.round(ROW_H * OBJ_SCALE);
    final int gap  = Math.round(V_GAP * OBJ_SCALE);
    final int x = START_X;
    final int y0 = START_Y;

    for (int i = 0; i < instances.size(); i++) {
      String id = instances.get(i);
      Texture tex = objectiveTex.get(id);
      if (tex == null) { Gdx.app.log("ObjectivesTab","No banner for '"+id+"'"); continue; }

      int y = y0 + i * (rowH + gap);
      int w = Math.round(tex.getWidth() * OBJ_SCALE); // widen by 1.5×
      int h = rowH;

      Image img = new Image(tex);
      placer.addOverlay(img, new Rect(x, y, w, h)); // PixelPerfectPlacer will scale/position
    }
  }

  /**
   * Adds an invisible, pixel-accurate clickable hotspot that switches tabs.
   *
   * @param placer PixelPerfectPlacer anchoring overlays to the background
   * @param rect background-space rectangle (top-left origin)
   * @param tab target pause-menu tab
   */
  private void addTabHotspot(PixelPerfectPlacer placer, Rect rect, PauseMenuDisplay.Tab tab) {
    Button b = new Button(new Button.ButtonStyle());
    b.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent event, Actor actor) {
        if (screen != null) screen.togglePauseMenu(tab);
      }
    });
    placer.addOverlay(b, rect);
  }

  /**
   * Dispose all textures owned by this tab
   */
  public void dispose() {
    bgTex.dispose();
    for (Texture t : objectiveTex.values()) {
      t.dispose();
    }
    objectiveTex.clear();
  }
}

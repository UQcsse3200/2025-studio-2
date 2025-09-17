package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.ui.PixelPerfectPlacer;
import com.csse3200.game.ui.PixelPerfectPlacer.Rect;

import java.util.Map;


/**
 * Pause-menu tab that renders the Upgrades UI.
 * The tab:
 *   Displays an upgrades-selected.png background via PixelPerfectPlacer
 *   Adds an invisible close hotspot positioned in background pixel space.
 *   Renders the player sprite centered on the tab, offset from the top by #PLAYER_TOP_Y
 *   Optionally underlays a “pack” beneath the player if the upgrades bag contains
 *   either "jetpack" or "glider". If both are present, jetpack wins
 *   Provides invisible hotspots for switching to Inventory/Objectives tabs
 */
public class UpgradesTab implements InventoryTabInterface {
  // Invisible close button placement in background pixels (top-left origin)
  private static final Rect CLOSE_BUTTON_POS = new Rect(971, 16, 39, 39);

  // Base background art size used to maintain aspect ratio when scaling
  private static final float BASE_W = 770f;
  private static final float BASE_H = 768f;
  private static final float BASE_ASPECT = BASE_W / BASE_H;

  // Tab button hit-areas (in background pixels). Y and height shared across tabs
  private static final int TAB_Y = 130;
  private static final int TAB_H = 72;
  private static final Rect TAB_INVENTORY = new Rect(32,  TAB_Y, 284, TAB_H);
  private static final Rect TAB_OBJECTIVE = new Rect(623, TAB_Y, 258, TAB_H);

  // Y position (from the top of the background) where the player and pack are placed.
  private static final int PLAYER_TOP_Y = 285;

  private final MainGameScreen screen;
  private final Entity player;

  // Textures owned by this tab
  private final Texture bgTex     = new Texture(Gdx.files.internal("inventory-screen/upgrades-selected.png"));
  private final Texture playerTex = new Texture(Gdx.files.internal("images/upgradesTab/player.png"));
  private final Texture packTex   = new Texture(Gdx.files.internal("images/upgradesTab/jetpack.png"));
  private final Texture gliderTex = new Texture(Gdx.files.internal("images/upgradesTab/glider.png"));
  // private final Texture dashTex = new Texture(Gdx.files.internal("images/upgradesTab/dash.png")); // (future)

  /**
   * Creates an Upgrades tab bound to the given main game screen and player entity
   *
   * @param player the player entity (source of the upgrades bag)
   * @param gameScreen the owning game screen (used to toggle pause/panels)
   */
  public UpgradesTab(Entity player, MainGameScreen gameScreen) {
    this.screen = gameScreen;
    this.player = player;
  }

  /**
   * Builds the Upgrades tab as a centered, aspect preserving canvas
   *   Scales the canvas to 2/3 of screen height; width derived from #BASE_ASPECT
   *   Overlays invisible hotspots for close and tab switches.
   *   Draws player (always) and optionally the pack (jetpack/glider) beneath.
   *
   * @param skin scene2d skin (not used for visuals here, but required by the interface)
   * @return root actor to add to the stage
   */
  @Override
  public Actor build(Skin skin) {
    float screenH = Gdx.graphics.getHeight();
    float canvasH = screenH * (2f / 3f);
    float canvasW = canvasH * BASE_ASPECT;

    PixelPerfectPlacer placer = new PixelPerfectPlacer(bgTex);

    // Invisible "close" button placed exactly over the background's close icon area
    Button closeButton = new Button(new Button.ButtonStyle());
    closeButton.addListener(new ChangeListener() {
      @Override public void changed(ChangeEvent event, Actor actor) {
        if (screen != null) {
          if (screen.isPaused()) screen.togglePaused();  // unpause
          screen.togglePauseMenu(PauseMenuDisplay.Tab.INVENTORY); // hide pause UI
        } else {
          Gdx.app.log("UpgradesTab", "MainGameScreen was null; close ignored.");
        }
      }
    });
    placer.addOverlay(closeButton, CLOSE_BUTTON_POS);

    // Hotspots that switch tabs, maintaining paused state
    addTabHotspot(placer, TAB_INVENTORY, PauseMenuDisplay.Tab.INVENTORY);
    addTabHotspot(placer, TAB_OBJECTIVE, PauseMenuDisplay.Tab.OBJECTIVES);

    // Layer the upgrade visuals + player
    addCharacterLayers(placer);

    Table root = new Table();
    root.add(placer).center().size(canvasW, canvasH);
    return root;
  }

  /**
   * Adds the upgrade visuals to the PixelPerfectPlacer:
   *   Underlay either the jetpack or the glider (if owned). Jetpack has priority if both are present.
   *   Overlay the player sprite on top (always)
   *
   * @param placer the placer anchoring overlays to the background in pixel space
   */
  private void addCharacterLayers(PixelPerfectPlacer placer) {
    int bgW = bgTex.getWidth();

    // Center horizontally
    int playerX = (bgW - playerTex.getWidth()) / 2;
    int playerY = PLAYER_TOP_Y;

    // Read upgrades bag (defensive default to empty)
    Map<String, Integer> upgrades = java.util.Collections.emptyMap();
    InventoryComponent inv = player.getComponent(InventoryComponent.class);
    if (inv != null) upgrades = inv.getUpgrades();

    // Priority: jetpack > glider. Only one may be shown underneath the player.
    boolean showJetpack = has(upgrades, "jetpack");
    boolean showGlider  = !showJetpack && has(upgrades, "glider");
    // boolean showDash = has(upgrades, "dash"); // (future)

    // Underlay the chosen pack beneath the player
    if (showJetpack) {
      int px = (bgW - packTex.getWidth()) / 2;
      int py = PLAYER_TOP_Y;
      placer.addOverlay(new com.badlogic.gdx.scenes.scene2d.ui.Image(packTex),
              new Rect(px, py, packTex.getWidth(), packTex.getHeight()));
    } else if (showGlider) {
      int px = (bgW - gliderTex.getWidth()) / 2;
      int py = PLAYER_TOP_Y;
      placer.addOverlay(new com.badlogic.gdx.scenes.scene2d.ui.Image(gliderTex),
              new Rect(px, py, gliderTex.getWidth(), gliderTex.getHeight()));
    }

    // Player (always) on top
    placer.addOverlay(new com.badlogic.gdx.scenes.scene2d.ui.Image(playerTex),
            new Rect(playerX, playerY, playerTex.getWidth(), playerTex.getHeight()));

    // Dash overlay (keep commented until implemented)
    /*
    if (showDash) {
      int dx = (bgW - dashTex.getWidth()) / 2;
      int dy = PLAYER_TOP_Y;
      placer.addOverlay(new Image(dashTex),
          new Rect(dx, dy, dashTex.getWidth(), dashTex.getHeight()));
    }
    */
  }

  /**
   * Returns whether the upgrades bag contains at least one instance of the given id
   *
   * @param bag upgrades multiset (id -> count)
   * @param id  upgrade id to check (e.g., "jetpack", "glider", "dash")
   * @return true if present and count &gt; 0; otherwise false
   */
  private static boolean has(Map<String, Integer> bag, String id) {
    Integer n = bag.get(id);
    return n != null && n > 0;
  }

  /**
   * Adds an invisible, pixel-accurate clickable hotspot that switches tabs without altering pause state
   *
   * @param placer PixelPerfectPlacer anchoring overlays to the background
   * @param rect background-space rectangle (top-left origin; width/height in pixels)
   * @param tab target pause-menu tab to display
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
   * Disposes all textures owned by this tab.
   * Call when the tab is no longer needed to free GPU resources.
   */
  public void dispose() {
    bgTex.dispose();
    playerTex.dispose();
    packTex.dispose();
    gliderTex.dispose();
    // dashTex.dispose();
  }
}
package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.ui.PixelPerfectPlacer;
import com.csse3200.game.ui.PixelPerfectPlacer.Rect;

import java.util.EnumMap;
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

  // Textures owned by this tab
  private final Texture bgTex     = new Texture(Gdx.files.internal("inventory-screen/upgrades-selected.png"));
  // Textures for upgrades tab titles
  private final Texture packUpgradeTex = new Texture(Gdx.files.internal("inventory-screen/jetpack-upgrade-slot.png"));
  private final Texture gliderUpgradeTex = new Texture(Gdx.files.internal("inventory-screen/glider-upgrade-slot.png"));
  private final Texture dashUpgradeTex = new Texture(Gdx.files.internal("inventory-screen/dash-upgrade-slot.png"));

  private final Texture playerTex = new Texture(Gdx.files.internal("images/upgradesTab/player.png"));
  private final Texture packTex   = new Texture(Gdx.files.internal("images/upgradesTab/jetpack.png"));
  private final Texture gliderTex = new Texture(Gdx.files.internal("images/upgradesTab/glider.png"));
  private final Texture dashTex = new Texture(Gdx.files.internal("images/upgradesTab/dash.png"));
  final String[] toolTipStrings= {
          "A Jetpack! Who needs stairs?\n" +
                  "Fly while you’ve got fuel, then catch your breath while it refuels." +
          "Press \"w\" to soar!",
          "The Glider! Why fall fast when you can fall fancy?\n" +
                  "Hold Left Ctrl mid-air to glide gracefully and slow your descent.",
          "Dash! Walking’s overrated.\n" +
                  "Hit Left Shift to Blast forward like you forgot how to stop!"};

  /**
   * Creates an Upgrades tab bound to the given main game screen and player entity
   *
   * @param gameScreen the owning game screen (used to toggle pause/panels)
   */
  public UpgradesTab(MainGameScreen gameScreen) {
    this.screen = gameScreen;
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


    // Invisible "close" button placed exactly over the background's close icon area
      Map<PauseMenuDisplay.Tab, Rect> tabs = new EnumMap<>(PauseMenuDisplay.Tab.class);
      tabs.put(PauseMenuDisplay.Tab.INVENTORY, TAB_INVENTORY);
      tabs.put(PauseMenuDisplay.Tab.OBJECTIVES, TAB_OBJECTIVE);

      PixelPerfectPlacer placer = PauseMenuDisplay.makeTabScaffold(
              screen, bgTex, CLOSE_BUTTON_POS, tabs);

      // Layer the upgrade visuals + player
    addCharacterLayers(placer);

    Container<PixelPerfectPlacer> root = new Container<>(placer);
    root.size(canvasW, canvasH);
    root.align(Align.center);
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

    // Align left
    int playerX = (bgW - playerTex.getWidth()) / 10;
    int playerY = PLAYER_TOP_Y;

    // Read upgrades bag (defensive default to empty)
    Map<String, Integer> upgrades = java.util.Collections.emptyMap();
    InventoryComponent inv = screen.getGameArea().getPlayer().getComponent(InventoryComponent.class);
    if (inv != null) upgrades = inv.getUpgrades();

    // Priority: jetpack > glider. Only one may be shown underneath the player.
    boolean haveJetpack = has(upgrades, "jetpack");
    boolean haveGlider  = has(upgrades, "glider");
    boolean haveDash = has(upgrades, "dash");
    Boolean[] haveUpgrades = {haveJetpack, haveGlider, haveDash};
    addUpgradeList(haveUpgrades,placer, bgW);
    // Underlay the chosen pack beneath the player
    if (haveJetpack) {
      int px = (bgW - packTex.getWidth()) / 10;
      int py = PLAYER_TOP_Y;
      placer.addOverlay(new com.badlogic.gdx.scenes.scene2d.ui.Image(packTex),
              new Rect(px, py, packTex.getWidth(), packTex.getHeight()));
    } else if (haveGlider) {
      int px = (bgW - gliderTex.getWidth()) / 10;
      int py = PLAYER_TOP_Y;
      placer.addOverlay(new com.badlogic.gdx.scenes.scene2d.ui.Image(gliderTex),
              new Rect(px, py, gliderTex.getWidth(), gliderTex.getHeight()));
    }

    // Dash overlay
    if (haveDash) {
      int px = (bgW - dashTex.getWidth()) / 10;
      int py = PLAYER_TOP_Y;
      placer.addOverlay(new com.badlogic.gdx.scenes.scene2d.ui.Image(dashTex),
              new Rect(px, py, dashTex.getWidth(), dashTex.getHeight()));
    }
    // Player (always) on top
    placer.addOverlay(new com.badlogic.gdx.scenes.scene2d.ui.Image(playerTex),
            new Rect(playerX, playerY, playerTex.getWidth(), playerTex.getHeight()));
  }

  /**
   * addUpgradesList()
   * Helper function to dynamically place in acquired upgrades in the upgrades tab
   *
   * @param upgrades boolean array of the form [0] -> Jetpack, [1] -> Glider, [2] -> Dash
   * @param placer the PixelPerfectPlacer Object to place in textures
   * @param bgW upgrades tab width
   */
  private void addUpgradeList(Boolean[] upgrades, PixelPerfectPlacer placer, int bgW ) {

    int upgradeX = (int) ((bgW - packUpgradeTex.getWidth())  / 1.07f);
    int upgradeY = (int) ((bgW - packUpgradeTex.getHeight()) / 3.5f);

    int offset = (int) (packUpgradeTex.getHeight() * 1.1f);

    Texture[] slotTextures = {packUpgradeTex,gliderUpgradeTex,dashUpgradeTex};

    for (int i = 0; i < 3; i++) {
      if (upgrades[i]) {
        Image slotImage = new Image(slotTextures[i]);
        placer.addOverlay(slotImage,
                new Rect(upgradeX, upgradeY, slotTextures[i].getWidth(), slotTextures[i].getHeight()));
        final int idx = i;

        slotImage.addListener(new InputListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            slotImage.setColor(0.8f, 0.9f, 0.9f, 1f); // warm tint
            TooltipSystem.TooltipManager.showTooltip(toolTipStrings[idx], TooltipSystem.TooltipStyle.DEFAULT);
          }

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            slotImage.setColor(1, 1, 1, 1);
            TooltipSystem.TooltipManager.hideTooltip();
          }
        });
        upgradeY += offset;
      }
    }
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
    Button b = new Button(new Button.ButtonStyle());b.setName("tab:" + tab.name());
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
    dashTex.dispose();
  }
}
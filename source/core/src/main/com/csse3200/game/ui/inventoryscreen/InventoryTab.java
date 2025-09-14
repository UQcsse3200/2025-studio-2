package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.ui.PixelPerfectPlacer;
import com.csse3200.game.ui.PixelPerfectPlacer.Rect;

import java.util.Map;


/**
 * UI tab that renders the player's inventory within the pause menu
 *
 * This tab shows a 4x4 grid of item slots. Each unique item present in the player's
 * {@link InventoryComponent} occupies one "filled" slot, which renders a slot border and,
 * if available, an item image. Remaining cells render as empty slots. The tab also provides
 * a close hotspot (positioned with {@link PixelPerfectPlacer}) which unpauses the game and
 * hides the pause screen when clicked.
 * </p>
 */
public class InventoryTab implements InventoryTabInterface {

  private final Entity player;
  private final MainGameScreen screen;

  private final Texture bgTex = new Texture(Gdx.files.internal("inventory-screen/inventory-selected.png"));
  private final Texture emptySlotTexture = new Texture(Gdx.files.internal("inventory-screen/empty-item.png"));

  private final Texture itemSlotTexture = new Texture(Gdx.files.internal("inventory-screen/item-slot.png"));
  private final Texture keyTexture = new Texture(Gdx.files.internal("images/key.png"));

  private static final Rect GRID_PX = new Rect(371, 247, 586, 661);
  private static final Rect CLOSE_BUTTON_POS = new Rect(971, 16, 39, 39);

  private static final int GRID_ROWS = 4;
  private static final int GRID_COLS = 4;
  private static final float SLOT_PADDING = 10f;

  /**
   * Creates an Inventory tab bound to the given main game screen.
   *
   * @param player entity holding upgrades information
   * @param gameScreen main game screen used to unpause and hide the pause menu
   */
  public InventoryTab(Entity player, MainGameScreen gameScreen) {
    this.player = player;
    this.screen = gameScreen;
  }

  /**
   * Builds the Scene2D Actor representing this inventory tab
   *
   * The returned actor contains the background-aligned layout via PixelPerfectPlacer,
   * an invisible close hotspot at #CLOSE_BUTTON_POS, and a grid region at
   * #GRID_PX populated from the player's InventoryComponent
   *
   *
   * @param skin the UI skin used for widgets and layout
   * @return a centered container wrapping the {@link PixelPerfectPlacer} contents
   */
  @Override
  public Actor build(Skin skin) {
    PixelPerfectPlacer placer = new PixelPerfectPlacer(bgTex);

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

  /**
   * Populates the grid so that each item occupies its own slot
   * notes: if inventory has {key=2}, two key slots are rendered
   *
   * @param gridTable table to populate with item/empty slots
   */
  private void populateGrid(Table gridTable) {
    gridTable.clear();
    gridTable.defaults().pad(SLOT_PADDING).expand().fill();

    InventoryComponent inv = player.getComponent(InventoryComponent.class);
    Map<String, Integer> items = (inv != null) ? inv.getItemsView() : java.util.Collections.emptyMap();

    int totalSlots = GRID_ROWS * GRID_COLS;

    // Flatten the multiset into a per-instance list of base ids (cap at totalSlots)
    java.util.List<String> instanceIds = new java.util.ArrayList<>();
    for (Map.Entry<String, Integer> e : items.entrySet()) {
      String baseId = resolveBaseId(e.getKey());
      int count = Math.max(0, e.getValue());
      for (int c = 0; c < count && instanceIds.size() < totalSlots; c++) {
        instanceIds.add(baseId);
      }
    }

    // Fill grid: first all instances, then empty cells
    for (int i = 0; i < totalSlots; i++) {
      if (i < instanceIds.size()) {
        // FILLED: slot border + item image (if available)
        Stack stack = new Stack();

        Image border = new Image(itemSlotTexture);
        border.setScaling(Scaling.fit);
        stack.addActor(border);

        Texture itemTex = getItemTexture(instanceIds.get(i));
        if (itemTex != null) {
          Image itemImg = new Image(itemTex);
          itemImg.setScaling(Scaling.fit);
          stack.addActor(itemImg);
        } else {
          // Unknown item -> show border only to indicate presence
          Gdx.app.log("InventoryTab", "No sprite for item '" + instanceIds.get(i) + "'; showing border-only slot.");
        }

        gridTable.add(stack);
      } else {
        // Empty item slot
        Image emptySlotImage = new Image(emptySlotTexture);
        emptySlotImage.setScaling(Scaling.fit);
        gridTable.add(emptySlotImage);
      }

      if ((i + 1) % GRID_COLS == 0) {
        gridTable.row();
      }
    }
  }

  /**
   * Resolves a base item id from a potentially qualified id
   *
   * For example,"key:door" becomes "key"; if there is no colon,
   * the input string is returned unchanged.
   *
   * @param full full item identifier (e.g., "key" or "key:door"); must not be null
   * @return the base id prior to any colon
   */
  private String resolveBaseId(String full) {
    int idx = full.indexOf(':');
    return (idx >= 0) ? full.substring(0, idx) : full;
  }

  /**
   * Maps a base item id to its display texture.
   *
   * Currently supports "key" and optionally "door" (mapped to the key icon).
   * Returns null when no sprite is available; the caller will render a border only slot
   *
   * @param baseId the base item identifier
   * @return the texture for the item, or null if unknown
   */
  private Texture getItemTexture(String baseId) {
    // Map known ids to textures
    if ("key".equals(baseId)) return keyTexture;
    if ("door".equals(baseId)) return keyTexture;

    // Unknown item → no sprite; show border-only slot
    return null;
  }

  /**
   * Disposes all textures owned by this tab
   */
  public void dispose() {
    bgTex.dispose();
    emptySlotTexture.dispose();
    itemSlotTexture.dispose();
    keyTexture.dispose();
  }
}
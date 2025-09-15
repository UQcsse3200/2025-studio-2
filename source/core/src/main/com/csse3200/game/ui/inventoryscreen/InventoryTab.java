package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
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
 * This tab shows a 4x4 grid of item slots. Each unique item present in the player's
 * {@link InventoryComponent} occupies one "filled" slot, which renders a slot border and,
 * if available, an item image. Remaining cells render as empty slots. The tab also provides
 * a close hotspot (positioned with {@link PixelPerfectPlacer}) which unpauses the game and
 * hides the pause screen when clicked.
 */
public class InventoryTab implements InventoryTabInterface {

  private final Entity player;
  private final MainGameScreen screen;

  private final Texture bgTex = new Texture(Gdx.files.internal("inventory-screen/inventory-selected.png"));
  private final Texture emptySlotTexture = new Texture(Gdx.files.internal("inventory-screen/empty-item.png"));

  private final Texture itemSlotTexture = new Texture(Gdx.files.internal("inventory-screen/item-slot.png"));
  private final Texture keyTexture = new Texture(Gdx.files.internal("images/key.png"));
  
  // Create a simple selection highlight texture (will be created programmatically)
  private final Texture selectionHighlight;

  private static final Rect GRID_PX = new Rect(371, 247, 586, 661);
  private static final Rect CLOSE_BUTTON_POS = new Rect(971, 16, 39, 39);

  private static final int GRID_ROWS = 4;
  private static final int GRID_COLS = 4;
  private static final float SLOT_PADDING = 10f;

  private Table currentGridTable; // Store reference for refreshing
  private java.util.List<String> currentInstanceIds = new java.util.ArrayList<>(); // Track current slot contents
  private com.csse3200.game.components.inventory.InventoryNavigationComponent navigationComponent;

  /**
   * Creates an Inventory tab bound to the given main game screen.
   *
   * @param player entity holding upgrades information
   * @param gameScreen main game screen used to unpause and hide the pause menu
   */
  public InventoryTab(Entity player, MainGameScreen gameScreen) {
    this.player = player;
    this.screen = gameScreen;
    
    // Create a simple highlight texture programmatically
    this.selectionHighlight = createSelectionHighlightTexture();
  }

  /**
   * Gets the item description for a specific slot index
   * @param slotIndex the slot index (0-based, row-major order)
   * @return the item description or "Empty Slot" if no item
   */
  public String getItemDescriptionAt(int slotIndex) {
    String itemId = getItemAt(slotIndex);
    if (itemId == null) {
      return "Empty Slot\nNo item in this slot";
    }
    
    // Provide detailed descriptions for each item type
    return switch (itemId) {
      case "key" -> "Key\nUnlocks doors and barriers";
      case "door" -> "Door Key\nUnlocks specific doors";  
      case "dash" -> "Dash Upgrade\nGrants dash ability for quick movement";
      case "glider" -> "Glider Upgrade\nAllows gliding through the air";
      case "grapple" -> "Grapple Upgrade\nEnables grappling to distant objects";
      default -> itemId.substring(0, 1).toUpperCase() + itemId.substring(1) + "\nUnknown item type";
    };
  }

  /**
   * Gets the item ID at a specific slot index
   * @param slotIndex the slot index (0-based, row-major order)
   * @return the item ID or null if slot is empty
   */
  public String getItemAt(int slotIndex) {
    if (slotIndex >= 0 && slotIndex < currentInstanceIds.size()) {
      return currentInstanceIds.get(slotIndex);
    }
    return null; // Empty slot
  }

  /**
   * Gets the item ID in a specific slot
   * @param row the row index (0-3)
   * @param col the column index (0-3)
   * @return the item ID or null if slot is empty or coordinates are invalid
   */
  public String getItemAt(int row, int col) {
    if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) {
      return null; // Invalid coordinates
    }
    int slotIndex = row * GRID_COLS + col;
    if (slotIndex < currentInstanceIds.size()) {
      return currentInstanceIds.get(slotIndex);
    }
    return null; // Empty slot
  }

  /**
   * Sets the navigation component that provides selection state
   */
  public void setNavigationComponent(com.csse3200.game.components.inventory.InventoryNavigationComponent navigationComponent) {
    this.navigationComponent = navigationComponent;
  }

  /**
   * Refreshes the grid display to update selection highlighting
   */
  public void refreshGrid(Table gridTable) {
    if (gridTable != null) {
      populateGrid(gridTable);
    } else if (currentGridTable != null) {
      populateGrid(currentGridTable);
    }
  }

  /**
   * Refreshes the current grid display
   */
  public void refreshGrid() {
    if (currentGridTable != null) {
      populateGrid(currentGridTable);
    }
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
    this.currentGridTable = gridTable; // Store reference for refreshing
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

    // Store the current slot contents for navigation access
    this.currentInstanceIds = new java.util.ArrayList<>(instanceIds);

    // Fill grid: first all instances, then empty cells
    for (int i = 0; i < totalSlots; i++) {
      int currentRow = i / GRID_COLS;
      int currentCol = i % GRID_COLS;
      boolean isSelected = navigationComponent != null && 
                          navigationComponent.isNavigationEnabled() && 
                          (currentRow == navigationComponent.getSelectedRow() && 
                           currentCol == navigationComponent.getSelectedCol());
      
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

        // Add selection highlight if this slot is selected
        if (isSelected) {
          Image highlight = new Image(selectionHighlight);
          highlight.setScaling(Scaling.fit);
          stack.addActor(highlight);
        }

        gridTable.add(stack);
      } else {
        // Empty item slot
        Stack stack = new Stack();
        
        Image emptySlotImage = new Image(emptySlotTexture);
        emptySlotImage.setScaling(Scaling.fit);
        stack.addActor(emptySlotImage);
        
        // Add selection highlight if this empty slot is selected
        if (isSelected) {
          Image highlight = new Image(selectionHighlight);
          highlight.setScaling(Scaling.fit);
          stack.addActor(highlight);
        }
        
        gridTable.add(stack);
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
   * Creates a selection highlight texture programmatically.
   * This creates a semi-transparent yellow border overlay.
   */
  private Texture createSelectionHighlightTexture() {
    int size = 64; // Size of the highlight texture
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    
    // Create a yellow border with transparency
    pixmap.setColor(1f, 1f, 0f, 0.8f); // Yellow with 80% opacity
    
    // Draw border (outline only)
    int borderWidth = 4;
    
    // Top and bottom borders
    for (int x = 0; x < size; x++) {
      for (int y = 0; y < borderWidth; y++) {
        pixmap.drawPixel(x, y); // Top border
        pixmap.drawPixel(x, size - 1 - y); // Bottom border
      }
    }
    
    // Left and right borders
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < borderWidth; x++) {
        pixmap.drawPixel(x, y); // Left border
        pixmap.drawPixel(size - 1 - x, y); // Right border
      }
    }
    
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  /**
   * Disposes all textures owned by this tab
   */
  public void dispose() {
    bgTex.dispose();
    emptySlotTexture.dispose();
    itemSlotTexture.dispose();
    keyTexture.dispose();
    selectionHighlight.dispose();
  }
}
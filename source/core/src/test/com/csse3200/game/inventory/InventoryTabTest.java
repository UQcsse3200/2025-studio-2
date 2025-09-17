package com.csse3200.game.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.inventory.InventoryNavigationComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.ui.inventoryscreen.InventoryTab;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for InventoryTab's grid population with per-instance rendering:
 * each instance of an item occupies one filled slot (border + optional item image).
 *
 * We detect:
 * - Empty slots by counting Image nodes that use the empty-slot texture
 * - Filled slots by counting Image nodes that use the item-slot border texture
 */
@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class InventoryTabTest {
    private static final int ROWS = 4;
    private static final int COLS = 4;
    private static final int TOTAL_SLOTS = ROWS * COLS;

    @Mock Entity player;
    @Mock InventoryComponent inventory;

    private Texture fakeBg;
    private Texture fakeEmptySlot;
    private Texture fakeItemSlot;
    private Texture fakeKey;
    private Texture fakeHighlight;

    @BeforeEach
    void setup() {
        when(player.getComponent(InventoryComponent.class)).thenReturn(inventory);

        // Tiny 2x2 RGBA textures so tests don't touch disk
        fakeBg = makeTinyTex();
        fakeEmptySlot = makeTinyTex();
        fakeItemSlot = makeTinyTex();
        fakeKey = makeTinyTex();
        fakeHighlight = makeTinyTex();
    }

    @AfterEach
    void tearDown() {
        Arrays.asList(fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey, fakeHighlight)
                .forEach(t -> { if (t != null) t.dispose(); });
    }


    @Test
    @DisplayName("Empty inventory -> all 16 empty slots")
    void emptyInventoryShowsAllEmptySlots() throws Exception {
        when(inventory.getInventory()).thenReturn(Collections.emptyMap());

        InventoryTab tab = new InventoryTab(player, /*screen*/ null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey);

        Actor root = tab.build(/*skin*/ null);

        assertEquals(TOTAL_SLOTS, countImagesUsingTexture(root, fakeEmptySlot));
        assertEquals(0, countImagesUsingTexture(root, fakeItemSlot));
    }

    @Test
    @DisplayName("Partial inventory per-instance: 5 keys -> 5 filled, 11 empty")
    void partiallyFilledInventoryCountsCorrectly() throws Exception {
        when(inventory.getInventory()).thenReturn(Map.of("key",5));

        InventoryTab tab = new InventoryTab(player, null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey);

        Actor root = tab.build(null);

        assertEquals(11, countImagesUsingTexture(root, fakeEmptySlot));
        assertEquals(5, countImagesUsingTexture(root, fakeItemSlot));
    }

    @Test
    @DisplayName("Overflow inventory is clamped: 100 keys -> 16 filled, 0 empty")
    void fullInventoryShowsNoEmptySlots() throws Exception {
        when(inventory.getInventory()).thenReturn(Map.of("key", 100));

        InventoryTab tab = new InventoryTab(player, null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey);

        Actor root = tab.build(null);

        assertEquals(0, countImagesUsingTexture(root, fakeEmptySlot));
        assertEquals(TOTAL_SLOTS, countImagesUsingTexture(root, fakeItemSlot));
    }

    @Test
    @DisplayName("Null inventory component -> all 16 empty slots")
    void nullInventoryGracefullyShowsEmpties() throws Exception {
        when(player.getComponent(InventoryComponent.class)).thenReturn(null);

        InventoryTab tab = new InventoryTab(player, null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey);

        Actor root = tab.build(null);

        assertEquals(TOTAL_SLOTS, countImagesUsingTexture(root, fakeEmptySlot));
        assertEquals(0, countImagesUsingTexture(root, fakeItemSlot));
    }

    @Test
    @DisplayName("Multiple item types flatten correctly: key=2, door=1 -> 3 filled, 13 empty")
    void multipleItemsFlattenPerInstance() throws Exception {
        // door maps to key texture in InventoryTab#getItemTexture
        when(inventory.getInventory()).thenReturn(Map.of("key", 2, "door", 1));

        InventoryTab tab = new InventoryTab(player, null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey);

        Actor root = tab.build(null);

        assertEquals(13, countImagesUsingTexture(root, fakeEmptySlot));
        assertEquals( 3, countImagesUsingTexture(root, fakeItemSlot));
    }

    @Test
    @DisplayName("Unknown item ids still show border-only filled slots")
    void unknownItemsShowBorderOnly() throws Exception {
        when(inventory.getInventory()).thenReturn(Map.of("mystery", 3));

        InventoryTab tab = new InventoryTab(player, null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey);

        Actor root = tab.build(null);

        // 3 filled (border), 13 empty; item image may be missing but border must be present
        assertEquals(13, countImagesUsingTexture(root, fakeEmptySlot));
        assertEquals( 3, countImagesUsingTexture(root, fakeItemSlot));

        // Optional: check that the key sprite wasn't used for unknown items
        assertEquals(0, countImagesUsingTexture(root, fakeKey));
    }

    @Test
    @DisplayName("Qualified ids use base id: 'key:door'=2 -> 2 key slots")
    void qualifiedIdsUseBaseId() throws Exception {
        when(inventory.getInventory()).thenReturn(Map.of("key:door", 2));

        InventoryTab tab = new InventoryTab(player, null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey);

        Actor root = tab.build(null);

        assertEquals(14, countImagesUsingTexture(root, fakeEmptySlot));
        assertEquals( 2, countImagesUsingTexture(root, fakeItemSlot));
        // Two actual key images are expected as well
        assertEquals( 2, countImagesUsingTexture(root, fakeKey));
    }

    @Test
    @DisplayName("Selection highlight appears on a FILLED slot")
    void selectionHighlightOnFilledSlot() throws Exception {
        // Inventory with 2 keys => slots 0 and 1 are filled
        when(inventory.getInventory()).thenReturn(Map.of("key", 2));

        InventoryTab tab = new InventoryTab(player, null);
        // Also replace the highlight texture so we can count it
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey, fakeHighlight);

        // Mock navigation: enable + select row=0, col=1  (slot index 1 -> filled)
        InventoryNavigationComponent nav = mock(InventoryNavigationComponent.class);
        when(nav.isNavigationEnabled()).thenReturn(true);
        when(nav.getSelectedRow()).thenReturn(0);
        when(nav.getSelectedCol()).thenReturn(1);
        tab.setNavigationComponent(nav);

        Actor root = tab.build(null);

        assertEquals(1, countHighlights(root, fakeHighlight), "Exactly one highlight should be drawn on the selected filled slot");
    }

    @Test
    @DisplayName("Selection highlight appears on an EMPTY slot")
    void selectionHighlightOnEmptySlot() throws Exception {
        // Inventory empty => all slots empty
        when(inventory.getInventory()).thenReturn(Collections.emptyMap());

        InventoryTab tab = new InventoryTab(player, null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey, fakeHighlight);

        // Select bottom-right cell (row=3, col=3 => index 15), which is empty
        InventoryNavigationComponent nav = mock(InventoryNavigationComponent.class);
        when(nav.isNavigationEnabled()).thenReturn(true);
        when(nav.getSelectedRow()).thenReturn(3);
        when(nav.getSelectedCol()).thenReturn(3);
        tab.setNavigationComponent(nav);

        Actor root = tab.build(null);

        assertEquals(1, countHighlights(root, fakeHighlight), "Exactly one highlight should be drawn on the selected empty slot");
    }

    @Test
    @DisplayName("Selection disabled -> no highlight")
    void selectionDisabledNoHighlight() throws Exception {
        when(inventory.getInventory()).thenReturn(Map.of("key", 1));

        InventoryTab tab = new InventoryTab(player, null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey, fakeHighlight);

        InventoryNavigationComponent nav = mock(InventoryNavigationComponent.class);
        when(nav.isNavigationEnabled()).thenReturn(false); // <- only this is needed
        tab.setNavigationComponent(nav);

        Actor root = tab.build(null);

        assertEquals(0, countHighlights(root, fakeHighlight),
                "No highlight should be drawn when navigation is disabled");
    }

    @Test
    @DisplayName("refreshGrid() moves the highlight when selection changes")
    void refreshMovesHighlight() throws Exception {
        // Inventory with 3 items => first row has indexes 0..2 filled
        when(inventory.getInventory()).thenReturn(Map.of("key", 3));

        InventoryTab tab = new InventoryTab(player, null);
        replacePrivateTextures(tab, fakeBg, fakeEmptySlot, fakeItemSlot, fakeKey, fakeHighlight);

        InventoryNavigationComponent nav = mock(InventoryNavigationComponent.class);
        when(nav.isNavigationEnabled()).thenReturn(true);

        // Initial selection (row=0, col=0)
        when(nav.getSelectedRow()).thenReturn(0);
        when(nav.getSelectedCol()).thenReturn(0);
        tab.setNavigationComponent(nav);

        Actor root = tab.build(null);
        assertEquals(1, countHighlights(root, fakeHighlight), "One highlight after initial build");

        // Change selection to (row=0, col=2) and refresh grid
        when(nav.getSelectedRow()).thenReturn(0);
        when(nav.getSelectedCol()).thenReturn(2);
        tab.refreshGrid();

        // Still exactly one highlight (moved to new cell)
        assertEquals(1, countHighlights(root, fakeHighlight), "One highlight after selection moved via refreshGrid()");
    }


    // Helpers

    // Create a tiny 2x2 RGBA texture
    private static Texture makeTinyTex() {
        Pixmap pm = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Swap InventoryTab's private textures (without highlight) with in memory ones so tests don't hit disk
     */
    private static void replacePrivateTextures(InventoryTab tab, Texture bgTex,
                                               Texture emptySlotTex, Texture itemSlotTex, Texture keyTex) throws Exception {
        setPrivateField(tab, "bgTex", bgTex);
        setPrivateField(tab, "emptySlotTexture", emptySlotTex);
        setPrivateField(tab, "itemSlotTexture", itemSlotTex);
        setPrivateField(tab, "keyTexture", keyTex);
    }

    /**
     * Swap InventoryTab's private textures including the selection highlight.
     */
    private static void replacePrivateTextures(InventoryTab tab, Texture bgTex,
                                               Texture emptySlotTex, Texture itemSlotTex, Texture keyTex,
                                               Texture selectionHighlightTex) throws Exception {
        replacePrivateTextures(tab, bgTex, emptySlotTex, itemSlotTex, keyTex);
        setPrivateField(tab, "selectionHighlight", selectionHighlightTex);
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = InventoryTab.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * Count Image nodes that use the given Texture (match by object identity).
     */
    private static int countImagesUsingTexture(Actor root, Texture tex) {
        List<Image> images = new ArrayList<>();
        collectImages(root, images);

        int count = 0;
        for (Image img : images) {
            Drawable d = img.getDrawable();
            if (d instanceof TextureRegionDrawable trd) {
                if (trd.getRegion() != null && trd.getRegion().getTexture() == tex) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countHighlights(Actor root, Texture highlightTex) {
        return countImagesUsingTexture(root, highlightTex);
    }

    // Recursively collect all Image actors from the subtree
    private static void collectImages(Actor a, List<Image> out) {
        if (a instanceof Image) {
            out.add((Image) a);
        }
        if (a instanceof Group g) {
            for (Actor c : g.getChildren()) {
                collectImages(c, out);
            }
        }
    }
}
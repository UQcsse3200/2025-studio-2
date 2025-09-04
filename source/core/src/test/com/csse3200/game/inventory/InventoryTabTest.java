package com.csse3200.game.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.ui.inventoryscreen.InventoryTab;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for InventoryTab's grid population
 * Detect empty slots by counting Image nodes that use the slot texture
 * Filled slots are blank Containers (no Image)
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
    private Texture fakeSlot;

    @BeforeEach
    void setup() throws Exception {
        // Default: player has an inventory
        when(player.getComponent(InventoryComponent.class)).thenReturn(inventory);

        // Build tiny 2x2 RGBA textures for bg and empty slot
        fakeBg = makeTinyTex();
        fakeSlot = makeTinyTex();
    }

    @AfterEach
    void tearDown() {
        if (fakeBg != null) fakeBg.dispose();
        if (fakeSlot != null) fakeSlot.dispose();
    }

    @Test
    @DisplayName("No inventory, all 16 cells are empty images")
    void emptyInventoryShowsAllEmptySlots() throws Exception {
        when(inventory.getTotalItemCount()).thenReturn(0);

        InventoryTab tab = new InventoryTab(player);
        replacePrivateTextures(tab, fakeBg, fakeSlot);

        Actor root = tab.build(/*skin*/ null);

        int emptyImages = countImagesUsingTexture(root, fakeSlot);
        assertEquals(TOTAL_SLOTS, emptyImages, "All slots should be empty images");
    }

    @Test
    @DisplayName("Partial inventory")
    void partiallyFilledInventoryCountsCorrectly() throws Exception {
        when(inventory.getTotalItemCount()).thenReturn(5); // 5 filled, 11 empty

        InventoryTab tab = new InventoryTab(player);
        replacePrivateTextures(tab, fakeBg, fakeSlot);

        Actor root = tab.build(null);

        int emptyImages = countImagesUsingTexture(root, fakeSlot);
        assertEquals(11, emptyImages, "Empty image count should be TOTAL - filled");
    }

    @Test
    @DisplayName("Full or overflow inventory")
    void fullInventoryShowsNoEmptySlots() throws Exception {
        when(inventory.getTotalItemCount()).thenReturn(100); // clamped to 16

        InventoryTab tab = new InventoryTab(player);
        replacePrivateTextures(tab, fakeBg, fakeSlot);

        Actor root = tab.build(null);

        int emptyImages = countImagesUsingTexture(root, fakeSlot);
        assertEquals(0, emptyImages, "No empty images when all slots are filled");
    }

    @Test
    @DisplayName("Null inventory component, all 16 cells are empty images")
    void nullInventoryGracefullyShowsEmpties() throws Exception {
        when(player.getComponent(InventoryComponent.class)).thenReturn(null);

        InventoryTab tab = new InventoryTab(player);
        replacePrivateTextures(tab, fakeBg, fakeSlot);

        Actor root = tab.build(null);

        int emptyImages = countImagesUsingTexture(root, fakeSlot);
        assertEquals(TOTAL_SLOTS, emptyImages);
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

    // Swap InventoryTab's private bg/slot textures with in memory ones
    // Uses reflection to set the final fields 'bgTex' and 'slotTx'
    private static void replacePrivateTextures(InventoryTab tab, Texture bgTex, Texture slotTex) throws Exception {
        setFinalField(tab, "bgTex", bgTex);
        setFinalField(tab, "slotTx", slotTex);
    }

    private static void setFinalField(Object target, String fieldName, Object value) throws Exception {
        Field f = InventoryTab.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // Count Image nodes by the provided texture
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

    // Recursively collect all Image actors from the subtree
    private static void collectImages(Actor a, List<Image> out) {
        if (a instanceof Image) {
            out.add((Image) a);
        }
        if (a instanceof Group g) {
            for (Actor c : g.getChildren()) collectImages(c, out);
        }
    }
}
package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.screens.MainGameScreen;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests for ObjectivesTab: verifies that one banner Image is rendered for each
 * instance in the OBJECTIVES bag; unknown IDs are skipped; null inventory is safe.
 */
@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class ObjectivesTabTest {
    @Mock MainGameScreen screen;
    @Mock GameArea gameArea;
    @Mock Entity player;
    @Mock InventoryComponent inventory;

    private Texture fakeBg;
    private Texture texDash, texDoor, texGlider, texJetpack, texKeycard, texTutorial;

    @BeforeEach
    void setup() {
        when(screen.getGameArea()).thenReturn(gameArea);
        when(gameArea.getPlayer()).thenReturn(player);
        when(player.getComponent(InventoryComponent.class)).thenReturn(inventory);

        // Tiny 2x2 RGBA textures so we never hit disk
        fakeBg = makeTinyTex();
        texDash = makeTinyTex();
        texDoor = makeTinyTex();
        texGlider = makeTinyTex();
        texJetpack = makeTinyTex();
        texKeycard = makeTinyTex();
        texTutorial= makeTinyTex();
    }

    @AfterEach
    void tearDown() {
        // Dispose our in-memory textures
        for (Texture t : List.of(fakeBg, texDash, texDoor, texGlider, texJetpack, texKeycard, texTutorial)) {
            if (t != null) t.dispose();
        }
    }

    @Test
    @DisplayName("Empty objectives -> no banners")
    void emptyObjectivesShowsNoBanners() throws Exception {
        when(inventory.getObjectives()).thenReturn(Collections.emptyMap());

        ObjectivesTab tab = new ObjectivesTab(screen);
        replaceTextures(tab, fakeBg, Map.of(
                "dash", texDash, "door", texDoor, "glider", texGlider,
                "jetpack", texJetpack, "keycard", texKeycard, "tutorial", texTutorial
        ));

        Actor root = tab.build(/*skin*/ null);

        int total =
                countImagesUsing(root, texDash) + countImagesUsing(root, texDoor) +
                        countImagesUsing(root, texGlider) + countImagesUsing(root, texJetpack) +
                        countImagesUsing(root, texKeycard) + countImagesUsing(root, texTutorial);

        assertEquals(0, total, "No objective banners should be drawn");
    }

    @Test
    @DisplayName("Single type per-instance: door=1 -> one door banner")
    void singleObjectiveRendersOneBanner() throws Exception {
        when(inventory.getObjectives()).thenReturn(Map.of("door", 1));

        ObjectivesTab tab = new ObjectivesTab(screen);
        replaceTextures(tab, fakeBg, Map.of(
                "dash", texDash, "door", texDoor, "glider", texGlider,
                "jetpack", texJetpack, "keycard", texKeycard, "tutorial", texTutorial
        ));

        Actor root = tab.build(null);

        assertEquals(1, countImagesUsing(root, texDoor));
        // Sanity: others should be zero
        assertEquals(0, countImagesUsing(root, texDash));
        assertEquals(0, countImagesUsing(root, texGlider));
        assertEquals(0, countImagesUsing(root, texJetpack));
        assertEquals(0, countImagesUsing(root, texKeycard));
        assertEquals(0, countImagesUsing(root, texTutorial));
    }

    @Test
    @DisplayName("Multiple types & counts: dash=1, glider=2, jetpack=1, keycard=1")
    void multipleObjectivesRenderAllInstances() throws Exception {
        when(inventory.getObjectives()).thenReturn(Map.of(
                "dash", 1,
                "glider", 2,
                "jetpack", 1,
                "keycard", 1
        ));

        ObjectivesTab tab = new ObjectivesTab(screen);
        replaceTextures(tab, fakeBg, Map.of(
                "dash", texDash, "door", texDoor, "glider", texGlider,
                "jetpack", texJetpack, "keycard", texKeycard, "tutorial", texTutorial
        ));

        Actor root = tab.build(null);

        assertEquals(1, countImagesUsing(root, texDash));
        assertEquals(2, countImagesUsing(root, texGlider));
        assertEquals(1, countImagesUsing(root, texJetpack));
        assertEquals(1, countImagesUsing(root, texKeycard));
    }

    @Test
    @DisplayName("Unknown objective IDs are skipped")
    void unknownIdsAreSkipped() throws Exception {
        when(inventory.getObjectives()).thenReturn(Map.of(
                "unknown", 3,
                "door", 2
        ));

        ObjectivesTab tab = new ObjectivesTab(screen);
        replaceTextures(tab, fakeBg, Map.of(
                "dash", texDash, "door", texDoor, "glider", texGlider,
                "jetpack", texJetpack, "keycard", texKeycard, "tutorial", texTutorial
        ));

        Actor root = tab.build(null);

        // Only 'door' banners should appear
        assertEquals(2, countImagesUsing(root, texDoor));
        assertEquals(0, countImagesUsing(root, texDash));
        assertEquals(0, countImagesUsing(root, texGlider));
        assertEquals(0, countImagesUsing(root, texJetpack));
        assertEquals(0, countImagesUsing(root, texKeycard));
        assertEquals(0, countImagesUsing(root, texTutorial));
    }

    @Test
    @DisplayName("Null inventory component -> safe (no banners)")
    void nullInventoryIsSafe() throws Exception {
        when(player.getComponent(InventoryComponent.class)).thenReturn(null);

        ObjectivesTab tab = new ObjectivesTab(screen);
        replaceTextures(tab, fakeBg, Map.of(
                "dash", texDash, "door", texDoor, "glider", texGlider,
                "jetpack", texJetpack, "keycard", texKeycard, "tutorial", texTutorial
        ));

        Actor root = tab.build(null);

        int total =
                countImagesUsing(root, texDash) + countImagesUsing(root, texDoor) +
                        countImagesUsing(root, texGlider) + countImagesUsing(root, texJetpack) +
                        countImagesUsing(root, texKeycard) + countImagesUsing(root, texTutorial);

        assertEquals(0, total);
    }

    // Helpers

    private static Texture makeTinyTex() {
        Pixmap pm = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Swap private textures in ObjectivesTab so tests never touch disk.
     * - Replaces bgTex
     * - Replaces objectiveTex map with our provided map
     */
    @SuppressWarnings("unchecked")
    private static void replaceTextures(ObjectivesTab tab, Texture bgTex, Map<String, Texture> idToTex) throws Exception {
        setPrivateField(tab, "bgTex", bgTex);
        // Replace the whole objectiveTex map with our in-memory textures
        setPrivateField(tab, "objectiveTex", new HashMap<>(idToTex));
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * Count Image nodes using a specific Texture (by object identity).
     */
    private static int countImagesUsing(Actor root, Texture tex) {
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

    private static void collectImages(Actor a, List<Image> out) {
        if (a instanceof Image) out.add((Image) a);
        if (a instanceof Group g) {
            for (Actor c : g.getChildren()) collectImages(c, out);
        }
    }
}
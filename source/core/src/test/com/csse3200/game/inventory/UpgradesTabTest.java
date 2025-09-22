package com.csse3200.game.inventory;

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
import com.csse3200.game.ui.inventoryscreen.UpgradesTab;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for UpgradesTab layering logic:
 * - Player is always shown
 * - Jetpack underlays player when present
 * - If both jetpack and glider exist, only jetpack shows
 * - Unknown ids do not render anything extra
 * - Null inventory is safe
 */
@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class UpgradesTabTest {
    @Mock MainGameScreen screen;
    @Mock GameArea gameArea;
    @Mock Entity player;
    @Mock InventoryComponent inventory;

    private Texture fakeBg;
    private Texture texPlayer;
    private Texture texJetpack;
    private Texture texGlider;

    @BeforeEach
    void setup() {
        when(screen.getGameArea()).thenReturn(gameArea);
        when(gameArea.getPlayer()).thenReturn(player);
        // player -> inventory unless explicitly overridden in a test
        when(player.getComponent(InventoryComponent.class)).thenReturn(inventory);

        // Tiny in-memory textures so tests never hit disk
        fakeBg     = makeTinyTex();
        texPlayer  = makeTinyTex();
        texJetpack = makeTinyTex();
        texGlider  = makeTinyTex();


    }

    @AfterEach
    void tearDown() {
        for (Texture t : List.of(fakeBg, texPlayer, texJetpack, texGlider)) {
            if (t != null) t.dispose();
        }
    }

    @Test
    @DisplayName("No upgrades: player only")
    void noUpgrades_playerOnly() throws Exception {
        when(inventory.getUpgrades()).thenReturn(Map.of());

        UpgradesTab tab = new UpgradesTab(screen);
        replacePrivateTextures(tab, fakeBg, texPlayer, texJetpack, texGlider);

        Actor root = tab.build(/*skin*/ null);

        assertEquals(1, countImagesUsing(root, texPlayer),  "player should render");
        assertEquals(0, countImagesUsing(root, texJetpack), "no jetpack");
        assertEquals(0, countImagesUsing(root, texGlider),  "no glider");
    }

    @Test
    @DisplayName("Jetpack present: player + jetpack")
    void jetpackOnly_rendersJetpack() throws Exception {
        when(inventory.getUpgrades()).thenReturn(Map.of("jetpack", 1));

        UpgradesTab tab = new UpgradesTab(screen);
        replacePrivateTextures(tab, fakeBg, texPlayer, texJetpack, texGlider);

        Actor root = tab.build(null);

        assertEquals(1, countImagesUsing(root, texPlayer));
        assertEquals(1, countImagesUsing(root, texJetpack));
        assertEquals(0, countImagesUsing(root, texGlider));
    }

    @Test
    @DisplayName("Glider present: player + glider")
    void gliderOnly_rendersGlider() throws Exception {
        when(inventory.getUpgrades()).thenReturn(Map.of("glider", 2)); // count>0 still one image

        UpgradesTab tab = new UpgradesTab(screen);
        replacePrivateTextures(tab, fakeBg, texPlayer, texJetpack, texGlider);

        Actor root = tab.build(null);

        assertEquals(1, countImagesUsing(root, texPlayer));
        assertEquals(0, countImagesUsing(root, texJetpack));
        assertEquals(1, countImagesUsing(root, texGlider));
    }

    @Test
    @DisplayName("Both jetpack and glider -> jetpack wins (glider hidden)")
    void bothJetpackAndGlider_jetpackWins() throws Exception {
        when(inventory.getUpgrades()).thenReturn(Map.of("jetpack", 1, "glider", 1));

        UpgradesTab tab = new UpgradesTab(screen);
        replacePrivateTextures(tab, fakeBg, texPlayer, texJetpack, texGlider);

        Actor root = tab.build(null);

        assertEquals(1, countImagesUsing(root, texPlayer));
        assertEquals(1, countImagesUsing(root, texJetpack), "jetpack underlay should render");
        assertEquals(0, countImagesUsing(root, texGlider),  "glider suppressed when jetpack present");
    }

    @Test
    @DisplayName("Unknown upgrade ids are ignored")
    void unknownIdsIgnored() throws Exception {
        when(inventory.getUpgrades()).thenReturn(Map.of("hoverboots", 3));

        UpgradesTab tab = new UpgradesTab(screen);
        replacePrivateTextures(tab, fakeBg, texPlayer, texJetpack, texGlider);

        Actor root = tab.build(null);

        assertEquals(1, countImagesUsing(root, texPlayer));
        assertEquals(0, countImagesUsing(root, texJetpack));
        assertEquals(0, countImagesUsing(root, texGlider));
    }

    @Test
    @DisplayName("Null inventory component: safe (player only)")
    void nullInventory_safe() throws Exception {
        when(player.getComponent(InventoryComponent.class)).thenReturn(null);

        UpgradesTab tab = new UpgradesTab(screen);
        replacePrivateTextures(tab, fakeBg, texPlayer, texJetpack, texGlider);

        Actor root = tab.build(null);

        assertEquals(1, countImagesUsing(root, texPlayer));
        assertEquals(0, countImagesUsing(root, texJetpack));
        assertEquals(0, countImagesUsing(root, texGlider));
    }

    // helpers

    private static Texture makeTinyTex() {
        Pixmap pm = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static void replacePrivateTextures(UpgradesTab tab, Texture bg, Texture player, Texture jetpack, Texture glider) throws Exception {
        setPrivate(tab, "bgTex", bg);
        setPrivate(tab, "playerTex", player);
        setPrivate(tab, "packTex", jetpack);
        setPrivate(tab, "gliderTex", glider);
    }

    private static void setPrivate(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    /** Count Image nodes that render the given Texture (by object identity). */
    private static int countImagesUsing(Actor root, Texture tex) {
        int[] count = {0};
        collectImages(root, img -> {
            Drawable d = img.getDrawable();
            if (d instanceof TextureRegionDrawable trd) {
                if (trd.getRegion() != null && trd.getRegion().getTexture() == tex) count[0]++;
            }
        });
        return count[0];
    }

    private interface ImageConsumer { void accept(Image img); }

    private static void collectImages(Actor a, ImageConsumer consumer) {
        if (a instanceof Image img) consumer.accept(img);
        if (a instanceof Group g) for (Actor c : g.getChildren()) collectImages(c, consumer);
    }
}
package com.csse3200.game.components.computerterminal;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.PixelPerfectPlacer;
import com.csse3200.game.ui.UIComponent;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Headless test that:
 * 1) builds TerminalUiComponent with a 1x2 spritesheet (right tile is correct)
 * 2) simulates clicking the right tile and pressing SUBMIT
 * 3) asserts "VERIFIED." appears and grid becomes untouchable
 */
public class TerminalUiComponentHeadlessTest {
    private static HeadlessApplication app;

    private Stage stage;
    private ResourceService resourceService;
    private RenderService renderService;

    private Texture bgTex, blueTex, spriteTex;

    @BeforeAll
    static void boot() {
        if (app != null) return;
        HeadlessApplicationConfiguration cfg = new HeadlessApplicationConfiguration();
        app = new HeadlessApplication(new ApplicationAdapter(){}, cfg);
    }

    @AfterAll
    static void shutdown() {
        if (app != null) {
            app.exit();
            app = null;
        }
    }

    @BeforeEach
    void setUp() {
        // Minimal Stage
        stage = new Stage();

        // Fake textures via Pixmap
        bgTex = solidTexture(800, 1000, 0xffffffff);       // white bg
        blueTex = solidTexture(16, 16, 0xff1e90ffffL);      // dodger blue

        // Spritesheet 1x2: left half red, right half green
        spriteTex = twoColorTexture(200, 100, 0xffcc4444L, 0xff44cc66L);

        // Mock ResourceService to return our textures
        resourceService = mock(ResourceService.class);
        when(resourceService.getAsset("images/terminal_bg.png", Texture.class)).thenReturn(bgTex);
        when(resourceService.getAsset("images/terminal_bg_blue.png", Texture.class)).thenReturn(blueTex);
        when(resourceService.getAsset("images/puzzles/test_1x2.png", Texture.class)).thenReturn(spriteTex);

        // Mock RenderService to expose our stage
        renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(stage);

        // Register services used by UI component
        ServiceLocator.registerRenderService(renderService);
        ServiceLocator.registerResourceService(resourceService);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
        if (bgTex != null) bgTex.dispose();
        if (blueTex != null) blueTex.dispose();
        if (spriteTex != null) spriteTex.dispose();
    }

    @Test
    void selectsRightTile_thenVerified_andGridDisabled() {
        // Build a bank with a single 1x2 spec (correct index = 1, i.e., right tile)
        SimpleCaptchaBank bank = new SimpleCaptchaBank()
                .add(new SpritesheetSpec("images/puzzles/test_1x2.png", 1, 2, Set.of(1), "Pick the right tile"));

        // Terminal UI component
        MainGameScreen fakeScreen = mock(MainGameScreen.class);
        when(fakeScreen.isPaused()).thenReturn(false); // allow it to toggle pause
        TerminalUiComponent ui = new TerminalUiComponent(fakeScreen).setCaptchaBank(bank);

        Entity uiEntity = new Entity();
        uiEntity.addComponent(ui);
        uiEntity.create();

        // Open via event (with a dummy "terminal" entity to receive result)
        Entity terminal = new Entity();
        final AtomicReference<CaptchaResult> lastResult = new AtomicReference<>();
        terminal.getEvents().addListener("terminal:captchaResult", (CaptchaResult r) -> lastResult.set(r));

        uiEntity.getEvents().trigger("terminal:open", terminal);

        // Process stage once to lay out actors
        stage.act(0);

        // Click the right tile (index 1)
        Actor rightTile = findNthTile(stage, 1);
        assertNotNull(rightTile, "Could not find 2nd tile (index 1)");
        clickActor(rightTile);

        // Click SUBMIT
        TextButton submit = findSubmit(stage);
        assertNotNull(submit, "Submit button not found");
        clickActor(submit);

        // Verify label shows VERIFIED. (somewhere in actor tree)
        Label verified = findLabelWithText(stage, "VERIFIED.");
        assertNotNull(verified, "VERIFIED. message not shown");

        // Grid must be untouchable (component sets gridTable touchable disabled)
        Table grid = findGridTable(stage);
        assertNotNull(grid, "Grid table not found");
        assertEquals(Touchable.disabled, grid.getTouchable(), "Grid should be disabled after verify");

        // Emitted result should be success with selected={1}
        assertNotNull(lastResult.get(), "Expected a captchaResult event");
        assertTrue(lastResult.get().success(), "Result should be success");
        assertEquals(Set.of(1), lastResult.get().selected());
    }

    // --- helpers ---

    private static Texture solidTexture(int w, int h, long rgba8888) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(intToColor(rgba8888));
        p.fill();
        Texture t = new Texture(p);
        p.dispose();
        return t;
    }

    private static Texture twoColorTexture(int w, int h, long leftRGBA, long rightRGBA) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        int mid = w / 2;
        p.setColor(intToColor(leftRGBA));
        p.fillRectangle(0, 0, mid, h);
        p.setColor(intToColor(rightRGBA));
        p.fillRectangle(mid, 0, w - mid, h);
        Texture t = new Texture(p);
        p.dispose();
        return t;
    }

    private static com.badlogic.gdx.graphics.Color intToColor(long rgba8888) {
        int i = (int) rgba8888;
        return new com.badlogic.gdx.graphics.Color(i);
    }

    private static void clickActor(Actor a) {
        // simulate a click by firing touchDown + touchUp
        InputEvent down = new InputEvent();
        down.setType(InputEvent.Type.touchDown);
        down.setButton(Input.Buttons.LEFT);
        down.setStageX(1); down.setStageY(1);
        a.fire(down);

        InputEvent up = new InputEvent();
        up.setType(InputEvent.Type.touchUp);
        up.setButton(Input.Buttons.LEFT);
        up.setStageX(1); up.setStageY(1);
        a.fire(up);

        // poke direct click listeners
        for (EventListener l : a.getListeners()) {
            if (l instanceof ClickListener cl) {
                cl.clicked(null, 1, 1);
            }
        }
    }

    private static TextButton findSubmit(Stage stage) {
        final AtomicReference<TextButton> ref = new AtomicReference<>();
        walk(stage.getRoot(), a -> {
            if (a instanceof TextButton b && "SUBMIT".equals(b.getText().toString())) {
                ref.set(b);
            }
        });
        return ref.get();
    }

    private static Label findLabelWithText(Stage stage, String text) {
        final AtomicReference<Label> ref = new AtomicReference<>();
        walk(stage.getRoot(), a -> {
            if (a instanceof Label l) {
                if (text.equals(l.getText().toString())) ref.set(l);
            }
        });
        return ref.get();
    }

    private static Table findGridTable(Stage stage) {
        // Table that’s a child of PixelPerfectPlacer and contains many Containers/Stacks
        final AtomicReference<Table> ref = new AtomicReference<>();
        walk(stage.getRoot(), a -> {
            if (a instanceof Table t && t.getParent() instanceof PixelPerfectPlacer) {
                if (containsTileLikeChildren(t)) ref.set(t);
            }
        });
        return ref.get();
    }

    private static boolean containsTileLikeChildren(Table t) {
        // tiles are Containers holding Stacks (image + border)
        var found = new boolean[]{false};
        walk(t, a -> {
            if (a instanceof Container<?> c && c.getActor() instanceof Stack) found[0] = true;
        });
        return found[0];
    }

    private static Actor findNthTile(Stage stage, int index) {
        // Collect tiles in render order; each tile is a Container<Stack>
        List<Actor> tiles = new ArrayList<>();
        walk(stage.getRoot(), a -> {
            if (a instanceof Container<?> c && c.getActor() instanceof Stack) {
                tiles.add(c);
            }
        });
        if (index < 0 || index >= tiles.size()) return null;
        return tiles.get(index);
    }

    private static void walk(Actor root, java.util.function.Consumer<Actor> fn) {
        fn.accept(root);
        if (root instanceof Group g) {
            for (Actor child : g.getChildren()) {
                walk(child, fn);
            }
        }
    }
}

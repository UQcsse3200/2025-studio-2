package com.csse3200.game.components.computerterminal;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TerminalUiComponentTest {
    private Stage stage;
    private RenderService renderSvc;
    private ResourceService resourceSvc;
    private Texture bgTex, blueTex, sheetTex;
    private MainGameScreen screen; // mock

    @BeforeEach
    void setUp() {
        if (Gdx.app == null) new HeadlessApplication(new ApplicationAdapter() {});

        ServiceLocator.clear();

        // Create the screen mock (was null before)
        screen = mock(MainGameScreen.class);

        // Register services and keep references on fields (no local shadowing)
        renderSvc = mock(RenderService.class);
        resourceSvc = mock(ResourceService.class);
        ServiceLocator.registerRenderService(renderSvc);
        ServiceLocator.registerResourceService(resourceSvc);

        // Tiny in-mem textures and assign to fields
        bgTex   = tinyTex();
        blueTex = tinyTex();
        sheetTex = tinyTex(); // <- used by the spritesheet spec

        when(resourceSvc.getAsset("images/terminal_bg.png",  Texture.class)).thenReturn(bgTex);
        when(resourceSvc.getAsset("images/terminal_bg_blue.png", Texture.class)).thenReturn(blueTex);
        when(resourceSvc.getAsset("test/sheet.png", Texture.class)).thenReturn(sheetTex); // <- important

        // Stage with mocked Batch (avoid shader compile) — assign to field
        Viewport vp = new ScalingViewport(Scaling.stretch, 800, 600, new OrthographicCamera());
        Batch batch = mock(Batch.class, RETURNS_DEEP_STUBS);
        stage = new Stage(vp, batch);
        when(renderSvc.getStage()).thenReturn(stage);

        doNothing().when(batch).begin();
        doNothing().when(batch).end();

    }

    @AfterEach
    void tearDown() {
        disposeQuietly(bgTex);
        disposeQuietly(blueTex);
        disposeQuietly(sheetTex);
        ServiceLocator.clear();
        if (stage != null) stage.dispose();
    }

    private static Texture tinyTex() {
        Pixmap pm = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static void disposeQuietly(Texture t) {
        if (t != null) t.dispose();
    }

    /** Build a component attached to an entity, and call create(). */
    private TerminalUiComponent buildComponent(SimpleCaptchaBank bank) {
        TerminalUiComponent ui = new TerminalUiComponent(screen).setCaptchaBank(bank);
        Entity e = new Entity().addComponent(ui);
        e.create();
        return ui;
    }

    /** Build a bank with ONE spritesheet spec so selection is deterministic. */
    private SimpleCaptchaBank oneSpecBank(int rows, int cols, Set<Integer> correct) {
        SimpleCaptchaBank bank = new SimpleCaptchaBank();
        bank.add(new SpritesheetSpec("test/sheet.png", rows, cols, correct, "pick the right ones"));
        return bank;
    }

    @Test
    @DisplayName("open -> pauses & shows, close -> unpauses & hides; isOpen() follows")
    void openCloseFlowAndIsOpen() {
        when(screen.isPaused()).thenReturn(false);

        // Component with a single 2x2 puzzle
        TerminalUiComponent ui = buildComponent(oneSpecBank(2, 2, Set.of(0)));

        // Simulate a terminal entity
        Entity terminal = new Entity();
        // Fire the “open” event (the component registered the listener on its own entity)
        ui.getEntity().getEvents().trigger("terminal:open", terminal);

        // Paused toggled + visible
        verify(screen).togglePaused();
        assertTrue(TerminalUiComponent.isOpen(), "terminalRoot should be visible after open");

        // Now close via event
        ui.getEntity().getEvents().trigger("terminal:close");
        // Unpause and not visible
        verify(screen, atLeastOnce()).togglePaused();
        assertFalse(TerminalUiComponent.isOpen(), "terminalRoot should be hidden after close");
    }

    @Test
    @DisplayName("Submit wrong selection -> shows error text; Submit correct -> VERIFIED and result event")
    void submitFailureAndSuccess() throws Exception {
        when(screen.isPaused()).thenReturn(false);

        var correct = Set.of(1, 3);
        TerminalUiComponent ui = buildComponent(oneSpecBank(2, 2, correct));

        final CaptchaResult[] captured = new CaptchaResult[1];
        Entity terminal = new Entity();
        terminal.getEvents().addListener("terminal:captchaResult", (CaptchaResult r) -> captured[0] = r);

        ui.getEntity().getEvents().trigger("terminal:open", terminal);
        assertTrue(TerminalUiComponent.isOpen());

        // Wrong: select only 0, then submit -> expect failure
        clickTileByIndex(stage, 0);
        clickSubmit(stage);
        assertTrue(findAnyLabelWithText(stage, "That's not quite right."));
        assertNotNull(captured[0]);
        assertFalse(captured[0].success());

        // Success path (Option A): directly set selection to the correct set, then submit
        java.lang.reflect.Field fSel = TerminalUiComponent.class.getDeclaredField("selected");
        fSel.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<Integer> selected = (Set<Integer>) fSel.get(ui);
        selected.clear();
        selected.addAll(correct);

        clickSubmit(stage);

        assertTrue(findAnyLabelWithExactText(stage, "VERIFIED."), "Expected VERIFIED after correct submission");
        assertNotNull(captured[0]);
        assertTrue(captured[0].success(), "Result should be success=true after correct submission");
        assertEquals(correct, captured[0].correct(), "Correct index set should be emitted");
    }

    @Test
    @DisplayName("Key handling: E closes; other keys are swallowed by capture listener")
    void keyCaptureBehavior() {
        when(screen.isPaused()).thenReturn(false);

        TerminalUiComponent ui = buildComponent(oneSpecBank(2, 2, Set.of(0)));
        Entity terminal = new Entity();
        ui.getEntity().getEvents().trigger("terminal:open", terminal);
        assertTrue(TerminalUiComponent.isOpen());

        // Non-E/Q should be consumed by the capture listener
        boolean handled = stage.keyDown(Input.Keys.Q);
        assertTrue(handled, "Non-E keys should be consumed by the terminal capture listener");
        assertTrue(TerminalUiComponent.isOpen());

        // E should bubble to root listener and close
        when(screen.isPaused()).thenReturn(true); // overlay set it paused; closing will unpause
        handled = stage.keyDown(Input.Keys.E);
        assertTrue(handled, "E is handled by the terminal to close it");
        assertFalse(TerminalUiComponent.isOpen());
    }

    // helpers

    private static boolean findAnyLabelWithText(Stage stage, String contains) {
        for (Actor a : stage.getActors()) {
            Label label = findLabelRecursive(a, contains, false);
            if (label != null) return true;
        }
        return false;
    }

    private static boolean findAnyLabelWithExactText(Stage stage, String text) {
        for (Actor a : stage.getActors()) {
            Label label = findLabelRecursive(a, text, true);
            if (label != null) return true;
        }
        return false;
    }

    private static Label findLabelRecursive(Actor root, String text, boolean exact) {
        if (root instanceof Label l) {
            String s = l.getText() == null ? "" : l.getText().toString();
            if (exact ? s.equals(text) : s.contains(text)) return l;
        }
        if (root instanceof Group g) {
            for (Actor c : g.getChildren()) {
                Label r = findLabelRecursive(c, text, exact);
                if (r != null) return r;
            }
        }
        return null;
    }

    /** Click the SUBMIT button by traversing the stage. */
    private static void clickSubmit(Stage stage) {
        TextButton btn = findSubmit(stage);
        assertNotNull(btn, "SUBMIT button not found");
        simulateClick(btn);
    }

    private static TextButton findSubmit(Stage stage) {
        for (Actor a : stage.getActors()) {
            TextButton b = findSubmitRecursive(a);
            if (b != null) return b;
        }
        return null;
    }

    private static TextButton findSubmitRecursive(Actor root) {
        if (root instanceof TextButton tb) {
            String s = tb.getText() == null ? "" : tb.getText().toString();
            if ("SUBMIT".equals(s)) return tb;
        }
        if (root instanceof Group g) {
            for (Actor c : g.getChildren()) {
                TextButton r = findSubmitRecursive(c);
                if (r != null) return r;
            }
        }
        return null;
    }

    /** Click a grid tile (by index in row-major order). */
    private static void clickTileByIndex(Stage stage, int index) {
        // Every tile is a Container<Stack> that has a ClickListener
        // just search for the Nth actor that has a ClickListener attached
        int seen = 0;
        for (Actor a : flatten(stage.getRoot())) {
            if (a.getListeners().size > 0 && containsClickListener(a)) {
                if (seen == index) {
                    simulateClick(a);
                    return;
                }
                seen++;
            }
        }
        fail("Tile actor for index " + index + " not found");
    }

    private static boolean containsClickListener(Actor a) {
        var ls = a.getListeners();
        for (int i = 0; i < ls.size; i++) if (ls.get(i) instanceof ClickListener) return true;
        return false;
    }

    private static java.util.List<Actor> flatten(Actor root) {
        java.util.ArrayList<Actor> out = new java.util.ArrayList<>();
        collect(root, out);
        return out;
    }

    private static void collect(Actor a, java.util.List<Actor> out) {
        out.add(a);
        if (a instanceof Group g) for (Actor c : g.getChildren()) collect(c, out);
    }

    /** Simulate a click: call touchDown/touchUp on input listeners that return handled=true. */
    private static void simulateClick(Actor actor) {
        float x = Math.max(1f, actor.getWidth() * 0.5f);
        float y = Math.max(1f, actor.getHeight() * 0.5f);

        InputEvent down = new InputEvent();
        down.setType(InputEvent.Type.touchDown);
        down.setListenerActor(actor);
        down.setTarget(actor);
        down.setPointer(0);
        down.setButton(0);
        down.setStageX(x);
        down.setStageY(y);

        InputEvent up = new InputEvent();
        up.setType(InputEvent.Type.touchUp);
        up.setListenerActor(actor);
        up.setTarget(actor);
        up.setPointer(0);
        up.setButton(0);
        up.setStageX(x);
        up.setStageY(y);

        var listeners = actor.getListeners();
        for (int i = 0; i < listeners.size; i++) {
            var l = listeners.get(i);
            if (l instanceof InputListener il) {
                boolean handled = il.touchDown(down, x, y, 0, 0);
                if (handled) il.touchUp(up, x, y, 0, 0);
            }
        }
    }

    /** Dispatch a keyDown to the root’s ClickListener (the one that closes on E). */
    private static boolean dispatchKeyDownToRoot(Stage stage, int keycode) {
        // The terminal root is named "terminalRoot"
        Actor root = stage.getRoot().findActor("terminalRoot");
        assertNotNull(root, "terminalRoot not found in stage");

        InputEvent ev = new InputEvent();
        ev.setType(InputEvent.Type.keyDown);
        ev.setKeyCode(keycode);
        ev.setListenerActor(root);
        ev.setTarget(root);

        // find the ClickListener attached in create()
        var listeners = root.getListeners();
        for (int i = 0; i < listeners.size; i++) {
            if (listeners.get(i) instanceof ClickListener cl) {
                return cl.keyDown(ev, keycode);
            }
        }
        fail("No ClickListener attached to terminalRoot");
        return false;
    }

    private static InputListener getOnlyCaptureListener(Stage stage) {
        var arr = stage.getRoot().getCaptureListeners();
        assertEquals(1, arr.size, "Expected exactly one stage capture listener");
        assertTrue(arr.get(0) instanceof InputListener);
        return (InputListener) arr.get(0);
    }
}


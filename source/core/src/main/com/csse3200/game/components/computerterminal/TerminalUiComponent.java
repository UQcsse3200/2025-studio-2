package com.csse3200.game.components.computerterminal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.PixelPerfectPlacer;
import com.csse3200.game.ui.UIComponent;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Overlay UI for a simple CAPTCHA-like puzzle shown on an in-world computer terminal.
 * Renders a header, prompt, grid of selectable tiles, an error/success line, and a submit button.
 * Uses sprite-sheet based puzzles (one big image split into equal tiles).
 */
public class TerminalUiComponent extends UIComponent {
    // Background panel texture and blue accent bar.
    private static final String BG_TEXTURE   = "images/terminal_bg.png";
    private static final String BLUE_TEXTURE = "images/terminal_bg_blue.png";

    // Overall canvas scale and grid sizing parameters.
    private static final float CANVAS_SCALE = 1.30f;
    private static final float GRID_SCALE   = 1.12f;
    private static final float GRID_GAP     = 2f;
    private static final float BORDER_PX    = 3f;
    private static final float IMAGE_OVERSCAN_PX = 0f;

    // Submit button size.
    private static final int SUBMIT_W = 110;
    private static final int SUBMIT_H = 28;

    // Spacing below grid and canvas padding.
    private static final int SUBMIT_BELOW_ERROR_MARGIN = 20;
    private static final int CANVAS_PAD_BOTTOM = 16;
    private static final int CANVAS_PAD_RIGHT  = 18;

    // Reduces blue header to show a thin white border.
    private static final int HEADER_INSET = 2;

    // True when puzzle already verified; disables grid input.
    private boolean verified = false;

    // Screen reference for pause/resume.
    private final MainGameScreen screen;

    // Provides available puzzle specs and random selection.
    private SimpleCaptchaBank captchaBank;

    // RNG for choosing a random puzzle.
    private final Random rng = new Random();

    // Stage and textures.
    private Texture bgTex, blueTex;

    // Root container that centers and scales the PixelPerfectPlacer.
    private Container<PixelPerfectPlacer> root;

    // Whether the overlay is currently visible.
    private boolean visible = false;

    // The terminal entity that opened this UI
    private Entity currentTerminal;

    // UI elements (prompt line, error/success line, grid table).
    private Label promptLabel;
    private Label errorLabel;
    private Table errorBox;
    private Table gridTable;

    // grid rectangle
    private int gridRectY;
    private int gridRectH;

    /** Currently active puzzle spec and player selections. */
    private CaptchaSpecLike currentSpecLike;
    private Set<Integer> selected = new HashSet<>();

    public TerminalUiComponent(MainGameScreen screen) {
        this.screen = screen;
    }

    /**
     * Injects the captcha bank that supplies puzzles.
     *
     * @param bank source of puzzle specs
     * @return this for chaining
     */
    public TerminalUiComponent setCaptchaBank(SimpleCaptchaBank bank) {
        this.captchaBank = bank;
        return this;
    }

    /**
     * Loads assets, builds the UI, and wires input/event listeners.
     */
    @Override
    public void create() {
        super.create();
        ResourceService resources = ServiceLocator.getResourceService();

        this.bgTex   = resources.getAsset(BG_TEXTURE, Texture.class);
        this.blueTex = resources.getAsset(BLUE_TEXTURE, Texture.class);

        root = build();
        root.setVisible(false);
        stage.addActor(root);

        entity.getEvents().addListener("terminal:open", this::openFromTerminal);
        entity.getEvents().addListener("terminal:close", this::close);
        entity.getEvents().addListener("terminal:toggle", (Entity term) -> {
            if (visible) close(); else openFromTerminal(term);
        });

        // Allow closing with E or ESC when overlay has keyboard focus
        // Allow closing with E when overlay has keyboard focus
        root.addListener(new ClickListener() {
            @Override public boolean keyDown(InputEvent event, int keycode) {
                if (!visible) return false;
                if (keycode == Input.Keys.E) {
                    close();
                    return true;      // consumed by terminal
                }
                return false;       // others are handled by the capture listener below
            }
        });

        stage.addCaptureListener(new InputListener() {
            public boolean sink(InputEvent event, int keycode) {
                if (!visible) return false;

                // Let E bubble to the root listener above (so it can close the terminal)
                if (keycode == Input.Keys.E || keycode == Input.Keys.ESCAPE) {
                    return false;  // do not consume
                }

                // Swallow everything else: ESC, Q, etc. so pause/HUD never see them
                event.stop();
                return true;     // consumed
            }

            @Override public boolean keyDown(InputEvent event, int keycode) {
                return sink(event, keycode);
            }

            @Override public boolean keyUp(InputEvent event, int keycode) {
                return sink(event, keycode);
            }
        });
    }

    public static boolean isOpen() {
        try {
            RenderService rs = ServiceLocator.getRenderService();
            if (rs == null) return false;
            var stage = rs.getStage();
            if (stage == null) return false;
            Actor a = stage.getRoot().findActor("terminalRoot");
            return a != null && a.isVisible();
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Builds the fixed-layout canvas using a PixelPerfectPlacer and places header,
     * prompt, grid area, error line, and submit button.
     */
    private Container<PixelPerfectPlacer> build() {
        PixelPerfectPlacer placer = new PixelPerfectPlacer(bgTex);

        // Header (blue strip with black title)
        Table header = new Table();
        header.setBackground(new TextureRegionDrawable(new TextureRegion(blueTex)));
        Label title = new Label("NON BOT VERIFICATION", skin, "large");
        title.setColor(Color.BLACK);
        title.setAlignment(Align.center);
        header.add(title).growX().pad(8f);
        int hW = bgTex.getWidth() - 2 * HEADER_INSET;
        int hH = 68 - HEADER_INSET;
        placer.addOverlay(header, new PixelPerfectPlacer.Rect(HEADER_INSET, HEADER_INSET, hW, hH));

        // Prompt (wrap to two lines, left-aligned)
        promptLabel = new Label("", skin);
        promptLabel.setColor(Color.BLACK);
        promptLabel.setWrap(true);
        promptLabel.setAlignment(Align.left);
        Table promptBox = new Table();
        final int pX = 8, pY = 70, pW = bgTex.getWidth() - 16, pH = 56;
        promptBox.add(promptLabel).left().top().width(pW).expandY().fillY().pad(3f);
        placer.addOverlay(promptBox, new PixelPerfectPlacer.Rect(pX, pY, pW, pH));

        // Grid rect (square: width governs height; then scaled with GRID_SCALE)
        final int baseGX = 24;
        final int baseGY = 140;
        final int baseGW = bgTex.getWidth() - 48;
        int baseGH = (int) (bgTex.getHeight() * 0.55f);
        baseGH = baseGW; // enforce square grid

        PixelPerfectPlacer.Rect gridRect = scaleRectAroundCenter(
                baseGX, baseGY, baseGW, baseGH, GRID_SCALE, bgTex.getWidth(), bgTex.getHeight());

        gridTable = new Table();
        placer.addOverlay(gridTable, gridRect);

        gridRectY = gridRect.y();
        gridRectH = gridRect.height();

        // Error / status line (wrap, left/top)
        errorLabel = new Label("", skin);
        errorLabel.setColor(Color.BLACK);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.left | Align.top);

        errorBox = new Table();
        final int errX = -30;                       // slight left shift to align visually with prompt
        final int errY = gridRectY + gridRectH + 25;
        final int errH = 15;
        final int errW = bgTex.getWidth() - 14;

        errorBox.add(errorLabel).left().top().width(errW).pad(0);
        placer.addOverlay(errorBox, new PixelPerfectPlacer.Rect(errX, errY, errW, errH));

        // Submit (right-aligned, under error line; clamped to panel bottom)
        TextureRegionDrawable blue = new TextureRegionDrawable(new TextureRegion(blueTex));
        TextButton submitButton = new TextButton(
                "SUBMIT",
                new TextButton.TextButtonStyle(blue, blue, blue, skin.get(Label.LabelStyle.class).font)
        );
        submitButton.getLabel().setColor(Color.BLACK);
        submitButton.getLabel().setAlignment(Align.center);
        submitButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { onSubmit(); }
        });

        int sW = SUBMIT_W, sH = SUBMIT_H;
        int sX = bgTex.getWidth() - CANVAS_PAD_RIGHT - sW;
        int sY = errY + errH + SUBMIT_BELOW_ERROR_MARGIN;
        sY = Math.min(sY, bgTex.getHeight() - CANVAS_PAD_BOTTOM - sH);
        placer.addOverlay(submitButton, new PixelPerfectPlacer.Rect(sX, sY, sW, sH));

        // Center the canvas and scale it to fit the screen
        Container<PixelPerfectPlacer> centered = new Container<>(placer);
        float screenW = Gdx.graphics.getWidth(), screenH = Gdx.graphics.getHeight();
        float canvasH = screenH * (2f / 3f);
        float aspect  = (float) bgTex.getWidth() / bgTex.getHeight();
        float canvasW = canvasH * aspect;
        canvasW *= CANVAS_SCALE; canvasH *= CANVAS_SCALE;
        float maxW = screenW * 0.96f, maxH = screenH * 0.96f;
        if (canvasW > maxW) { float k = maxW / canvasW; canvasW *= k; canvasH *= k; }
        if (canvasH > maxH) { float k = maxH / canvasH; canvasW *= k; canvasH *= k; }

        centered.size(canvasW, canvasH);
        centered.align(Align.center);
        centered.setFillParent(true);
        centered.setName("terminalRoot");
        return centered;
    }

    /**
     * Picks a random puzzle spec, resets state and rebuilds the grid.
     */
    private void rebuildForRandomPuzzle() {
        if (captchaBank == null) throw new IllegalStateException("CaptchaBank not injected");
        selected.clear();
        verified = false;
        errorLabel.setText("");
        gridTable.setTouchable(Touchable.enabled);

        currentSpecLike = captchaBank.random(rng);
        promptLabel.setText(currentSpecLike.prompt());

        gridTable.clearChildren();
        buildGridFromSpritesheet((SpritesheetSpec) currentSpecLike);

        gridTable.invalidateHierarchy();
    }

    /**
     * Splits a single spritesheet into equal tiles and lays them out in the grid.
     *
     * @param spec spritesheet path and grid dimensions
     */
    private void buildGridFromSpritesheet(SpritesheetSpec spec) {
        ResourceService res = ServiceLocator.getResourceService();
        Texture tex = res.getAsset(spec.texturePath(), Texture.class);
        if (tex == null) {
            errorLabel.setText("Missing spritesheet: " + spec.texturePath());
        return;
        }

        int rows = spec.rows(), cols = spec.cols();
        int tileW = tex.getWidth() / cols;
        int tileH = tex.getHeight() / rows;
        TextureRegion[][] split = TextureRegion.split(tex, tileW, tileH);

        gridTable.clear();
        gridTable.defaults().pad(GRID_GAP).expand().fill();

        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                TextureRegion region = split[r][c];
                final int index = idx++;
                gridTable.add(makeSelectableTile(region, index)).expand().fill();
            }
            gridTable.row();
        }
    }

    /**
     * Creates one selectable tile (image + selection border). Clicking toggles selection.
     *
     * @param region sprite region for this tile
     * @param index  index used to compare against the correct set
     * @return an Actor added to the grid table
     */
    private Actor makeSelectableTile(TextureRegion region, int index) {
        Image img = new Image(new TextureRegionDrawable(region));
        img.setScaling(Scaling.fit);
        img.setAlign(Align.center);

        Container<Image> imgWrap = new Container<>(img);
        imgWrap.fill();
        imgWrap.pad(-IMAGE_OVERSCAN_PX);

        TextureRegionDrawable blue = new TextureRegionDrawable(new TextureRegion(blueTex));
        Image top = new Image(blue), bottom = new Image(blue),
                left = new Image(blue), right = new Image(blue);

        Table frame = new Table();
        frame.setTouchable(null);
        frame.setVisible(false);
        frame.top();
        frame.add(top).colspan(3).growX().height(BORDER_PX);
        frame.row();
        frame.add(left).width(BORDER_PX).growY();
        frame.add().grow();
        frame.add(right).width(BORDER_PX).growY();
        frame.row();
        frame.add(bottom).colspan(3).growX().height(BORDER_PX);

        Stack stack = new Stack();
        stack.add(imgWrap);
        stack.add(frame);

        stack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (verified) return;                 // <— ignore clicks after verify
                boolean now = !selected.contains(index);
                if (now) selected.add(index); else selected.remove(index);
                frame.setVisible(now);
            }
        });

        Container<Stack> cell = new Container<>(stack);
        cell.fill();
        return cell;
    }

    /**
     * Scales a rectangle about its center within background bounds.
     */
    private PixelPerfectPlacer.Rect scaleRectAroundCenter(
            int x, int y, int w, int h, float scale, int bgW, int bgH) {
        if (scale == 1f) return new PixelPerfectPlacer.Rect(x, y, w, h);
        float cx = x + w / 2f, cy = y + h / 2f;
        int nw = Math.max(1, Math.round(w * scale));
        int nh = Math.max(1, Math.round(h * scale));
        int nx = Math.round(cx - nw / 2f);
        int ny = Math.round(cy - nh / 2f);
        nx = Math.max(0, Math.min(nx, bgW - nw));
        ny = Math.max(0, Math.min(ny, bgH - nh));
        return new PixelPerfectPlacer.Rect(nx, ny, nw, nh);
    }

    /**
     * Validates the current selection. On failure, shows a shake and error text.
     * On success, disables the grid, shows VERIFIED, and emits a result event.
     */
    private void onSubmit() {
        boolean ok = selected.equals(currentSpecLike.correct());

        // Emit one event containing the result + selections
        CaptchaResult result = new CaptchaResult(ok, Set.copyOf(selected), currentSpecLike.correct());
        if (currentTerminal != null) {
            currentTerminal.getEvents().trigger("terminal:captchaResult", result);
        }

        if (!ok) {
            errorLabel.setText("That's not quite right. Try again.");
            errorBox.clearActions();
            errorBox.addAction(Actions.sequence(
                    Actions.moveBy(-6f, 0f, 0.04f),
                    Actions.moveBy(12f, 0f, 0.04f),
                    Actions.moveBy(-12f, 0f, 0.04f),
                    Actions.moveBy(12f, 0f, 0.04f),
                    Actions.moveBy(-6f, 0f, 0.04f)
            ));
            return;
        }

        // Success: lock the grid and show VERIFIED
        verified = true;
        gridTable.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        errorLabel.setText("VERIFIED.");
    }

    /**
     * Opens the overlay for a given terminal. Pauses game and rebuilds a random puzzle.
     *
     * @param terminal the terminal entity initiating the open
     */
    private void openFromTerminal(Entity terminal) {
        if (visible) {
            return;
        }
        if (!screen.isPaused()) {
            screen.togglePaused();
        }

        verified = false;
        currentTerminal = terminal;
        visible = true;
        root.setVisible(true);
        stage.setKeyboardFocus(root);
        stage.setScrollFocus(root);

        rebuildForRandomPuzzle();
    }

    /**
     * Hides the overlay and resumes the game.
     */
    private void close() {
        if (!visible) return;

        root.setVisible(false);
        visible = false;

        if (screen.isPaused()) screen.togglePaused();

        if (currentTerminal != null) {
            currentTerminal.getEvents().trigger("terminal:closed");
            currentTerminal = null;
        }

        if (stage.getKeyboardFocus() == root) stage.setKeyboardFocus(null);
        if (stage.getScrollFocus() == root) stage.setScrollFocus(null);
    }

    @Override protected void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) { }
    @Override public void dispose() {
        if (root != null) root.remove();
        super.dispose();
    }
}

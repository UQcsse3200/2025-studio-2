package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Centre-screen arrow that points toward the last damage source, then fades out.
 * Robust to missing/preload timing: tries ResourceService first, then falls back to a direct load.
 * Never captures input, avoids duplicate HUD on respawn, and rate-limits refresh to prevent blinking.
 */
public class DamageIndicatorUI extends UIComponent {
    // --- KILL SWITCH: set true to disable this UI everywhere ---
    private static final boolean DISABLED = true;
// -----------------------------------------------------------

    private static final String ASSET_PATH = "images/damage_arrow.png";

    private static final float SHOW_TIME  = 1.35f; // seconds arrow stays visible
    private static final float MIN_ALPHA  = 0.25f; // end alpha
    private static final float MAX_ALPHA  = 0.95f; // start alpha
    private static final float COOLDOWN   = 0.05f; // seconds between UI refreshes
    private static final float ARROW_SIZE = 48f;   // px

    private Image arrow;
    private Table container;
    private final Vector2 lastDir = new Vector2(1, 0);

    private float timer    = 0f;
    private float cooldown = 0f;

    // Only used if we create a texture ourselves (not via ResourceService)
    private boolean ownsTexture = false;
    private Texture ownedTexture;

    @Override
    public void create() {
        super.create();
        if (DISABLED) return;


        // 1) Try to get the texture from the ResourceService (may throw if not preloaded)
        var resources = ServiceLocator.getResourceService();
        Texture tex = null;
        try {
            tex = resources.getAsset(ASSET_PATH, Texture.class);
        } catch (Exception ignored) {
            tex = null;
        }

        // 2) If not available, enqueue and block until it's loaded, then get it
        if (tex == null) {
            try {
                resources.loadTextures(new String[]{ASSET_PATH});
            } catch (Exception ignored) { /* some implementations never throw here */ }

            boolean loaded = false;
            try {
                // Preferred path if your service supports it
                resources.loadAll();
                tex = resources.getAsset(ASSET_PATH, Texture.class);
            } catch (Throwable t) {
                // Fallback: step the loader a little and keep trying to fetch the texture
                // (cap ~1s total so we don't hang if something goes wrong)
                for (int i = 0; i < 200 && tex == null; i++) {
                    try { resources.loadForMillis(5); } catch (Throwable ignore) {}
                    try { tex = resources.getAsset(ASSET_PATH, Texture.class); } catch (Exception ignore) {}
                }
            }


            if (loaded) {
                try {
                    tex = resources.getAsset(ASSET_PATH, Texture.class);
                } catch (Exception ignored) { tex = null; }
            }
        }

        // 3) Final fallback: direct load from assets, or a tiny generated arrow if the file is missing
        if (tex == null) {
            if (Gdx.files.internal(ASSET_PATH).exists()) {
                tex = new Texture(Gdx.files.internal(ASSET_PATH));
                ownsTexture = true;
                ownedTexture = tex;
            } else {
                // Built-in 32x32 red arrow so we never crash even if the asset is missing
                Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
                pm.setColor(1, 0, 0, 1);
                pm.fillTriangle(26, 16, 12, 8, 12, 24);
                pm.fillRectangle(8, 12, 18, 8);
                tex = new Texture(pm);
                pm.dispose();
                ownsTexture = true;
                ownedTexture = tex;
                Gdx.app.error("DamageIndicatorUI",
                        "Missing " + ASSET_PATH + " – using built-in fallback sprite");
            }
        }

        // 4) Build the UI actor
        arrow = new Image(tex);
        arrow.setVisible(false);
        arrow.setSize(ARROW_SIZE, ARROW_SIZE);
        arrow.setOrigin(ARROW_SIZE * 0.5f, ARROW_SIZE * 0.5f);
        arrow.setColor(1f, 0.25f, 0.25f, 0f);

        // Add once to Stage; don't capture input
        Stage stage = ServiceLocator.getRenderService().getStage();
        var root = stage.getRoot();
        if (root.findActor("DamageIndicatorUI") == null) {
            container = new Table();
            container.setName("DamageIndicatorUI");
            container.setFillParent(true);
            container.setTouchable(Touchable.disabled);
            container.add(arrow).expand().center();
            stage.addActor(container);
        } else {
            container = (Table) root.findActor("DamageIndicatorUI");
        }

        // Listen for direction vectors from damage events
        entity.getEvents().addListener("damageDirection", this::onDamageDirection);
    }

    private void onDamageDirection(Vector2 dir) {
        if (DISABLED) return;

        if (dir == null || dir.isZero(1e-6f) || Float.isNaN(dir.x) || Float.isNaN(dir.y)) return;
        if (cooldown > 0f) return;     // simple rate-limit to prevent "blinking"

        cooldown = COOLDOWN;
        lastDir.set(dir).nor();
        timer = SHOW_TIME;

        arrow.setVisible(true);
        arrow.setRotation(lastDir.angleDeg());
        arrow.setColor(1f, 0.25f, 0.25f, MAX_ALPHA);
    }

    @Override
    public void update() {
        if (DISABLED) return;

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        if (cooldown > 0f) cooldown -= dt;

        if (!arrow.isVisible()) return;

        timer -= dt;
        if (timer <= 0f) {
            arrow.setVisible(false);
            arrow.setColor(1f, 0.25f, 0.25f, 0f);
            return;
        }

        arrow.setRotation(lastDir.angleDeg());

        float t = 1f - (timer / SHOW_TIME);
        float alpha = MAX_ALPHA - t * (MAX_ALPHA - MIN_ALPHA);
        arrow.setColor(1f, 0.25f, 0.25f, alpha);
    }

    /** Stage is drawn by the engine's RenderService. */
    @Override public void draw(SpriteBatch batch) { /* no-op */ }

    @Override
    public void dispose() {
        super.dispose();
        if (DISABLED) return;
        if (container != null) container.remove();
        if (ownsTexture && ownedTexture != null) {
            ownedTexture.dispose();
            ownedTexture = null;
            ownsTexture = false;
        }
    }
}

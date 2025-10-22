package com.csse3200.game.ui.achievements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.achievements.AchievementId;
import com.csse3200.game.achievements.AchievementService;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class AchievementToastUI extends Component implements AchievementService.Listener {
    private Stage stage;
    private Skin skin;

    // --- trophy asset (lazy-loaded) ---
    private Texture trophyTex;
    private Drawable trophyDrawable;

    @Override
    public void create() {
        AchievementService.get().addListener(this);
        initStageAndSkin();
        Gdx.app.postRunnable(this::initStageAndSkin);
    }

    private void initStageAndSkin() {
        if (stage == null) {
            try { stage = ServiceLocator.getRenderService().getStage(); } catch (Exception ignored) {}
        }
        if (skin == null) {
            try {
                skin = new Skin(Gdx.files.internal("uiskin.json"));
            } catch (Exception ignored) {
                skin = createFallbackSkin();
            }
        }
    }

    private Skin createFallbackSkin() {
        Skin s = new Skin();
        BitmapFont font = new BitmapFont();
        s.add("default-font", font, BitmapFont.class);
        s.add("default", new Label.LabelStyle(font, Color.WHITE));

        Pixmap pm = new Pixmap(4, 4, Format.RGBA8888);
        pm.setColor(0, 0, 0, 0.75f);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        s.add("toast-bg", new TextureRegionDrawable(new TextureRegion(tex)), Drawable.class);
        return s;
    }

    private void ensureTrophyLoaded() {
        if (trophyDrawable != null) return;
        try {
            trophyTex = new Texture(Gdx.files.internal("images/achievements/trophy.png"));
            trophyDrawable = new TextureRegionDrawable(new TextureRegion(trophyTex));
        } catch (Exception e) {
            Gdx.app.error("AchvToast", "Failed to load trophy icon", e);
        }
    }

    @Override
    public void onUnlocked(AchievementId id, String title, String desc) {
        Gdx.app.postRunnable(() -> {
            initStageAndSkin();
            if (stage == null || skin == null) {
                Gdx.app.log("Achv", "ToastUI: stage/skin not ready, skip toast");
                return;
            }
            showToast(id, "🏅 " + title, desc);
        });
    }

    private void showToast(AchievementId id, String title, String desc) {
        // Outer container
        Table toast = new Table(skin);
        if (skin.has(TOAST_BG, Drawable.class)) {
            toast.setBackground(skin.getDrawable(TOAST_BG));
        }
        toast.pad(10f).defaults().left().padBottom(4f);

        // Optional left icon (only for sprint/adrenaline)
        if (id == AchievementId.ADRENALINE_RUSH) {
            ensureTrophyLoaded();
            if (trophyDrawable != null) {
                Image icon = new Image(trophyDrawable);
                toast.add(icon).size(100, 100).padRight(10f).top();
            }
        }

        // Right text column
        Table text = new Table(skin);
        Label titleLbl = new Label(title, skin);
        titleLbl.setColor(Color.GOLD);
        Label descLbl = new Label(desc, skin);
        descLbl.setWrap(true);

        text.add(titleLbl).left().row();
        text.add(descLbl)
                .width(Math.min(420f, stage.getViewport().getWorldWidth() * 0.6f))
                .left();

        toast.add(text).left().row();
        toast.pack();

        // Top-right placement
        float margin = 20f;
        float x = stage.getViewport().getWorldWidth() - toast.getWidth() - margin;
        float y = stage.getViewport().getWorldHeight() - toast.getHeight() - margin;
        toast.setPosition(x, y);

        // Fade in → delay → fade out (same timings)
        toast.getColor().a = 0f;
        stage.addActor(toast);
        toast.setZIndex(Integer.MAX_VALUE);
        toast.addAction(Actions.sequence(
                Actions.fadeIn(0.25f),
                Actions.delay(4.0f),
                Actions.fadeOut(0.25f),
                Actions.removeActor()
        ));
    }

    @Override
    public void dispose() {
        AchievementService.get().removeListener(this);
        if (trophyTex != null) {
            trophyTex.dispose();
            trophyTex = null;
            trophyDrawable = null;
        }
    }
}



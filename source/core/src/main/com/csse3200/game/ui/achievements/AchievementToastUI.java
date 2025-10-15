package com.csse3200.game.ui.achievements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.achievements.AchievementId;
import com.csse3200.game.achievements.AchievementService;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class AchievementToastUI extends Component implements AchievementService.Listener {
    private Stage stage;
    private Skin skin;

    @Override
    public void create() {
        // Register to receive achievement events
        AchievementService.get().addListener(this);

        // Try immediately, but also re-try next frame if stage/skin aren't ready yet.
        initStageAndSkin();
        Gdx.app.postRunnable(this::initStageAndSkin);
    }

    private void initStageAndSkin() {
        if (stage == null) {
            try {
                stage = ServiceLocator.getRenderService().getStage();
            } catch (Exception ignored) {}
        }
        if (skin == null) {
            // Adjust to your real skin path if you have one. It’s fine if this fails – we fall back.
            try {
                skin = new Skin(Gdx.files.internal("uiskin.json"));
            } catch (Exception ignored) {
                skin = createFallbackSkin();
            }
        }
    }

    private Skin createFallbackSkin() {
        Skin s = new Skin();
        BitmapFont font = new BitmapFont(); // default font
        s.add("default-font", font, BitmapFont.class);
        s.add("default", new Label.LabelStyle(font, Color.WHITE));

        // simple rounded-ish dark background
        Pixmap pm = new Pixmap(4, 4, Format.RGBA8888);
        pm.setColor(0, 0, 0, 0.75f);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        Drawable bg = new TextureRegionDrawable(new TextureRegion(tex));
        s.add("toast-bg", bg, Drawable.class);

        return s;
    }

    @Override
    public void onUnlocked(AchievementId id, String title, String desc) {
        // Make sure we’re on the GL thread and stage/skin are ready
        Gdx.app.postRunnable(() -> {
            initStageAndSkin();
            if (stage == null || skin == null) {
                Gdx.app.log("Achv", "ToastUI: stage/skin not ready, skip toast");
                return;
            }
            showToast("🏅 " + title, desc);
        });
    }

    private void showToast(String title, String desc) {
        // Build toast content
        Table toast = new Table(skin);
        if (skin.has("toast-bg", Drawable.class)) {
            toast.setBackground(skin.getDrawable("toast-bg"));
        }

        Label titleLbl = new Label(title, skin);
        titleLbl.setColor(Color.GOLD);
        Label descLbl = new Label(desc, skin);
        descLbl.setWrap(true);

        toast.pad(10f).defaults().left().padBottom(4f);
        toast.add(titleLbl).row();
        toast.add(descLbl).width(Math.min(420f, stage.getViewport().getWorldWidth() * 0.6f)).row();

        toast.pack();

        // Place top-right with 20px margin
        float margin = 20f;
        float x = stage.getViewport().getWorldWidth() - toast.getWidth() - margin;
        float y = stage.getViewport().getWorldHeight() - toast.getHeight() - margin;
        toast.setPosition(x, y);

        // Start invisible, then animate
        toast.getColor().a = 0f;
        stage.addActor(toast);
        toast.setZIndex(Integer.MAX_VALUE); // ensure on top

        toast.addAction(Actions.sequence(
                Actions.fadeIn(0.25f),
                Actions.delay(2.0f),
                Actions.fadeOut(0.25f),
                Actions.removeActor()
        ));
    }

    @Override
    public void dispose() {
        AchievementService.get().removeListener(this);
    }
}


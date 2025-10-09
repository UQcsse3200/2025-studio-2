package com.csse3200.game.ui.achievements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import com.csse3200.game.achievements.AchievementId;
import com.csse3200.game.achievements.AchievementService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

public class AchievementsMenuUI extends UIComponent {
    private Window root;
    private Table list;
    private ScrollPane scroller;

    @Override
    public void create() {
        super.create();

        Stage stage = ServiceLocator.getRenderService().getStage();

        root = new Window("Achievements", skin);
        root.setMovable(true);
        root.setResizable(false);

        list = new Table(skin);
        list.defaults().left().pad(6f);

        scroller = new ScrollPane(list, skin);
        scroller.setFadeScrollBars(false);

        Table content = new Table(skin);
        content.add(new Label("Achievements", skin, "title")).left().padBottom(10f).row();
        content.add(scroller).width(520f).height(360f);

        root.add(content).pad(16f);
        root.pack();

        // center
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();
        root.setPosition((w - root.getWidth()) / 2f, (h - root.getHeight()) / 2f);

        stage.addActor(root);
        root.setVisible(false);

        // 🔁 Auto-refresh whenever an achievement unlocks
        AchievementService.get().addListener((id, title, desc) -> {
            Gdx.app.log("AchvMenu", "Unlocked event for " + id + " – refreshing menu");
            if (root.isVisible()) {
                refresh();
            }
        });

        // Build once so the first open is populated
        refresh();
    }

    public void toggle() {
        boolean show = !root.isVisible();
        root.setVisible(show);
        if (show) {
            refresh();
            root.toFront();
        }
    }

    private void refresh() {
        list.clear();
        // Optional: debug what the service thinks
        Gdx.app.log("AchvMenu", "Refreshing. Current unlocked:");
        for (AchievementId id : AchievementId.values()) {
            boolean unlocked = AchievementService.get().isUnlocked(id);
            Gdx.app.log("AchvMenu", " - " + id + " : " + unlocked);

            String line = (unlocked ? "✔ " : "□ ") + pretty(id);
            list.add(new Label(line, skin)).left().row();
        }
        list.invalidateHierarchy();
        scroller.layout();
    }

    private String pretty(AchievementId id) {
        switch (id) {
            case LEVEL_1_COMPLETE:  return "Level One Hero – Finish Level 1";
            case LEVEL_2_COMPLETE:  return "Level Two Conqueror – Finish Level 2";
            case ADRENALINE_RUSH:  return "Adrenaline Rush – Sprint for 30s total";
            case STAMINA_MASTER:   return "Stamina Master – Finish a level without exhausting stamina";
            default:               return id.name();
        }
    }

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            toggle();
        }
        // Convenience: force-refresh with R while menu is open
        if (root.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            refresh();
        }
    }

    @Override public void draw(SpriteBatch batch) {}
    @Override public void dispose() { if (root != null) root.remove(); super.dispose(); }
}

package com.csse3200.game.ui.achievements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.graphics.Color;                 // ← new
import com.csse3200.game.achievements.AchievementId;
import com.csse3200.game.achievements.AchievementService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

public class AchievementsMenuUI extends UIComponent {
    private Window root;
    private Table list;
    private ScrollPane scroller;
    private Label.LabelStyle rowStyle;

    private static final String ACHV_MENU = "AchvMenu";

    @Override
    public void create() {
        super.create();
        Label.LabelStyle base = skin.get(Label.LabelStyle.class); // the default label style
        rowStyle = new Label.LabelStyle(base);
        rowStyle.fontColor = com.badlogic.gdx.graphics.Color.BLACK;
        Stage stage = ServiceLocator.getRenderService().getStage();
        float vw = stage.getViewport().getWorldWidth();
        float vh = stage.getViewport().getWorldHeight();

        // Window
        root = new Window("Achievements", skin);
        root.setMovable(true);
        root.setResizable(false);

        // List that will contain rows
        list = new Table(skin);
        list.defaults().left().pad(6.0f);

        // Bigger content area – target ~70% width, ~55% height of the viewport
        float bodyW = Math.min(vw * 0.7f, 900f);
        float bodyH = Math.min(vh * 0.55f, 560f);

        scroller = new ScrollPane(list, skin);
        scroller.setFadeScrollBars(false);
        scroller.setScrollingDisabled(true, true); // effectively no scroll for our bigger body

        Table content = new Table(skin);
        content.defaults().left().padBottom(8f);
        content.add(new Label("[Achievements]", skin, "title")).left().row();
        content.add(scroller).width(bodyW).height(bodyH);

        root.add(content).pad(16.0f);
        root.pack();

        // Center on screen
        root.setPosition((vw - root.getWidth()) / 2f, (vh - root.getHeight()) / 2f);
        stage.addActor(root);
        root.setVisible(false);

        // Auto-refresh when an achievement unlocks
        AchievementService.get().addListener((id, title, desc) -> {
            if (root.isVisible()) refresh(bodyW);
        });

        refresh(bodyW); // initial build
    }

    public void toggle() {
        Stage stage = ServiceLocator.getRenderService().getStage();
        float bodyW = Math.min(stage.getViewport().getWorldWidth() * 0.7f, 900f);
        boolean show = !root.isVisible();
        root.setVisible(show);
        if (show) {
            refresh(bodyW);
            root.toFront();
        }
    }

    /** Rebuild the list showing only unlocked achievements. */
    private void refresh(float bodyW) {
        list.clear();

        int shown = 0;
        for (AchievementId id : AchievementId.values()) {
            if (!AchievementService.get().isUnlocked(id)) continue; // 🔎 show only unlocked
            Label line = new Label("• " + pretty(id), rowStyle);
            line.setWrap(true);
            list.add(line).width(bodyW - 40f).left().row(); // wide rows, wrap long text
            shown++;
        }

        if (shown == 0) {
            Label none = new Label("No achievements unlocked yet.", rowStyle);
            list.add(none).left().row();
        }

        list.invalidateHierarchy();
        scroller.layout();
    }

    private String pretty(AchievementId id) {
        switch (id) {
            case LEVEL_1_COMPLETE: return "Level One Hero  –  Finish Level 1";
            case LEVEL_2_COMPLETE: return "Level Two Conqueror  –  Finish Level 2";
            case ADRENALINE_RUSH:  return "Adrenaline Rush  –  Sprint for 3s total";
            case STAMINA_MASTER:   return "Stamina Master  –  Finish a level without exhausting stamina";
            default: return id.name();
        }
    }

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            toggle();
        }
    }

    @Override public void draw(SpriteBatch batch) {
        // Handled by the renderer
    }
    @Override public void dispose() {
        if (null != root) root.remove();
        super.dispose();
    }
}

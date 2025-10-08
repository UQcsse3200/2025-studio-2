package com.csse3200.game.ui.achievements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.achievements.AchievementId;
import com.csse3200.game.achievements.AchievementService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

public class AchievementsMenuUI extends UIComponent {
    private Table root;

    @Override
    public void create() {
        super.create();
        Stage stage = ServiceLocator.getRenderService().getStage();

        root = new Table(skin);
        root.setFillParent(true);
        root.pad(30f);
        root.defaults().left().pad(6f);
        root.add(new Label("Achievements", skin, "title")).left().row();

        for (AchievementId id : AchievementId.values()) {
            boolean done = AchievementService.get().isUnlocked(id);
            String line = (done ? "✔ " : "□ ") + pretty(id);
            root.add(new Label(line, skin)).left().row();
        }

        stage.addActor(root);
        root.setVisible(false);
    }

    public void toggle() { root.setVisible(!root.isVisible()); }

    private String pretty(AchievementId id) {
        switch (id) {
            case FIRST_STEPS: return "First Steps – Complete the tutorial";
            case LEVEL1_COMPLETE: return "Level One Hero – Finish Level 1";
            case LEVEL2_COMPLETE: return "Level Two Conqueror – Finish Level 2";
            case BOSS_SLAYER: return "Boss Slayer – Defeat the boss";
            case ADRENALINE_RUSH: return "Adrenaline Rush – Sprint for 30s total";
            case STAMINA_MASTER: return "Stamina Master – Finish level without exhausting stamina";
            default: return id.name();
        }
    }

    @Override public void draw(SpriteBatch batch) {}
    @Override public void dispose() { if (root != null) root.remove(); super.dispose(); }
}

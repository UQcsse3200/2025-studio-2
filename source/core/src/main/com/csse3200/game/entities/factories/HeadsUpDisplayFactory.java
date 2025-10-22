package com.csse3200.game.entities.factories;

import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.achievements.AchievementToastUI;
import com.csse3200.game.ui.achievements.AchievementsMenuUI;

/**
 * Factory to create HUD entity.
 */
public class HeadsUpDisplayFactory {

    private HeadsUpDisplayFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    public static Entity createHeadsUpDisplay(String name) {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay(name));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        ui.addComponent(new AchievementToastUI());
        ui.addComponent(new AchievementsMenuUI());
        return ui;
    }
}

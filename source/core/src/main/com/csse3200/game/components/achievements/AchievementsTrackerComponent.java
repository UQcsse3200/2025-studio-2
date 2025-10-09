package com.csse3200.game.components.achievements;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.achievements.AchievementService;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.StaminaComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Periodically feeds gameplay signals to the AchievementService.
 * - Adds sprinting delta-time while the player's StaminaComponent reports sprinting.
 * - Marks stamina exhaustion when the player fires the "exhausted" event.
 * - Resets per-level flags on "levelStarted".
 * - (Optional) Call AchievementService.get().onLevelCompleted(levelNum) from
 *   the level code when you know the level number.
 */
public class AchievementsTrackerComponent extends Component {
    private StaminaComponent stamina;

    @Override
    public void create() {
        super.create();
        stamina = entity.getComponent(StaminaComponent.class);

        // Hooks from player/gameplay
        entity.getEvents().addListener("exhausted", this::onExhausted);
        entity.getEvents().addListener("levelStarted", this::onLevelStarted);
        entity.getEvents().addListener("levelCompleted", this::onLevelCompleted);
    }

    private void onExhausted() {
        AchievementService.get().markStaminaExhausted();
    }

    private void onLevelStarted() {
        AchievementService.get().onLevelStarted();
    }

    // If your level code knows the level number, call the service from there instead.
    private void onLevelCompleted() {
        // e.g., AchievementService.get().onLevelCompleted(1);
    }

    @Override
    public void update() {
        // Keep the service's internal autosave ticking.
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        AchievementService.get().update(dt);

        // In case create() ran before StaminaComponent was attached, try again.
        if (stamina == null) {
            stamina = entity.getComponent(StaminaComponent.class);
        }

        // Count sprint time when stamina says we're sprinting.
        if (stamina != null && stamina.isSprinting()) {
            AchievementService.get().addSprintTime(dt);
            Gdx.app.log("Achv", "sprinting=true, +dt=" + dt);
        }
    }
}

package com.csse3200.game.components.achievements;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.achievements.AchievementService;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public class AchievementsTrackerComponent extends Component {
    private boolean sprinting = false;

    @Override
    public void create() {
        super.create();
        // hook existing player events
        entity.getEvents().addListener("sprintStart", this::onSprintStart);
        entity.getEvents().addListener("sprintStop",  this::onSprintStop);
        entity.getEvents().addListener("exhausted",   this::onExhausted);

        // optional: listen for tutorial / level started / completed events if they route via player
        entity.getEvents().addListener("levelStarted", this::onLevelStarted);
        entity.getEvents().addListener("levelCompleted", this::onLevelCompleted);
    }

    private void onSprintStart() { sprinting = true; }
    private void onSprintStop()  { sprinting = false; }
    private void onExhausted()   { AchievementService.get().markStaminaExhausted(); }
    private void onLevelStarted() { AchievementService.get().onLevelStarted(); }
    private void onLevelCompleted() { /* if level# known elsewhere, trigger there */ }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        AchievementService.get().update(dt);
        if (sprinting) {
            AchievementService.get().addSprintTime(dt);
        }
    }
}

package com.csse3200.game.components.collectables;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;

public class promptComponent extends CollectableComponent{

    public final String prompt;
    public final int duration;
    public promptComponent(String prompt, int duration) {
        this.prompt = prompt;
        this.duration = duration;
    }

    @Override
    protected boolean onCollect(Entity collector) {

        TooltipSystem.TooltipManager.showTooltip(prompt, TooltipSystem.TooltipStyle.DEFAULT);
        // Schedule it to disappear after 5 seconds
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                TooltipSystem.TooltipManager.hideTooltip();
            }
        }, duration);

        return true;
    }

}

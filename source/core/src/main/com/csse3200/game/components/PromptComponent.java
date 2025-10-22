package com.csse3200.game.components;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;

/**
 * A component responsible for displaying a temporary prompt or tooltip
 * when player collides with another entity.
 * <p>
 * The prompt text and display duration are configured upon instantiation.
 */
public class PromptComponent extends Component {

    /** The text content of the prompt/tooltip to be displayed. */
    public final String prompt;
    /** The duration, in seconds, for which the prompt will be displayed. */
    public final Float duration;

    /**
     * Creates a new PromptComponent with the specified prompt text and display duration.
     *
     * @param prompt The string content to display in the tooltip.
     * @param duration The time, in seconds, before the tooltip automatically disappears.
     */
    public PromptComponent(String prompt, Float duration) {
        this.prompt = prompt;
        this.duration = duration;
    }

    /**
     * Registers a listener for {@code "onCollisionStart"} events.
     * When a collision starts, the {@link #onCollide(Entity)} method is executed.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("onCollisionStart", this::onCollide);
    }

    /**
     * Handles collision events.
     * <p>
     * Displays the stored {@link #prompt} using the {@link TooltipSystem}
     * and schedules the tooltip to hide after the specified {@link #duration}.
     *
     * @param collector The entity that has collided with this entity (and is thus
     * the one "triggering" the prompt display).
     * @return true to indicate the collision event was handled.
     */
    protected boolean onCollide(Entity collector) {
        TooltipSystem.TooltipManager.showTooltip(prompt, TooltipSystem.TooltipStyle.DEFAULT);

        // Schedule it to disappear after duration
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                TooltipSystem.TooltipManager.hideTooltip();
            }
        }, duration);

        return true;
    }
}
package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.PromptComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.ui.achievements.AchievementToastUI;
import com.csse3200.game.ui.achievements.AchievementsMenuUI;

/**
 * Factory to create HUD entity.
 */
public class HeadsUpDisplayFactory {

    private HeadsUpDisplayFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    /**
     * Creates and configures the main Heads-Up Display (HUD) entity for the game.
     * This entity contains various UI components responsible for persistent display elements
     * and menus.
     *
     * @param name The name of the game area/screen (e.g., "GameScreen").
     * @return The fully configured UI entity.
     */
    public static Entity createHeadsUpDisplay(String name) {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay(name));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        ui.addComponent(new AchievementToastUI());
        ui.addComponent(new AchievementsMenuUI());
        return ui;
    }

    /**
     * Creates a new entity that acts as a physical trigger for a temporary on-screen prompt.
     * This entity has a physics collider set as a sensor, and when another entity
     * (the player) collides with it, the specified prompt is displayed for a set duration.
     *
     * @param promptMessage The text to be displayed when the prompt is triggered.
     * @param duration The time, in seconds, the prompt will remain visible.
     * @param width The width of the physical trigger entity (used for scaling).
     * @param height The height of the physical trigger entity (used for scaling).
     * @return The fully configured prompt trigger entity.
     */
    public static Entity createPrompt(String promptMessage, Float duration, float width, float height) {
        Entity obj = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent()
                        .setLayer(PhysicsLayer.COLLECTABLE)
                        .setSensor(true))
                .addComponent(new PromptComponent(promptMessage, duration));

        obj.setScale(width, height);
        PhysicsUtils.setScaledCollider(obj, width, height);

        return obj;
    }
}

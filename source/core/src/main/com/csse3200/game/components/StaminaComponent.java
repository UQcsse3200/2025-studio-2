package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.csse3200.game.achievements.AchievementProgression;


/**
 * StaminaComponent - resource manager for player stamina.
 */
public class StaminaComponent extends Component {
    private float maxStamina;
    private float currentStamina;
    private float staminaRegenPerSecond;
    private float sprintDrainPerSecond;
    private int attackStaminaCost;
    private float regenDelaySeconds;
    private float regenDelayTimer = 0f;

    private boolean sprinting = false;
    private boolean exhausted = false;

    public StaminaComponent(float maxStamina,
                            float staminaRegenPerSecond,
                            float sprintDrainPerSecond,
                            int attackStaminaCost,
                            float regenDelaySeconds) {
        this.maxStamina = Math.max(1f, maxStamina);
        this.currentStamina = this.maxStamina;
        this.staminaRegenPerSecond = Math.max(0f, staminaRegenPerSecond);
        this.sprintDrainPerSecond = Math.max(0f, sprintDrainPerSecond);
        this.attackStaminaCost = Math.max(0, attackStaminaCost);
        this.regenDelaySeconds = Math.max(0f, regenDelaySeconds);
    }

    public StaminaComponent(float maxStamina,
                            float staminaRegenPerSecond,
                            float sprintDrainPerSecond,
                            int attackStaminaCost) {
        this(maxStamina, staminaRegenPerSecond, sprintDrainPerSecond, attackStaminaCost, 1.0f);
    }
    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("sprintStart", () -> setSprinting(true));
        entity.getEvents().addListener("sprintStop", () -> setSprinting(false));
    }

    public void update(float dt) {
        if (dt <= 0f) return;
        tick(dt);
    }
    @Override
    public void update() {
        tick(1f / 60f);
    }

    public void tick(float dt) {
        if (sprinting) {
            changeStamina(-sprintDrainPerSecond * dt);
            regenDelayTimer = regenDelaySeconds;
            return;
        }

        if (regenDelayTimer > 0f) {
            regenDelayTimer = Math.max(0f, regenDelayTimer - dt);
            return;
        }

        if (currentStamina < maxStamina && staminaRegenPerSecond > 0f) {
            changeStamina(staminaRegenPerSecond * dt);
        }
    }

    private void changeStamina(float delta) {
        float prev = currentStamina;
        currentStamina = Math.max(0f, Math.min(maxStamina, currentStamina + delta));

        if ((int) prev != (int) currentStamina) {
            triggerStaminaUpdate();
        }

        if (!exhausted && currentStamina <= 0f) {
            exhausted = true;
            triggerEvent("exhausted");
            AchievementProgression.onStaminaExhausted();
        } else if (exhausted && currentStamina > 0f) {
            exhausted = false;
            triggerEvent("recovered");
        }
    }

    private void triggerStaminaUpdate() {
        if (entity != null && entity.getEvents() != null) {
            entity.getEvents().trigger("staminaUpdate", (int) currentStamina, (int) maxStamina);

            // Update stamina bar
            entity.getEvents().trigger("updateStamina", currentStamina);
        }
    }

    private void triggerEvent(String name) {
        if (entity != null && entity.getEvents() != null) {
            entity.getEvents().trigger(name);
        }
    }

    public boolean tryConsumeForAttack() {
        if (currentStamina >= attackStaminaCost) {
            changeStamina(-attackStaminaCost);
            regenDelayTimer = regenDelaySeconds;
            return true;
        }
        return false;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
        if (sprinting) regenDelayTimer = regenDelaySeconds;
    }

    public int getCurrentStamina() { return Math.round(currentStamina); }
    public int getMaxStamina() { return Math.round(maxStamina); }
    public boolean isExhausted() { return exhausted; }

    public void setCurrentStamina(int value) {
        this.currentStamina = Math.max(0f, Math.min(maxStamina, value));
        triggerStaminaUpdate();
    }

    public static StaminaComponent fromConfig(Object cfg) {
        if (cfg == null) {
            return new StaminaComponent(100f, 10f, 25f, 20, 1.0f);
        }
        try {
            Class<?> c = cfg.getClass();

            Float max = readFloatFromConfig(c, cfg,
                    new String[]{"getMaxStamina", "getStamina", "maxStamina", "stamina"});
            Float regen = readFloatFromConfig(c, cfg,
                    new String[]{"getStaminaRegenPerSecond", "getStaminaRegen", "staminaRegenPerSecond", "staminaRegen"});
            Float sprintDrain = readFloatFromConfig(c, cfg,
                    new String[]{"getSprintDrainPerSecond", "getSprintDrain", "sprintDrainPerSecond", "sprintDrain"});
            Integer attackCost = readIntFromConfig(c, cfg,
                    new String[]{"getAttackStaminaCost", "getStaminaDrain", "attackStaminaCost", "staminaDrain"});
            Float regenDelay = readFloatFromConfig(c, cfg,
                    new String[]{"getStaminaRegenDelay", "staminaRegenDelay", "regenDelaySeconds", "regenDelay"});

            float maxF = (max != null) ? max : 100f;
            float regenF = (regen != null) ? regen : 10f;
            float drainF = (sprintDrain != null) ? sprintDrain : 25f;
            int costI = (attackCost != null) ? attackCost : 20;
            float delayF = (regenDelay != null) ? regenDelay : 1.0f;

            return new StaminaComponent(maxF, regenF, drainF, costI, delayF);
        } catch (Exception e) {
            return new StaminaComponent(100f, 10f, 25f, 20, 1.0f);
        }
    }

    private static Float readFloatFromConfig(Class<?> c, Object cfg, String[] names) {
        for (String name : names) {
            try {
                Method m = c.getMethod(name);
                Object v = m.invoke(cfg);
                if (v instanceof Number) return ((Number) v).floatValue();
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {}

            try {
                Field f = c.getField(name);
                Object v = f.get(cfg);
                if (v instanceof Number) return ((Number) v).floatValue();
            } catch (NoSuchFieldException | IllegalAccessException ignore) {}
        }
        return null;
    }

    private static Integer readIntFromConfig(Class<?> c, Object cfg, String[] names) {
        for (String name : names) {
            try {
                Method m = c.getMethod(name);
                Object v = m.invoke(cfg);
                if (v instanceof Number) return ((Number) v).intValue();
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {}

            try {
                Field f = c.getField(name);
                Object v = f.get(cfg);
                if (v instanceof Number) return ((Number) v).intValue();
            } catch (NoSuchFieldException | IllegalAccessException ignore) {}
        }
        return null;
    }
}

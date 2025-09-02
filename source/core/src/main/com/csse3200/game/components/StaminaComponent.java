package com.csse3200.game.components;

/**
 * StaminaComponent - resource manager for player stamina.
 *
 * - Handles drain while sprinting
 * - Handles regen after a short regen delay
 * - tryConsumeForAttack() to gate attacks
 * - Fires events via entity.getEvents().trigger(...):
 *     - "staminaUpdate" (int current, int max)
 *     - "exhausted"
 *     - "recovered"
 *
 * This implementation avoids a rigid dependency on a particular PlayerConfig
 * shape by providing fromConfig(Object) which uses reflection to probe likely
 * field/getter names. If you have a typed PlayerConfig, you can create the
 * component directly with the typed constructor.
 */
public class StaminaComponent extends Component {
    private float maxStamina;
    private float currentStamina;
    private float staminaRegenPerSecond;
    private float sprintDrainPerSecond;
    private int attackStaminaCost;

    // seconds to wait after spending stamina before regen resumes
    private float regenDelaySeconds;
    private float regenDelayTimer = 0f;

    private boolean sprinting = false;
    private boolean exhausted = false;

    // --- Constructors ---
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

    /** Convenience constructor with default regen delay = 1.0s */
    public StaminaComponent(float maxStamina,
                            float staminaRegenPerSecond,
                            float sprintDrainPerSecond,
                            int attackStaminaCost) {
        this(maxStamina, staminaRegenPerSecond, sprintDrainPerSecond, attackStaminaCost, 1.0f);
    }

    // --- Lifecycle hooks (many engines call either update() or update(dt)) ---
    // Provide both to be tolerant; remove one if your base Component requires a specific signature.

    /** Called by engines that provide delta-time */
    public void update(float dt) {
        if (dt <= 0f) return;
        tick(dt);
    }

    /** Called by engines without delta-time; we assume 60fps step */
    public void update() {
        tick(1f / 60f);
    }

    /** Core per-frame logic; call every frame with delta seconds */
    public void tick(float dt) {
        if (sprinting) {
            // Sprint drains stamina immediately and pauses regen
            changeStamina(-sprintDrainPerSecond * dt);
            // Reset regen delay so regen doesn't start while sprinting
            regenDelayTimer = regenDelaySeconds;
            return;
        }

        // If regen delay timer is active, count it down
        if (regenDelayTimer > 0f) {
            regenDelayTimer = Math.max(0f, regenDelayTimer - dt);
            return;
        }

        // Otherwise, regenerate
        if (currentStamina < maxStamina && staminaRegenPerSecond > 0f) {
            changeStamina(staminaRegenPerSecond * dt);
        }
    }

    private void changeStamina(float delta) {
        float prev = currentStamina;
        currentStamina = Math.max(0f, Math.min(maxStamina, currentStamina + delta));

        // Fire staminaUpdate when integer display value changes (reduces spam)
        if ((int) prev != (int) currentStamina) {
            triggerStaminaUpdate();
        }

        // Fire exhausted / recovered events on transitions
        if (!exhausted && currentStamina <= 0f) {
            exhausted = true;
            triggerEvent("exhausted");
        } else if (exhausted && currentStamina > 0f) {
            exhausted = false;
            triggerEvent("recovered");
        }
    }

    private void triggerStaminaUpdate() {
        if (entity != null && entity.getEvents() != null) {
            entity.getEvents().trigger("staminaUpdate", (int) currentStamina, (int) maxStamina);
        }
    }

    private void triggerEvent(String name) {
        if (entity != null && entity.getEvents() != null) {
            entity.getEvents().trigger(name);
        }
    }

    /**
     * Attempt to pay the attack stamina cost.
     * Returns true and deducts cost when enough stamina; otherwise returns false.
     * Also resets regen delay when stamina is spent.
     */
    public boolean tryConsumeForAttack() {
        if (currentStamina >= attackStaminaCost) {
            changeStamina(-attackStaminaCost);
            regenDelayTimer = regenDelaySeconds;
            return true;
        }
        return false;
    }

    /** Set whether the player is sprinting (input should call this) */
    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
        if (sprinting) regenDelayTimer = regenDelaySeconds;
    }

    /* Getters/Setters */
    public int getCurrentStamina() { return Math.round(currentStamina); }
    public int getMaxStamina() { return Math.round(maxStamina); }
    public boolean isExhausted() { return exhausted; }
    public void setCurrentStamina(int value) {
        this.currentStamina = Math.max(0f, Math.min(maxStamina, value));
        triggerStaminaUpdate();
    }

    // --- Reflection-friendly factory: uses common config getter/field names ---
    public static StaminaComponent fromConfig(Object cfg) {
        if (cfg == null) {
            return new StaminaComponent(100f, 10f, 25f, 20, 1.0f);
        }
        try {
            Class<?> c = cfg.getClass();

            Float max = readFloatFromConfig(c, cfg,
                    new String[] {"getMaxStamina", "getStamina", "maxStamina", "stamina"});
            Float regen = readFloatFromConfig(c, cfg,
                    new String[] {"getStaminaRegenPerSecond", "getStaminaRegen", "staminaRegenPerSecond", "staminaRegen"});
            Float sprintDrain = readFloatFromConfig(c, cfg,
                    new String[] {"getSprintDrainPerSecond", "getSprintDrain", "sprintDrainPerSecond", "sprintDrain"});
            Integer attackCost = readIntFromConfig(c, cfg,
                    new String[] {"getAttackStaminaCost", "getStaminaDrain", "attackStaminaCost", "staminaDrain"});
            Float regenDelay = readFloatFromConfig(c, cfg,
                    new String[] {"getStaminaRegenDelay", "staminaRegenDelay", "regenDelaySeconds", "regenDelay"});

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

    // Reflection helpers: try getters then fields
    private static Float readFloatFromConfig(Class<?> c, Object cfg, String[] names) {
        for (String name : names) {
            try {
                java.lang.reflect.Method m = c.getMethod(name);
                Object v = m.invoke(cfg);
                if (v instanceof Number) return ((Number) v).floatValue();
            } catch (NoSuchMethodException ignore) {}
            try {
                java.lang.reflect.Field f = c.getField(name);
                Object v = f.get(cfg);
                if (v instanceof Number) return ((Number) v).floatValue();
            } catch (NoSuchFieldException ignore) {}
        }
        return null;
    }

    private static Integer readIntFromConfig(Class<?> c, Object cfg, String[] names) {
        for (String name : names) {
            try {
                java.lang.reflect.Method m = c.getMethod(name);
                Object v = m.invoke(cfg);
                if (v instanceof Number) return ((Number) v).intValue();
            } catch (NoSuchMethodException ignore) {}
            try {
                java.lang.reflect.Field f = c.getField(name);
                Object v = f.get(cfg);
                if (v instanceof Number) return ((Number) v).intValue();
            } catch (NoSuchFieldException ignore) {}
        }
        return null;
    }
}

package com.csse3200.game.components;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.BossLaserFactory;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that allows a boss to spawn lasers periodically.
 */
public class BossLaserSpawnerComponent extends Component {
    private final Entity player;       // Player to target
    private boolean spawningEnabled = false;
    private float cooldownSec = 6f;    // seconds between laser shots
    private float timer = 0f;

    public BossLaserSpawnerComponent(Entity player) {
        this.player = player;
    }

    @Override
    public void create() {
        // Listen for start/stop events from the boss
        entity.getEvents().addListener("boss:startSpawning", () -> spawningEnabled = true);
        entity.getEvents().addListener("boss:stopSpawning", () -> spawningEnabled = false);
    }

    @Override
    public void update() {
        if (!spawningEnabled) return;

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer += dt;
        if (timer < cooldownSec) return;

        timer = 0f;
        spawnLaser();
    }

    private void spawnLaser() {
        // Create a new laser entity targeting the player
        Entity laser = BossLaserFactory.createBossLaser(player,0f);

        // Position the laser at the boss's current position
        laser.setPosition(entity.getPosition());

        // Register the laser so it is added to the active GameArea
        ServiceLocator.getEntityService().register(laser);
    }

    /**
     * Optional: allows you to adjust cooldown dynamically
     */
    public void setCooldown(float seconds) {
        this.cooldownSec = seconds;
    }
}
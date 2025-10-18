package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that allows a boss to spawn lasers periodically.
 */
public class BossLaserSpawnerComponent extends Component {
    private final Entity player;       // Player to target
    private boolean spawningEnabled = false;
    private float cooldownSec = 6f;    // default seconds between laser shots
    private float timer = 0f;

    public BossLaserSpawnerComponent(Entity player) {
        this.player = player;
    }

    @Override
    public void create() {
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

    // TODO: implement
    private void spawnLaser() {
        // `Entity laser = BossLaserFactory.createBossLaser(player);`
        // position laser at boss

        // Register with the EntityService, which will automatically add it to the active GameArea
        // `serviceLocator.getEntityService().register(laser);`
    }
}

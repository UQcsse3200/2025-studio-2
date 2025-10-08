package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.services.ServiceLocator;
/**
 * Components that allow bosses to spawn "small self-destruction drones" based on events.
 *
 * To use：
 *  1) In create boss factory .addComponent(new BossSpawnerComponent(player))。
 *  2) trigger by players/event：
 *      boss.getEvents().trigger("boss:startSpawning");
 *      boss.getEvents().trigger("boss:stopSpawning");
 *      boss.getEvents().trigger("boss:setPhase", 2); // upgrade level
 */
public class BossSpawnerComponent extends Component {
	// target: player
	private final Entity player;

	// spawn/refresh control
	private boolean spawningEnabled = false; // Whether spawn is allowed
	private float cooldownSec = 3.0f;        // default The number of seconds between spawns
	private float timer = 0f;                // Current time for cooldown
	private float initialDelaySec = 1.5f;    // Wait a while before opening
	private boolean waitingInitialDelay = false;
	private float initialDelayTimer = 0f;

	//config
	private int burstCount = 1;              // Brush a few at a time
	private int maxActive = 4;               // Maximum number of small drones present

	// The offset of the spawn position relative to the boss (can be configured as a circle around the boss)
	private final Vector2[] spawnOffsets = new Vector2[] {
			new Vector2(+1.5f, 0.3f),
			new Vector2(+2.0f, -0.2f),
			new Vector2(+1.2f, +0.8f)
	};

	// Simple "stage" example: shorten the cooldown and
	// increase the number of waves when entering the high stage
	private int phase = 1;

	public BossSpawnerComponent(Entity player) {
		this.player = player;
	}

	@Override
	public void create() {
		// 监听来自关卡/其他组件的事件
		entity.getEvents().addListener("boss:startSpawning", this::enableSpawning);
		entity.getEvents().addListener("boss:stopSpawning", this::disableSpawning);
		entity.getEvents().addListener("boss:setPhase", this::setPhase);
	}

	private void enableSpawning() {
		spawningEnabled = true;
		waitingInitialDelay = true;
		initialDelayTimer = 0f;
	}

	private void disableSpawning() {
		spawningEnabled = false;
	}

	private void setPhase(int newPhase) {
		this.phase = newPhase;
		// The higher the stage, the shorter the cooldown, and the more you can spawn at a time.
		if (phase >= 3) {
			cooldownSec = 1.5f;
			burstCount = 3;
			maxActive = 7;
		} else if (phase == 2) {
			cooldownSec = 2.2f;
			burstCount = 2;
			maxActive = 5;
		} else {
			cooldownSec = 3.0f;
			burstCount = 1;
			maxActive = 4;
		}
	}

	@Override
	public void update() {
		if (!spawningEnabled) return;

		float dt = ServiceLocator.getTimeSource().getDeltaTime();

		// A "start delay" after turning on
		if (waitingInitialDelay) {
			initialDelayTimer += dt;
			if (initialDelayTimer < initialDelaySec) return;
			waitingInitialDelay = false;
			timer = 0f; // reset timer
		}

		timer += dt;
		if (timer < cooldownSec) return;
		timer = 0f;

		// Control the "presence limit" to prevent overloading
		int alive = countActiveDronesNearby();
		if (alive >= maxActive) return;

		spawnBurst(Math.max(1, Math.min(burstCount, maxActive - alive)));
	}

	/** Estimate the number of small drones around the boss (look for nearby entities with self-destruct components). */
	private int countActiveDronesNearby() {
		// optional TO DO
		// For simplicity, this returns 0 (generation is always allowed).
		// If you have an entity query service (e.g., EntityService provides getEntities()),
		// you can iterate here, filter out entities with a SelfDestructionComponent and not marked disposed, and count the number.
		return 0;
	}

	/** one burst */
	private void spawnBurst(int n) {
		Vector2 bossPos = entity.getPosition().cpy();

		for (int i = 0; i < n; i++) {
			Vector2 offset = spawnOffsets[i % spawnOffsets.length];
			Vector2 spawnPos = bossPos.cpy().add(offset);

			// (target + spawnPos)
			Entity drone = EnemyFactory.createSelfDestructionDrone(player, spawnPos);

			// EntityService register entity into area
			ServiceLocator.getEntityService().register(drone);

			// optional
			drone.setPosition(spawnPos);
			// active small drone
			drone.getEvents().trigger("enemyActivated");
		}

	}
}

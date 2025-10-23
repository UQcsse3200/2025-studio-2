package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Boss three-stage drone spawning component:
 * * - Phase transitions based on the player's position as a trigger point;
 * * - Each phase is configurable: drone variant, spawn interval, concurrency limit, total spawns, and pre-swing duration;
 * * - Phase completion condition: Full spawns AND active count on the field is 0 -> switch back to pursuit animation/behavior.
 */
public class BossSpawnerComponent extends Component {
	private static final Logger logger = LoggerFactory.getLogger(BossSpawnerComponent.class);

	private final List<Vector2> spawnTriggers;
	private final List<Boolean> triggered;
	private Entity player;

	/** Cooldown: Wait between builds (set by stage config) */
	private float spawnCooldown = 0f;

	/** Pre-swing: start the animation ("generateDroneStart") before actually generating and wait for a while */
	private float windup = 0f;
	private final float spawnInterval; //won't use for new component
	private int currentTriggerIndex = 0;

	// Track spawned drones for cleanup
	private final List<Entity> spawnedDrones = new ArrayList<>();


	/** phase config */
	public static class PhaseConfig {
		public final EnemyFactory.DroneVariant variant; // atlas and speed
		public final float spawnInterval;               // generation interval /s
		public final int   maxConcurrent;               // Concurrency limit (maximum number of players on the field at the same time)
		public final int   totalToSpawn;                // How many are generated in this phase?
		public final float windup;

		public PhaseConfig(EnemyFactory.DroneVariant variant,
						   float spawnInterval,
						   int maxConcurrent,
						   int totalToSpawn,
						   float windup) {
			this.variant = variant;
			this.spawnInterval = spawnInterval;
			this.maxConcurrent = maxConcurrent;
			this.totalToSpawn = totalToSpawn;
			this.windup = windup;
		}
	}
	/** Stage configuration table (default for 3 stages) */
	private final List<PhaseConfig> phaseConfigs = new ArrayList<>();
	/** if stage complete */
	private final List<Boolean> phaseCompleted = new ArrayList<>();
	private boolean isSpawningActive = false;
	/** The number of generated items in the current phase */
	private int spawnedInPhase = 0;


	/**
	 * Create boss drone spawn component with configurable triggers
	 * @param spawnTriggers List of player position thresholds that trigger spawning
	 * @param spawnInterval Time between drone spawns during active phase (old use)
	 */
	public BossSpawnerComponent(List<Vector2> spawnTriggers, float spawnInterval) {
		this.spawnTriggers = new ArrayList<>(spawnTriggers);
		this.spawnInterval = spawnInterval;
		this.triggered = new ArrayList<>();
		for (int i = 0; i < spawnTriggers.size(); i++) {
			triggered.add(false);
		}
		initDefaultPhaseConfigs(); // default config
	}
	/** default config */
	private void initDefaultPhaseConfigs() {
		// Phase 0：Scouting (slow, few, long intervals, short pre-swings)
		phaseConfigs.add(new PhaseConfig(
				EnemyFactory.DroneVariant.SCOUT,
				1.5f, // spawnInterval
				1,    // maxConcurrent
				1,    // totalToSpawn
				1.4f  // windup
		));
		// Phase 1：chaser (Medium)
		phaseConfigs.add(new PhaseConfig(
				EnemyFactory.DroneVariant.CHASER,
				1.0f,
				5,
				5,
				1.4f
		));
		// Phase 2: brutal (fast, frequent, short intervals, slightly longer pre-swing)
		phaseConfigs.add(new PhaseConfig(
				EnemyFactory.DroneVariant.BRUTAL,
				0.7f,
				7,
				7,
				1.4f
		));
		// finish initial
		for (int i = 0; i < phaseConfigs.size(); i++) {
			phaseCompleted.add(false);
		}
	}
	@Override
	public void create() {
		super.create();
		findPlayer();
	}

	/**
	 * Find the player entity in the entity service
	 */
	private void findPlayer() {
		for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
			if (entity.getComponent(com.csse3200.game.components.player.PlayerActions.class) != null) {
				player = entity;
				return;
			}
		}
	}

	@Override
	public void update() {
		if (player == null) {
			findPlayer();
			return;
		}

		checkSpawnTriggers();
		updateSpawning();

		// Clean up dead drones from the tracking list
		cleanupDeadDrones();
	}

	/**
	 * Check if player has reached any spawn trigger positions
	 */
	private void checkSpawnTriggers() {
		Vector2 playerPos = player.getPosition();

		for (int i = currentTriggerIndex; i < spawnTriggers.size(); i++) {
			Vector2 trigger = spawnTriggers.get(i);

			// Trigger when player X position reaches or exceeds trigger X
			if (Boolean.FALSE.equals(triggered.get(i)) && playerPos.x >= trigger.x) {
				triggered.set(i, true);
				currentTriggerIndex = i;
				startSpawningPhase();
				break;
			}
		}
	}

	/**
	 * Start a new spawning phase
	 */
	private void startSpawningPhase() {
		// Validity & Completed Check: If there is no corresponding configuration or
		// the stage has been completed, no further entry will be made.
		if (currentTriggerIndex < 0 || currentTriggerIndex >= phaseConfigs.size()) {
			logger.info("No phase config for trigger {}", currentTriggerIndex);
			return;
		}
		if (phaseCompleted.get(currentTriggerIndex)) {
			logger.info("Phase {} already completed, skip.", currentTriggerIndex);
			return;
		}

		// Entry stage: reset count and timing
		spawnedInPhase = 0;
		isSpawningActive = true;
		spawnCooldown = 0.5f; // Give it a little buffer at the beginning
		windup = 0f;

		// Throw events to Boss animation/performance/QA
		entity.getEvents().trigger("spawningPhaseStart", currentTriggerIndex);
		entity.getEvents().trigger("boss:phaseChanged", currentTriggerIndex);
		logger.info("Phase {} started", currentTriggerIndex);
	}

	/**
	 * Update drone spawning logic
	 */
	private void updateSpawning() {
		if (!isSpawningActive) return;

		if (currentTriggerIndex < 0 || currentTriggerIndex >= phaseConfigs.size()) return;
		PhaseConfig cfg = phaseConfigs.get(currentTriggerIndex);

		// Phase completion judgment: Fully generated and the field cleared -> Completed,
		// switch back to chase
		if (spawnedInPhase >= cfg.totalToSpawn && getActiveDroneCount() == 0) {
			if (!phaseCompleted.get(currentTriggerIndex)) {
				phaseCompleted.set(currentTriggerIndex, true);
				entity.getEvents().trigger("chaseStart");
				logger.info("Phase {} completed -> back to chase", currentTriggerIndex);
			}
			isSpawningActive = false;
			return;
		}

		float dt = ServiceLocator.getTimeSource().getDeltaTime();

		// Generation is cooling down
		if (spawnCooldown > 0f) {
			spawnCooldown -= dt;
			return;
		}

		// Not yet full, and concurrency limit not exceeded -> Prepare for a single generation
		int alive = getActiveDroneCount();
		if (spawnedInPhase < cfg.totalToSpawn && alive < cfg.maxConcurrent) {
			// Do a pre-swing first and play the Generate Animation
			if (windup <= 0f) {
				windup = cfg.windup;
				entity.getEvents().trigger("generateDroneStart"); // animation
				return;
			}

			// Countdown ends -> actual generation
			windup -= dt;
			if (windup <= 0f) {
				spawnDrone(cfg.variant);        // Build with variants
				spawnedInPhase++;               // Current stage cumulative +1
				spawnCooldown = cfg.spawnInterval; // next stage interval
			}
		}
		// Otherwise: Either maxed out or not maxed out -> wait for existing drones to die/leave
	}

	/**
	 * Spawn variables self-destruct drone from the boss position
	 */
	private void spawnDrone(EnemyFactory.DroneVariant variant) {
		Vector2 bossPos = entity.getPosition();
		Vector2 spawnPos = getSpawnPositionAroundBoss(bossPos);

		// create variable drones
		Entity drone = EnemyFactory.createBossSelfDestructDrone(player, spawnPos, variant);

		drone.setPosition(spawnPos);

		// register and activate
		ServiceLocator.getEntityService().register(drone);
		// Make sure there is a frame available before scaling
		AnimationRenderComponent arc = drone.getComponent(AnimationRenderComponent.class);
		if (arc != null) {
			// Start first (some components set the default frame when startAnimation)
			try {
				arc.startAnimation("angry_float");
			} catch (Exception ignore) {
				arc.startAnimation("float");
			}
		}

		drone.getEvents().trigger("enemyActivated");

		// count
		spawnedDrones.add(drone);

		//animation trigger
		entity.getEvents().trigger("droneSpawned");
		logger.info("Drone spawned (variant: {}, alive: {})", variant, getActiveDroneCount());
	}

	/** Compatible with old logic: the default version without parameters is to refresh CHASER */
	@SuppressWarnings("unused")
	private void spawnDrone() {
		spawnDrone(EnemyFactory.DroneVariant.CHASER);

	}

	/**
	 * Get number of currently active drones
	 */
	private int getActiveDroneCount() {
		int activeCount = 0;
		for (Entity drone : spawnedDrones) {
			if (drone != null && ServiceLocator.getEntityService().getEntities().contains(drone, true)) {
				activeCount++;
			}
		}
		return activeCount;
	}

	/**
	 * Clean up dead drones from the tracking list
	 */
	private void cleanupDeadDrones() {
		spawnedDrones.removeIf(drone ->
				drone == null || !ServiceLocator.getEntityService().getEntities().contains(drone, true)
		);
	}

	/**
	 * Get spawn position around the boss
	 */
	private Vector2 getSpawnPositionAroundBoss(Vector2 bossPos) {
		// Spawn drones around the boss in different positions
		float offsetX = 4f;
		float offsetY = 0f;

		return bossPos.cpy().add(offsetX, offsetY);
	}

	/**
	 * Reset all triggers (for level reset)
	 */
	public void resetTriggers() {
        triggered.replaceAll(ignored -> false);
		currentTriggerIndex = 0;

		spawnCooldown = 0f;
		windup = 0f;
		spawnedInPhase = 0;
		isSpawningActive = false;

        phaseCompleted.replaceAll(ignored -> false);

		spawnedDrones.clear();
		logger.info("Triggers & phase states reset");
	}

	/**
	 * Clean up all spawned drones
	 */
	public void cleanupDrones() {
		logger.info("Cleaning up {} spawned drones", spawnedDrones.size());
		for (Entity drone : spawnedDrones) {
			if (drone != null && ServiceLocator.getEntityService().getEntities().contains(drone, true)) {
				drone.dispose();
			}
		}
		spawnedDrones.clear();
		spawnedInPhase = 0;
		isSpawningActive = false;
	}

	@Override
	public void dispose() {
		cleanupDrones();
		super.dispose();
	}

	/**
	 * Add a new spawn trigger dynamically
	 */
	public void addSpawnTrigger(Vector2 trigger) {
		spawnTriggers.add(trigger);
		triggered.add(false);
		logger.info("Added trigger at X: {}", trigger.x);
	}

	/**
	 * Get current active trigger index
	 */
	public int getCurrentTriggerIndex() {
		return currentTriggerIndex;
	}

	/**
	 * Check if specific trigger has been activated
	 */
	public boolean isTriggerActivated(int index) {
		return index >= 0 && index < triggered.size() && triggered.get(index);
	}

	/**
	 * Get number of triggers
	 */
	public int getTriggerCount() {
		return spawnTriggers.size();
	}

	/**
	 * Get the player entity for testing purposes
	 * @return player entity
	 */
	public Entity getPlayer() {
		return player;
	}
}
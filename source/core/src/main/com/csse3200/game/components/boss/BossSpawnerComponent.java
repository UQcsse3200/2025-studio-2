package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Component for boss enemy to spawn self-destruct drones based on player position triggers
 */
public class BossSpawnerComponent extends Component {
	private static final Logger logger = LoggerFactory.getLogger(BossSpawnerComponent.class);

	private final List<Vector2> spawnTriggers;
	private final List<Boolean> triggered;
	private Entity player;
	private float spawnCooldown = 0f;
	private final float spawnInterval;
	private int currentTriggerIndex = 0;
	// private float debugTimer = 0f;

	// Track spawned drones for cleanup
	private final List<Entity> spawnedDrones = new ArrayList<>();

	// Maximum drones - fixed at 3
	private static final int MAX_DRONES = 3;
	private int totalDronesSpawned = 0;

	/**
	 * Create boss drone spawn component with configurable triggers
	 * @param spawnTriggers List of player position thresholds that trigger spawning
	 * @param spawnInterval Time between drone spawns during active phase
	 */
	public BossSpawnerComponent(List<Vector2> spawnTriggers, float spawnInterval) {
		this.spawnTriggers = new ArrayList<>(spawnTriggers);
		this.spawnInterval = spawnInterval;
		this.triggered = new ArrayList<>();
		for (int i = 0; i < spawnTriggers.size(); i++) {
			triggered.add(false);
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
		for (Entity entity : ServiceLocator.getEntityService().get_entities()) {
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
			if (!triggered.get(i) && playerPos.x >= trigger.x) {
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
		spawnCooldown = 0f; // Start spawning immediately
		entity.getEvents().trigger("spawningPhaseStart", currentTriggerIndex);
		entity.getEvents().trigger("generateDroneStart"); // Trigger boss animation
	}

	/**
	 * Update drone spawning logic
	 */
	private void updateSpawning() {
		if (totalDronesSpawned >= MAX_DRONES) {
			return;
		}

		if (spawnCooldown > 0) {
			spawnCooldown -= ServiceLocator.getTimeSource().getDeltaTime();
			return;
		}

		// Check if we should spawn a drone
		if (currentTriggerIndex < spawnTriggers.size() && triggered.get(currentTriggerIndex)) {
			spawnDrone();
			spawnCooldown = spawnInterval;
		}
	}

	/**
	 * Spawn a self-destruct drone from the boss position
	 */
	private void spawnDrone() {
		// Check if we've reached the maximum drone limit
		if (totalDronesSpawned >= MAX_DRONES) {
			return;
		}


		Vector2 bossPos = entity.getPosition();
		Vector2 spawnPos = getSpawnPositionAroundBoss(bossPos);

		// Create self-destruct drone targeting the player
		Entity drone = EnemyFactory.createBossSelfDestructDrone(player, spawnPos);
		drone.getEvents().trigger("enemyActivated");

		drone.setPosition(spawnPos);

		// Register the drone
		ServiceLocator.getEntityService().register(drone);

		// Track the drone for cleanup
		spawnedDrones.add(drone);
		totalDronesSpawned++;

		// Trigger spawn event
		entity.getEvents().trigger("droneSpawned", drone);
		logger.info("Drone spawned successfully! Total: {}/{}", totalDronesSpawned, MAX_DRONES);
	}

	/**
	 * Get number of currently active drones
	 */
	private int getActiveDroneCount() {
		int activeCount = 0;
		for (Entity drone : spawnedDrones) {
			if (drone != null && ServiceLocator.getEntityService().get_entities().contains(drone, true)) {
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
				drone == null || !ServiceLocator.getEntityService().get_entities().contains(drone, true)
		);
	}

	/**
	 * Get spawn position around the boss
	 */
	private Vector2 getSpawnPositionAroundBoss(Vector2 bossPos) {
		// Spawn drones around the boss in different positions
		float offsetX = 2f;
		float offsetY = 0f;

		return bossPos.cpy().add(offsetX, offsetY);
	}

	/**
	 * Reset all triggers (for level reset)
	 */
	public void resetTriggers() {
		for (int i = 0; i < triggered.size(); i++) {
			triggered.set(i, false);
		}
		currentTriggerIndex = 0;
		spawnCooldown = 0f;
		totalDronesSpawned = 0; // Reset drone counter on level reset
		spawnedDrones.clear(); // Clear the tracking list
		logger.info("Triggers and drone counter reset");
	}

	/**
	 * Clean up all spawned drones
	 */
	public void cleanupDrones() {
		logger.info("Cleaning up {} spawned drones", spawnedDrones.size());
		for (Entity drone : spawnedDrones) {
			if (drone != null && ServiceLocator.getEntityService().get_entities().contains(drone, true)) {
				drone.dispose();
			}
		}
		spawnedDrones.clear();
		totalDronesSpawned = 0; // Reset counter
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
	 * Get current drone count
	 */
	public int getTotalDronesSpawned() {
		return totalDronesSpawned;
	}

	/**
	 * Get maximum drone limit
	 */
	public int getMaxDrones() {
		return MAX_DRONES;
	}

	/**
	 * Check if maximum drone limit has been reached
	 */
	public boolean isMaxDronesReached() {
		return totalDronesSpawned >= MAX_DRONES;
	}

	/**
	 * Get the player entity for testing purposes
	 * @return player entity
	 */
	public Entity getPlayer() {
		return player;
	}
}
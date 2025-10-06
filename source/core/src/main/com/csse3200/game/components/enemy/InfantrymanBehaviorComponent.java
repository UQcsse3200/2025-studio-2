package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that manages the behavioral state of an Infantryman enemy.
 * Works in conjunction with InfantrymanChaseTask to provide state-based behavior.
 * Similar to how drone enemies work - the chase task handles movement,
 * this component handles state transitions and animations.
 */
public class InfantrymanBehaviorComponent extends Component {

    public enum InfantrymanState {
        OFF,            // Inactive, not detecting targets
        ENABLING,       // Transition from off to active
        IDLE,          // Active but no target in attack range
        CHASING,       // Moving toward target
        ATTACKING,     // Close enough to attack target
        SHUTTING_DOWN  // Transition from active to off
    }

    private InfantrymanState currentState = InfantrymanState.OFF;
    private float shutdownTimer = 0f;
    private final float shutdownDelay = 2f; // 2 seconds before shutdown
    private final GameTime timeSource;

    // Target status from chase task
    private boolean targetVisible = false;
    private boolean targetInAttackRange = false;
    private boolean targetCloseEnough = false;

    public InfantrymanBehaviorComponent() {
        this.timeSource = ServiceLocator.getTimeSource();
    }

    @Override
    public void create() {
        super.create();

        // Listen for events from the chase task
        entity.getEvents().addListener("chaseActivated", this::onChaseActivated);
        entity.getEvents().addListener("chaseDeactivated", this::onChaseDeactivated);
        entity.getEvents().addListener("targetStatusUpdate", this::onTargetStatusUpdate);

        // Start in OFF state
        changeState(InfantrymanState.OFF);
    }

    @Override
    public void update() {
        float deltaTime = timeSource.getDeltaTime();
        updateStateMachine(deltaTime);
    }

    /**
     * Called when chase task is activated (enemy activated event)
     */
    private void onChaseActivated() {
        if (currentState == InfantrymanState.OFF) {
            changeState(InfantrymanState.ENABLING);
        }
    }

    /**
     * Called when chase task is deactivated (end conditions met)
     */
    private void onChaseDeactivated() {
        changeState(InfantrymanState.OFF);
    }

    /**
     * Called when chase task updates target status
     */
    private void onTargetStatusUpdate(boolean visible, boolean inRange, boolean closeEnough) {
        this.targetVisible = visible;
        this.targetInAttackRange = inRange;
        this.targetCloseEnough = closeEnough;
    }

    /**
     * Main state machine logic
     */
    private void updateStateMachine(float deltaTime) {
        switch (currentState) {
            case ENABLING:
                handleEnablingState();
                break;

            case IDLE:
                handleIdleState();
                break;

            case CHASING:
                handleChasingState();
                break;

            case ATTACKING:
                handleAttackingState();
                break;

            case SHUTTING_DOWN:
                handleShuttingDownState(deltaTime);
                break;
        }
    }

    private void handleEnablingState() {
        // Brief enabling state, then transition to idle
        if (targetVisible) {
            changeState(InfantrymanState.IDLE);
        } else {
            // If we lose target during enabling, start shutdown
            startShutdownSequence();
        }
    }

    private void handleIdleState() {
        if (!targetVisible) {
            startShutdownSequence();
        } else if (targetInAttackRange) {
            if (targetCloseEnough) {
                changeState(InfantrymanState.ATTACKING);
            } else {
                changeState(InfantrymanState.CHASING);
            }
        }
    }

    private void handleChasingState() {
        if (!targetVisible) {
            startShutdownSequence();
        } else if (targetCloseEnough) {
            changeState(InfantrymanState.ATTACKING);
        } else if (!targetInAttackRange) {
            changeState(InfantrymanState.IDLE);
        }
        // Continue chasing - movement handled by chase task
    }

    private void handleAttackingState() {
        if (!targetVisible) {
            startShutdownSequence();
        } else if (!targetCloseEnough) {
            if (targetInAttackRange) {
                changeState(InfantrymanState.CHASING);
            } else {
                changeState(InfantrymanState.IDLE);
            }
        }
        // Continue attacking while target is close enough
    }

    private void handleShuttingDownState(float deltaTime) {
        shutdownTimer -= deltaTime;
        if (shutdownTimer <= 0) {
            changeState(InfantrymanState.OFF);
        }

        // If target comes back into view during shutdown, cancel shutdown
        if (targetVisible) {
            changeState(InfantrymanState.IDLE);
        }
    }

    private void startShutdownSequence() {
        shutdownTimer = shutdownDelay;
        changeState(InfantrymanState.SHUTTING_DOWN);
    }

    /**
     * Changes the current state and triggers appropriate animation events
     */
    private void changeState(InfantrymanState newState) {
        if (currentState == newState) return;

        currentState = newState;

        // Trigger appropriate animation event
        switch (newState) {
            case OFF:
                entity.getEvents().trigger("offStart");
                break;
            case ENABLING:
                entity.getEvents().trigger("enableStart");
                break;
            case IDLE:
                entity.getEvents().trigger("idleStart");
                break;
            case CHASING:
                entity.getEvents().trigger("walkStart");
                break;
            case ATTACKING:
                entity.getEvents().trigger("attackStart");
                break;
            case SHUTTING_DOWN:
                entity.getEvents().trigger("shutdownStart");
                break;
        }
    }

    // Getters for debugging and external access
    public InfantrymanState getCurrentState() {
        return currentState;
    }

    public boolean isActive() {
        return currentState != InfantrymanState.OFF;
    }

    public boolean isTransitioning() {
        return currentState == InfantrymanState.ENABLING ||
                currentState == InfantrymanState.SHUTTING_DOWN;
    }
}
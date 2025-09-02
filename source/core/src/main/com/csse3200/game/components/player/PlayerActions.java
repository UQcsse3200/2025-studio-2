package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.StaminaComponent;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Action component for interacting with the player. Player events should be initialised in create()
 * and when triggered should call methods within this class.
 */
public class PlayerActions extends Component {
  // Base top speed (m/s). Sprint multiplies this.
  private static final Vector2 MAX_SPEED = new Vector2(3f, 3f);
  private static final float   SPRINT_MULT = 1.8f; // make it obvious while testing; tune later

  private PhysicsComponent physicsComponent;
  private StaminaComponent stamina;

  private final Vector2 walkDirection = Vector2.Zero.cpy();
  private boolean moving = false;

  // Whether player is currently holding sprint (Shift)
  private boolean wantsSprint = false;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    stamina = entity.getComponent(StaminaComponent.class);

    // Movement
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);

    // Actions
    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("interact", this::interact);

    // Sprint intent from keyboard input (Shift down/up)
    entity.getEvents().addListener("sprintStart", () -> {
      wantsSprint = true;
      if (stamina != null && !stamina.isExhausted() && stamina.getCurrentStamina() > 0) {
        stamina.setSprinting(true); // start draining now
      }
    });
    entity.getEvents().addListener("sprintStop", () -> {
      wantsSprint = false;
      if (stamina != null) {
        stamina.setSprinting(false); // stop draining, start delay
      }
    });
  }

  @Override
  public void update() {
    if (moving) {
      updateSpeed();
    }
  }

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();

    // Only allow sprint bonus if stamina says it's okay
    boolean canSprint = stamina != null && !stamina.isExhausted() && stamina.getCurrentStamina() > 0;
    float mult = (wantsSprint && canSprint) ? SPRINT_MULT : 1f;

    Vector2 desiredVelocity = walkDirection.cpy().scl(MAX_SPEED).scl(mult);

    // impulse = (desiredVel - currentVel) * mass
    Vector2 impulse = desiredVelocity.sub(velocity).scl(body.getMass());
    body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
  }

  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
    this.walkDirection.set(direction);
    moving = true;
  }

  /**
   * Returns the player's current walking direction as a 2D vector.
   * x: +right, -left; y: +up, -down
   */
  public Vector2 getWalkDirection() {
    return walkDirection.cpy();
  }

  /**
   * Stops the player from walking.
   */
  void stopWalking() {
    this.walkDirection.setZero();
    updateSpeed(); // apply zero desired velocity so we decelerate immediately
    moving = false;
  }

  /**
   * Makes the player attack.
   */
  void attack() {
    // (Optional) If you want attacks to spend stamina:
    // if (stamina != null && !stamina.tryConsumeForAttack()) return;

    Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play(UserSettings.getMasterVolume());
  }

  /**
   * Makes the player interact.
   */
  void interact() {
    Sound interactSound = ServiceLocator.getResourceService().getAsset("sounds/chimesound.mp3", Sound.class);
    interactSound.play(UserSettings.getMasterVolume());
  }
}


package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.csse3200.game.components.*;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.statisticspage.StatsTracker;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.CrouchingColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.StandingColliderComponent;
import com.csse3200.game.physics.raycast.AllHitCallback;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.physics.raycast.SingleHitCallback;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.utils.math.Vector2Utils;

import java.awt.*;

/**
 * Action component for interacting with the player. Player events should be initialised in create()
 * and when triggered should call methods within this class.
 */
public class PlayerActions extends Component {
  private static final float MAX_ACCELERATION = 70f;
  private static Vector2 WALK_SPEED = new Vector2(7f, 7f); // Metres
  private static final Vector2 ADRENALINE_SPEED = WALK_SPEED.cpy().scl(3);
  private static final Vector2 CROUCH_SPEED = WALK_SPEED.cpy().scl(0.3F);
  private static final float   SPRINT_MULT = 2.3f;

  private static final int DASH_SPEED_MULTIPLIER = 30;
  private static final float JUMP_IMPULSE_FACTOR = 20f;

  private static final int FUEL_CAPACITY = 100;

  private PhysicsComponent physicsComponent;
  private StaminaComponent stamina;
  private CameraComponent cameraComponent;
  private Vector2 walkDirection = Vector2.Zero.cpy();

  private boolean moving = false;
  private boolean adrenaline = false;
  private boolean crouching = false;

  // For Tests (Not Functionality)
  private boolean hasDashed = false;

  private boolean isJumping = false;
  private boolean isDoubleJump = false;

  private boolean soundPlayed = false;

  private CombatStatsComponent combatStatsComponent;

  // Whether player is currently holding sprint (Shift)
  private boolean wantsSprint = false;
  private int jetpackFuel = FUEL_CAPACITY;
  private boolean isJetpackOn = false;
  private boolean isGliding = false;
  private boolean hasActivatedJetpack;

  private Sound jetpackSound = ServiceLocator.getResourceService().getAsset(
          "sounds/jetpacksound.mp3", Sound.class);
  private Sound walkSound = ServiceLocator.getResourceService().getAsset(
          "sounds/walksound.mp3", Sound.class);

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    combatStatsComponent = entity.getComponent(CombatStatsComponent.class);
    cameraComponent = entity.getComponent(CameraComponent.class);
    stamina = entity.getComponent(StaminaComponent.class);
    walkSound.loop(UserSettings.get().masterVolume);
    walkSound.pause();

    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);

    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("interact", this::interact);

    entity.getEvents().addListener("jump", this::jump);
    entity.getEvents().addListener("landed", this::onLand);

    entity.getEvents().addListener("toggleAdrenaline", this::toggleAdrenaline);
    entity.getEvents().addListener("dash", this::dash);

    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    entity.getEvents().addListener("gravityForPlayerOff", this::gravityOff);
    entity.getEvents().addListener("gravityForPlayerOn", this::gravityOn);

    entity.getEvents().addListener("glide", this::glide);
    entity.getEvents().addListener("jetpackOn", this::jetpackOn);
    entity.getEvents().addListener("jetpackOff", this::jetpackOff);

    entity.getEvents().addListener("crouch", this::crouch);
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
    entity.getEvents().addListener("exhausted", () -> {
      wantsSprint = false;
      if (stamina != null) stamina.setSprinting(false);
    });
  }

  @Override
  public void update() {

    Body body = physicsComponent.getBody();

    if (moving || isJetpackOn) {
      updateSpeed();
    }

    if (jetpackFuel <= 0) {
      jetpackOff();
    }

    if (isJetpackOn) {
      jetpackFuel--;
      body.setGravityScale(0f); //for impulse to act upwards
         entity.getEvents().trigger("updateJetpackFuel", jetpackFuel);
    } else if (jetpackFuel < FUEL_CAPACITY) {
        if (!hasActivatedJetpack) {
            jetpackFuel++;
            entity.getEvents().trigger("updateJetpackFuel", jetpackFuel);
        }
    }

    if (!isJetpackOn && !isGliding) {
      body.setGravityScale(1f);
    }

    if (body.getLinearVelocity().y < 0) {
      body.applyForceToCenter(new Vector2(0, -body.getMass() * 10f), true);
    }

    // Check if the player's health is currently 0, in which case, death screen will handle reset
    // (Death screen is triggered by the "playerDied" event from CombatStatsComponent)
    if (combatStatsComponent.isDead()) {
      // Stop player movement when dead
      moving = false;
      setEnabled(false); // Disable repeated death events and like a thousand function calls!!
      entity.getEvents().trigger("death");
      // Death screen component will handle the reset when user chooses to restart
    }
  }

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();
    Vector2 desiredVelocity;
    boolean canSprint = stamina != null && !stamina.isExhausted() && stamina.getCurrentStamina() > 0;
    float mult = (wantsSprint && canSprint) ? SPRINT_MULT : 1f;

      if (adrenaline) {
      desiredVelocity = walkDirection.cpy().scl(ADRENALINE_SPEED);
    } else if (crouching) {
      desiredVelocity = walkDirection.cpy().scl(CROUCH_SPEED);
    } else {
      desiredVelocity = walkDirection.cpy().scl(WALK_SPEED);
    }
    desiredVelocity.scl(mult);

    // impulse = (desiredVel - currentVel) * mass
    // only update the horizontal impulse

    float deltaV = desiredVelocity.x - velocity.x;
    float maxDeltaV =
            MAX_ACCELERATION * Gdx.graphics.getDeltaTime();
    if (deltaV > maxDeltaV) deltaV = maxDeltaV;
    if (deltaV < -maxDeltaV) deltaV = -maxDeltaV;
    float impulseY;

    if (entity.getComponent(KeyboardPlayerInputComponent.class).getIsCheatsOn()
    || entity.getComponent(KeyboardPlayerInputComponent.class).getOnLadder()) {
      entity.getEvents().trigger("gravityForPlayerOff");
      float deltaVy = desiredVelocity.y - velocity.y;
      float maxDeltaVy = MAX_ACCELERATION * Gdx.graphics.getDeltaTime();
      if (deltaVy > maxDeltaVy) deltaVy = maxDeltaVy;
      if (deltaVy < -maxDeltaVy) deltaVy = -maxDeltaVy;
      impulseY = deltaVy * body.getMass();

    } else if (isJetpackOn) {
        float targetVy = 7f;
        float deltaVy = targetVy - velocity.y;
        impulseY = deltaVy * body.getMass();

    } else {
      //entity.getComponent(KeyboardPlayerInputComponent.class).setOnLadder(false);
        if (!isGliding) {
           entity.getEvents().trigger("gravityForPlayerOn");
        }
      impulseY = 0f;
    }

    Vector2 impulse = new Vector2(deltaV * body.getMass(), impulseY);
    body.applyLinearImpulse(new Vector2(impulse.x, impulseY), body.getWorldCenter(), true);


    /*Vector2 impulse =
            new Vector2((desiredVelocity.x - velocity.x), 0).scl(body.getMass());
    body.applyLinearImpulse(impulse, body.getWorldCenter(), true);*/

  }

  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
      this.walkDirection.set(direction); // <- keep/make this
      walkSound.resume();
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
    walkSound.pause();
    moving = false;
  }

  /**
   * Makes the player jump
   *
   * This method applies an upward impules to the players physics body to initiate a jump.
   * It handles both single and double jumps - if the player has already used their single and
   * double jump the method returns immediately
   *
   * Before applying the impuse, the players vertical velocity is set to 0 to keep consistent jump heights
   */
  void jump() {

    if (isJumping && isDoubleJump) return;

    if(isJumping) isDoubleJump = true;

    Body body = physicsComponent.getBody();

    Vector2 vel = body.getLinearVelocity();

    if (vel.y != 0) {
      body.setLinearVelocity(vel.x, 0f);
    }

      float impulseY = body.getMass() * JUMP_IMPULSE_FACTOR;

      body.applyLinearImpulse(new Vector2(0f, impulseY), body.getWorldCenter(), true);

      StatsTracker.addJump();

      isJumping = true;
  }

  /**
   * Called when a player lands on a surface
   *
   * This method resets the players jump state, allowing them to jump again
   */
  void onLand() {
    isJumping = false;
    isDoubleJump = false;
    hasActivatedJetpack = false;

//    Sound interactSound = ServiceLocator.getResourceService().getAsset(
//            "sounds/thudsound.mp3", Sound.class);
//    interactSound.play(0.08f);
  }

  /**
   * Boosts the players speed, `activates adrenaline`
   */
  void toggleAdrenaline() {
    // Player cannot sprint (adrenaline) while crouching
    if (crouching) {
      return;
    }

    // Toggle the adrenaline on or off
    adrenaline = !adrenaline;
  }

  /**
   * Gives the player a boost of speed in the given direction
   */
  void dash() {
    if (crouching) {
      return;
    }

    moving = true;
    hasDashed = true;

    Body body = physicsComponent.getBody();

    // Scale the direction vector to increase speed
    this.walkDirection.scl(DASH_SPEED_MULTIPLIER);

    Sound interactSound = ServiceLocator.getResourceService().getAsset(
            "sounds/whooshsound.mp3", Sound.class);
    interactSound.play(UserSettings.get().masterVolume*0.2f);

    body.applyLinearImpulse(new Vector2(this.walkDirection.x, 0f), body.getWorldCenter(), true);
    // Unscale the direction vector to ensure player does not infinitely dash in one direction
    this.walkDirection.scl((float) 1 / DASH_SPEED_MULTIPLIER);

  }

  /**
   * Makes the player attack.
   */
  void attack() {
    Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play(UserSettings.getMasterVolume());
  }

  /**
   * Makes the player interact
   */
  void interact() {
    Sound interactSound = ServiceLocator.getResourceService().getAsset(
            "sounds/pickupsound.mp3", Sound.class);
    interactSound.play(UserSettings.get().masterVolume);
    soundPlayed = true;
  }

  /**
   * Makes the player glide
   */
  private void glide(boolean on) {
    Body body = physicsComponent.getBody();
    boolean isOutOfJumps = (isJumping) && (isDoubleJump);

    if (on && isOutOfJumps) {
      if (body.getLinearVelocity().y < 0.5f) {

        body.setGravityScale(0.1f);
        isGliding = true;
      }
    } else {
      body.setGravityScale(1f);
      isGliding = false;
    }
  }

  /**
   * Used to activate the jetpack upgrade for the player - allows for upwards movement
   */
  private void jetpackOn() {

          isJetpackOn = true;
          isJumping = true;
          hasActivatedJetpack = true;
          jetpackSound.loop(UserSettings.get().masterVolume);

  }

  /**
   * Used to disable the upwards movement gained by the jetpack upgrade
   */
  private void jetpackOff() {
    isJetpackOn = false;
    jetpackSound.pause();
  }


  /**
   * Called when a collision involving the players starts
   *
   * @param selfFixture The fixture belonging to the player entity involved in the collision
   * @param otherFixture The fixture belonging to the other entity involved in the collision
   */
  void onCollisionStart(Fixture selfFixture, Fixture otherFixture) {

    if ("foot".equals(selfFixture.getUserData()) || "foot".equals(otherFixture.getUserData())) {
      if (isJumping || isDoubleJump) {
        Sound interactSound = ServiceLocator.getResourceService().getAsset(
                "sounds/thudsound.mp3", Sound.class);
        interactSound.play(UserSettings.get().masterVolume*0.08f);
      }
      entity.getEvents().trigger("landed");
    }
  }

  /**
   * Makes the player crouch
   */
  void crouch() {
    StandingColliderComponent standing = entity.getComponent(StandingColliderComponent.class);
    CrouchingColliderComponent crouch =
            entity.getComponent(CrouchingColliderComponent.class);
    if (crouching) {
      crouching = false;
      standing.getFixtureRef().setSensor(false);
      crouch.getFixtureRef().setSensor(true);
    } else {
      crouching = true;
      standing.getFixtureRef().setSensor(true);
      crouch.getFixtureRef().setSensor(false);
    }
    updateSpeed();
  }

  /**
   * @param moving weather the entity position will be updated or not
   */
  public void setMoving(boolean moving) {
    this.moving = moving;
  }

  /**
   * @return returns weather the entity is allowed to move
   */
  public boolean isMoving() {
    return moving;
  }

  public float getXDirection() {
    return walkDirection.x;
  }

  public float getYDirection() {
    return walkDirection.y;
  }

  public boolean getIsJumping() {
    return isJumping;
  }

  public boolean getIsDoubleJumping() {
    return isDoubleJump;
  }

  public boolean getIsCrouching() {
    return crouching;
  }

  public boolean hasSoundPlayed() {
    return soundPlayed;
  }

  public boolean hasAdrenaline() {
    return adrenaline;
  }

  public boolean hasDashed() {
    return hasDashed;
  }

  public int getJetpackFuel(){return jetpackFuel;};

  public boolean getIsJetpackOn() {return isJetpackOn;}

  public boolean getIsGliding() {return isGliding;}

  /**
   * Turns the gravity off/on for the player depending if cheats are on
   */
  private void toggleGravity() {
    Body body = physicsComponent.getBody();
    if (entity.getComponent(KeyboardPlayerInputComponent.class).getIsCheatsOn()) {
      body.setGravityScale(0f);
    } else {
      body.setGravityScale(1f);
    }
  }

  public void setWalkSpeed(int x, int y) {
      WALK_SPEED = new Vector2((float)x, (float)y);
  }

  private void gravityOff() {
    Body body = physicsComponent.getBody();
    body.setGravityScale(0f);
  }
  private void gravityOn() {
      Body body = physicsComponent.getBody();
      body.setGravityScale(1f);
  }
}

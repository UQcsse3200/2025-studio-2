package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.csse3200.game.components.*;
import com.csse3200.game.components.Component;
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
  // second
  private static final Vector2 WALK_SPEED = new Vector2(7f, 7f); // Metres
  private static final Vector2 ADRENALINE_SPEED = WALK_SPEED.cpy().scl(3);
  private static final Vector2 CROUCH_SPEED = WALK_SPEED.cpy().scl(0.3F);
    private static final Vector2 MAX_SPEED = new Vector2(3f, 3f);
    private static final float   SPRINT_MULT = 2.3f;

  private static final int DASH_SPEED_MULTIPLIER = 30;
  private static final float JUMP_IMPULSE_FACTOR = 12.5f;

  private PhysicsComponent physicsComponent;
  private StaminaComponent stamina;
  private CameraComponent cameraComponent;
  private Vector2 walkDirection = Vector2.Zero.cpy();

  private Vector2 jumpDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean adrenaline = false;
  private boolean crouching = false;

  private StandingColliderComponent standingCollider;
  private CrouchingColliderComponent crouchingCollider;


  // For Tests (Not Functionality)
  private boolean hasDashed = false;

  private boolean isJumping = false;
  private boolean isDoubleJump = false;

  private boolean soundPlayed = false;

  private CombatStatsComponent combatStatsComponent;

  // Whether player is currently holding sprint (Shift)
  private boolean wantsSprint = false;
  private Joint grappleJoint;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    combatStatsComponent = entity.getComponent(CombatStatsComponent.class);
    cameraComponent = entity.getComponent(CameraComponent.class);
    stamina = entity.getComponent(StaminaComponent.class);

    Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
    cameraComponent.resize(displayMode.width, displayMode.height, 15f);

    standingCollider = entity.getComponent(StandingColliderComponent.class);
    crouchingCollider = entity.getComponent(CrouchingColliderComponent.class);

    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);

    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("interact", this::interact);

    entity.getEvents().addListener("jump", this::jump);
    entity.getEvents().addListener("landed", this::onLand);

    entity.getEvents().addListener("toggleAdrenaline", this::toggleAdrenaline);

    entity.getEvents().addListener("dash", this::dash);

    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    entity.getEvents().addListener("gravityForPlayerOff", this::toggleGravity);

    entity.getEvents().addListener("glide", this::glide);
    entity.getEvents().addListener("grapple", this::grapple);
    //entity.getEvents().addListener("destroyGrapple", this::destoryGrapple);

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
    if (moving) {
      updateSpeed();
    }

    Body body = physicsComponent.getBody();

    if (body.getLinearVelocity().y < 0) {
      body.applyForceToCenter(new Vector2(0, -body.getMass() * 10f), true);
    }

    // Check if the player's health is currently 0, in which case, reset level
    if (combatStatsComponent.isDead()) {
      entity.requestReset();
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
    //only update the horizontal impulse
    /*float inAirControl = isJumping ? 0.2f : 1f;*/

    float deltaV = desiredVelocity.x - velocity.x;
    float maxDeltaV = MAX_ACCELERATION /*inAirControl*/ * Gdx.graphics.getDeltaTime();
    if (deltaV > maxDeltaV) deltaV = maxDeltaV;
    if (deltaV < -maxDeltaV) deltaV = -maxDeltaV;
    float impulseY;

<<<<<<< HEAD
=======
//    Gdx.app.log("Is cheats on", entity.getComponent(KeyboardPlayerInputComponent.class).getIsCheatsOn().toString());
>>>>>>> 32e454331aa8598e9089ca92af26d19a110b889a
    if (entity.getComponent(KeyboardPlayerInputComponent.class).getIsCheatsOn()) {
      float deltaVy = desiredVelocity.y - velocity.y;
      float maxDeltaVy = MAX_ACCELERATION /*inAirControl*/ * Gdx.graphics.getDeltaTime();
      deltaVy = deltaVy > maxDeltaVy ? maxDeltaVy : -maxDeltaVy;
      impulseY = deltaVy * body.getMass();
    } else {
      impulseY = 0f;
    }
    Vector2 impulse = new Vector2(deltaV * body.getMass(), impulseY);
    body.applyLinearImpulse(impulse, body.getWorldCenter(), true);



    /**
    Vector2 impulse =
            new Vector2((desiredVelocity.x - velocity.x) * inAirControl, 0).scl(body.getMass());
    body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
     */
  }

  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
      this.walkDirection.set(direction); // <- keep/make this
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
      /*body.applyForce(new Vector2(Math.abs(vel.x), impulseY), body.getWorldCenter(), true);*/

      isJumping = true;
  }

  /**
   * Called when a player lands on a surface
   *
   * This method resets the players jump state, allowing them to jump again
   */
  void onLand() {
    Body body = physicsComponent.getBody();
    //body.setLinearVelocity(body.getLinearVelocity().x, 0f);
    isJumping = false;
    isDoubleJump = false;

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
    body.applyLinearImpulse(this.walkDirection, body.getWorldCenter(), true);
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
    // do something
    Sound interactSound = ServiceLocator.getResourceService().getAsset(
            "sounds/chimesound.mp3", Sound.class);
    interactSound.play();
    soundPlayed = true;
  }

  /**
   * Makes the player glide
   */
  private void glide(boolean on) {
    Body body = physicsComponent.getBody();
    boolean isOutOfJumps = (isJumping) && (isDoubleJump);

    if (on && isOutOfJumps) {
      if (body.getLinearVelocity().y < 0.1f) {
        body.setGravityScale(0.1f);
      }
    } else {
      body.setGravityScale(1f);
    }
  }

  private void grapple() {
    Body body = physicsComponent.getBody();

    Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
    Vector2 playerPos = body.getPosition().cpy();
    Gdx.app.log("Mouse Input Pixels", Float.toString(mousePos.x));
    Gdx.app.log("Player Position Units", Float.toString(playerPos.x));


   /* float maxDistance = 10f;
    Vector2 cameraPosition = new Vector2(cameraComponent.getCamera().position.x, cameraComponent.getCamera().position.y);

    Gdx.app.log("Player position", Float.toString(playerPos.x) + " " + Float.toString(playerPos.y));

    Vector2 impulseDir = target.sub(playerPos);

    //Raycast here
    RaycastHit callback = new RaycastHit();
    ServiceLocator.getPhysicsService().getPhysics().raycast(playerPos, impulseDir, callback);

    Gdx.app.log("Direction of grapple", Float.toString(impulseDir.x));
    Vector2 grappleImpulse = impulseDir.scl(10f);

    body.applyLinearImpulse(grappleImpulse, body.getWorldCenter(), true);*/
  }


  /**
   * Called when a collision involving the players starts
   *
   * @param selfFixture The fixture belonging to the player entity involved in the collision
   * @param otherFixture The fixture belonging to the other entity involved in the collision
   */
  void onCollisionStart(Fixture selfFixture, Fixture otherFixture) {

    if ("foot".equals(selfFixture.getUserData()) || "foot".equals(otherFixture.getUserData())) {
      onLand();
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
      //PhysicsUtils.setScaledCollider(entity, 0.6f, 1f);
      //standingCollider.getFixture().setSensor(false);
      //crouchingCollider.getFixture().setSensor(true);
      standing.getFixtureRef().setSensor(false);
      crouch.getFixtureRef().setSensor(true);
    } else {
      crouching = true;
      //PhysicsUtils.setScaledCollider(entity, 0.6f, 0.5f);
      standing.getFixtureRef().setSensor(true);
      crouch.getFixtureRef().setSensor(false);
    }
    updateSpeed();
  }

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
}

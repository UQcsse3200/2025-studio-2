package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.physics.*;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Action component for interacting with the player. Player events should be initialised in create()
 * and when triggered should call methods within this class.
 */
public class PlayerActions extends Component {
  private static final Vector2 MAX_SPEED = new Vector2(6f, 6f); // Metres per
  private static final float MAX_ACCELERATION = 70f;
  // second
  private static final Vector2 WALK_SPEED = new Vector2(3f, 3f); // Metres
  private static final Vector2 ADRENALINE_SPEED = WALK_SPEED.cpy().scl(10);
  private static final int DASH_SPEED_MULTIPLIER = 4;
  private static final float JUMP_IMPULSE_FACTOR = 12.5f;

  private PhysicsComponent physicsComponent;
  private Vector2 walkDirection = Vector2.Zero.cpy();

  private Vector2 jumpDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean adrenaline = false;

  private boolean isJumping = false;
  private boolean isDoubleJump = false;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);

    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);

    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("interact", this::interact);

    entity.getEvents().addListener("jump", this::jump);
    entity.getEvents().addListener("landed", this::onLand);

    entity.getEvents().addListener("toggleAdrenaline", this::toggleAdrenaline);

    entity.getEvents().addListener("dash", this::dash);

    entity.getEvents().addListener("collisionStart", this::onCollisionStart);

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
  }

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();
    Vector2 desiredVelocity;

    System.out.println(adrenaline);
    if (adrenaline) {
      desiredVelocity = walkDirection.cpy().scl(ADRENALINE_SPEED);
    } else {
      desiredVelocity = walkDirection.cpy().scl(WALK_SPEED);
    }

    // impulse = (desiredVel - currentVel) * mass
    //only update the horizontal impulse
    float inAirControl = isJumping ? 0.2f : 1f;

    float deltaV = desiredVelocity.x - velocity.x;
    float maxDeltaV = MAX_ACCELERATION * inAirControl * Gdx.graphics.getDeltaTime();
    if (deltaV > maxDeltaV) deltaV = maxDeltaV;
    if (deltaV < -maxDeltaV) deltaV = -maxDeltaV;

    Vector2 impulse = new Vector2(deltaV * body.getMass(), 0);
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
    this.walkDirection.x = direction.x;
    moving = true;
  }

  /**
   * Stops the player from walking.
   */
  void stopWalking() {
    this.walkDirection = Vector2.Zero.cpy();
    updateSpeed();
    moving = false;
  }

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

  void onLand() {
    Body body = physicsComponent.getBody();
    body.setLinearVelocity(body.getLinearVelocity().x, 0f);
    isJumping = false;
    isDoubleJump = false;

  }

  /**
   * Boosts the players speed, `activates adrenaline`
   * @param direction The direction in which the player should move
   */
  void toggleAdrenaline(Vector2 direction) {
    this.walkDirection = direction;
    adrenaline = !adrenaline;
  }

  /**
   * Gives the player a boost of speed in the given direction
   * @param direction The direction in which the player should dash
   */
  void dash(Vector2 direction) {
    this.walkDirection = direction;
    moving = true;

    Body body = physicsComponent.getBody();

    direction.scl(DASH_SPEED_MULTIPLIER);
    body.applyLinearImpulse(direction, body.getWorldCenter(), true);
    direction.scl((float) 1 / DASH_SPEED_MULTIPLIER);
  }

  /**
   * Makes the player attack.
   */
  void attack() {
    Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play();
  }

  /**
   * Makes the player interact
   */
  void interact() {
    // do something
    Sound interactSound = ServiceLocator.getResourceService().getAsset(
            "sounds/chimesound.mp3", Sound.class);
    interactSound.play();
  }

  void onCollisionStart(Fixture selfFixture, Fixture otherFixture) {
        onLand();

  }

}

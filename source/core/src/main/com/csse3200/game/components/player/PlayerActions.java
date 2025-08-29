package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Action component for interacting with the player. Player events should be initialised in create()
 * and when triggered should call methods within this class.
 */
public class PlayerActions extends Component {
<<<<<<< Updated upstream
  private static final Vector2 MAX_SPEED = new Vector2(6f, 6f); // Metres per
  private static final float MAX_ACCELERATION = 70f;
  // second
=======
  private static final Vector2 WALK_SPEED = new Vector2(3f, 3f); // Metres per second
  private static final Vector2 DASH_SPEED = new Vector2(6f, 6f); // Metres per second
>>>>>>> Stashed changes

  private PhysicsComponent physicsComponent;
  private Vector2 walkDirection = Vector2.Zero.cpy();

  private Vector2 jumpDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean dashing = false;

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

    entity.getEvents().addListener("dash", this::dash);
    entity.getEvents().addListener("dashStop", this::stopDashing);
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
    if (dashing) {
      desiredVelocity = walkDirection.cpy().scl(DASH_SPEED);
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

      float impulseY = body.getMass() * 12.5f;

      body.applyLinearImpulse(new Vector2(0f, impulseY), body.getWorldCenter(), true);
      /*body.applyForce(new Vector2(Math.abs(vel.x), impulseY), body.getWorldCenter(), true);*/

      isJumping = true;
  }

  void onLand() {
    Body body = physicsComponent.getBody();
    //SORT OUT COLLISION WITH GROUND STUFF HERE
    isJumping = false;
    isDoubleJump = false;
  }

  void dash(Vector2 direction) {
    System.out.println("Dashing!");
    this.walkDirection = direction;
    moving = true;
    dashing = true;
  }

  /**
   * Stops the player from walking.
   */
  void stopDashing() {
    this.walkDirection = Vector2.Zero.cpy();
    updateSpeed();
    moving = false;
    dashing = false;
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
}

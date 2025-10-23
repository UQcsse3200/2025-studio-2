package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.crashinvaders.vfx.VfxManager;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.effects.ScreenTransitioningEffect;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the screen transition visual effect for the player.
 * Listens for a "transition_start" event and creates a closing vignette
 * centered on the player.
 */
public class PlayerScreenTransitionComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(PlayerScreenTransitionComponent.class);

  private VfxManager vfxManager;
  private Camera camera;
  private final ScreenTransitioningEffect effect = new ScreenTransitioningEffect();
  private float remainingDuration = 0f;
  private float totalDuration = 0f;
  private Runnable onComplete = null;

  @Override
  public void create() {
    vfxManager = ServiceLocator.getVfxService();
    camera = ServiceLocator.getRenderService().getRenderer().getCamera().getCamera();
    entity.getEvents().addListener("startTransition", this::startEffect);

//    Timer timer = new Timer();
//    timer.scheduleTask(new Timer.Task() {
//      @Override
//      public void run() {
//        startEffect(2.5f, null);
//      }
//    }, 3);
  }

  /**
   * Starts the screen transition effect if not running already
   *
   * @param duration The duration of the transition effect in seconds.
   * @param onComplete A Runnable to execute when the transition is complete.
   */
  public void startEffect(float duration, Runnable onComplete) {
    if (remainingDuration != 0) return; // Already transitioning
    logger.debug("Starting screen transition on entity {}", entity);
    vfxManager.addEffect(effect);
    remainingDuration = duration;
    totalDuration = duration;
    this.onComplete = onComplete;
    entity.getComponent(KeyboardPlayerInputComponent.class).setEnabled(false);
    entity.getComponent(PlayerActions.class).setMoving(false);
  }

  @Override
  public void update(){
    if (totalDuration == 0f) return;
    if (remainingDuration == 0f) {
      totalDuration = 0f;
      stopEffect();
      if (onComplete != null) {
        final Runnable runnable = onComplete;
        Gdx.app.postRunnable(runnable);
        this.onComplete = null;
      }
      return;
    }

    remainingDuration -= Gdx.graphics.getDeltaTime();
    if (remainingDuration <= 0f) {
      remainingDuration = 0f;
    }

    if (entity == null) {
      effect.setCenter(0.5f, 0.5f);
    } else {
      Vector3 screenPos = camera.project(new Vector3(entity.getCenterPosition(), 0));
      effect.setCenter(screenPos.x / Gdx.graphics.getWidth(), screenPos.y / Gdx.graphics.getHeight());
    }

    float progress = 1f - (remainingDuration / totalDuration);

    effect.setProgress(progress);
  }

  private void stopEffect() {
    vfxManager.removeEffect(effect);
    entity.getComponent(KeyboardPlayerInputComponent.class).setEnabled(true);
    entity.getComponent(PlayerActions.class).setMoving(true);
  }

  @Override
  public void dispose() {
    stopEffect();
    effect.dispose();
  }
}
package com.csse3200.game.components.player;

import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.ChromaticAberrationEffect;
import com.crashinvaders.vfx.effects.GaussianBlurEffect;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * Applies a screen-wide visual effect when the player dies.
 */
public class PlayerDeathEffectComponent extends Component {
  private final GaussianBlurEffect gaussianBlur = new GaussianBlurEffect(GaussianBlurEffect.BlurType.Gaussian5x5b);
  private final ChromaticAberrationEffect chromaticAberration = new ChromaticAberrationEffect(7);

  public PlayerDeathEffectComponent() {
    gaussianBlur.setAmount(100);
    gaussianBlur.setPasses(10);
    chromaticAberration.setMaxDistortion(7);
  }

  @Override
  public void create() {
    // onPlayerDeath();
    entity.getEvents().addListener("death", this::onPlayerDeath);
    entity.getEvents().addListener("reset", this::onReset);
  }

  private void onPlayerDeath() {
    VfxManager vfxManager = ServiceLocator.getVfxService();
    vfxManager.addEffect(chromaticAberration);
    //vfxManager.addEffect(gaussianBlur);

    entity.getComponent(KeyboardPlayerInputComponent.class).setEnabled(false);
  }

  private void onReset() {
    VfxManager vfxManager = ServiceLocator.getVfxService();
    //vfxManager.removeEffect(gaussianBlur);
    vfxManager.removeEffect(chromaticAberration);

    entity.getComponent(KeyboardPlayerInputComponent.class).setEnabled(true);
  }

  @Override
  public void dispose() {
    onReset();
    //gaussianBlur.dispose();
    //chromaticAberration.dispose();
  }
}
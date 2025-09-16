package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.ChromaticAberrationEffect;

/**
 * Adds a screen-wide effect applied depending on player's event listeners.
 */
public class PlayerEffectComponent extends Component {
  private final ChromaticAberrationEffect chromaticAberration = new ChromaticAberrationEffect(7);

  @Override
  public void create() {
    // When death screen is implemented maybe
    // entity.getEvents().addListener("death", this::chromaticAberration);
    //chromaticAberration();
  }

  private void chromaticAberration() {
    VfxManager vfxService = ServiceLocator.getVfxService();

    vfxService.addEffect(chromaticAberration);

    // To make this time based for eg, you can use
    // Timer.schedule(new Timer.Task() {
    //   @Override
    //   public void run() {
    //     vfxService.removeEffect(chromaticAberration);
    //   }
    // }, duration);
    // But do note that you will need to cancel this timer in some way if
    // this function is called again, otherwise the effect's duaration will
    // NOT be extended.
  }

  @Override
  public void dispose() {
    ServiceLocator.getVfxService().removeEffect(chromaticAberration);
  }
}

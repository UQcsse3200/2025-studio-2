package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.ChromaticAberrationEffect;

/**
 * Adds a screen-wide glitch effect when the player toggles their adrenaline ability.
 * The effect is temporary and will be removed after a short duration.
 */
public class PlayerEffectComponent extends Component {
  private final ChromaticAberrationEffect chromaticAberration = new ChromaticAberrationEffect(7);

  @Override
  public void create() {
    // When death screen is implemented maybe
    // entity.getEvents().addListener("death", this::chromaticAberration);
    chromaticAberration();
  }

  private void chromaticAberration() {
    VfxManager vfxService = ServiceLocator.getVfxService();

    vfxService.addEffect(chromaticAberration);
  }

  @Override
  public void dispose() {
    ServiceLocator.getVfxService().removeEffect(chromaticAberration);
  }
}

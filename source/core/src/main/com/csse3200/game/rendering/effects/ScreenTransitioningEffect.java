package com.csse3200.game.rendering.effects;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;

public class ScreenTransitioningEffect extends ShaderVfxEffect implements ChainVfxEffect {
  private static final String TEXTURE0 = "u_texture0";
  private static final String PROGRESS = "u_progress";
  private static final String SMOOTHNESS = "u_smoothness";
  private static final String CENTER_X = "u_centerX";
  private static final String CENTER_Y = "u_centerY";


  private float progress = 0.0f;
  private float smoothness = 0.25f;
  private float centerX = 0.5f;
  private float centerY = 0.5f;

  public ScreenTransitioningEffect() {
    super(VfxGLUtils.compileShader(
        Gdx.files.classpath("effects/screenspace.vert"),
        Gdx.files.classpath("effects/level-transitioning.frag"),
        ""));
    rebind();
  }

  @Override
  public void rebind() {
    program.bind();
    program.setUniformi(TEXTURE0, TEXTURE_HANDLE0);
    program.setUniformf(PROGRESS, progress);
    program.setUniformf(SMOOTHNESS, smoothness);
    program.setUniformf(CENTER_X, centerX);
    program.setUniformf(CENTER_Y, centerY);
  }

  @Override
  public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
    render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
  }

  private void render(VfxRenderContext context, VfxFrameBuffer src, VfxFrameBuffer dst) {
    // Bind src buffer's texture as a primary one.
    src.getTexture().bind(TEXTURE_HANDLE0);
    // Apply shader effect and render result to dst buffer.
    renderShader(context, dst);
  }

  /**
   * Sets the progress of this effect
   * @param progress The new progress value
   */
  public void setProgress(float progress) {
    this.progress = progress;
    setUniform(PROGRESS, progress);
  }

  /**
   * Sets the smoothness of this effect (used in smoothstep function)
   * @param smoothness the new smoothness value
   */
  public void setSmoothness(float smoothness) {
    this.smoothness = smoothness;
    setUniform(SMOOTHNESS, smoothness);
  }

  /** Specify the center, in normalized screen coordinates. */
  public void setCenter(float x, float y) {
    this.centerX = x;
    this.centerY = y;
    program.bind();
    program.setUniformf(CENTER_X, centerX);
    program.setUniformf(CENTER_Y, centerY);
  }

  /**
   * @return the x coordinate for the center of the effect
   */
  public float getCenterX() {
    return centerX;
  }

  /**
   * @return the y coordinate for the center of the effect
   */
  public float getCenterY() {
    return centerY;
  }

  /**
   * @return The current progress
   */
  public float getProgress() {
    return progress;
  }

  /**
   * @return the effect smoothness value
   */
  public float getSmoothness() {
    return smoothness;
  }
}

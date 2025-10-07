package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * A UI component that displays a tutorial image and corresponding key as text.
 */
public class TutorialComponent extends TextureRenderComponent {
  private final String keyText;
  private BitmapFont font;
  Skin skin;

  /**
   * @param imagePath Path to the texture for the action's image.
   * @param keyText The text to display for the key name
   */
  public TutorialComponent(String imagePath, String keyText) {
    super(imagePath);
    this.keyText = keyText;
  }

  @Override
  public void create() {
    super.create();
    skin = new Skin(Gdx.files.internal("commodore64/skin/uiskin.json"));
    font = skin.getFont("commodore-64");
    font.setUseIntegerPositions(false);
    super.create();
  }

  @Override
  protected void draw(SpriteBatch batch) {
    super.draw(batch);
    if (font == null) return;
    Vector2 scale = entity.getScale();
    Vector2 pos = entity.getPosition();

    font.getData().setScale(scale.x / 40);

    float textX = pos.x - scale.x / 2;
    float textY = pos.y - 0.2f;

    font.draw(batch, keyText, textX, textY, scale.x * 2, Align.center, false);
  }

  @Override
  public void dispose() {
    super.dispose();
    skin.dispose();
  }
}

package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * A UI component that displays a tutorial image and corresponding key as text.
 */
public class ActionIndicatorComponent extends TextureRenderComponent {
  private final String keyText;
  private Skin skin;
  private BitmapFont font;
  /**
   * @param imagePath Path to the texture for the action's image.
   * @param keyText The text for the action being performed (used to get the keymap)
   */
  public ActionIndicatorComponent(String imagePath, String keyText) {
    super(imagePath);
    this.keyText = keyText;
  }

  @Override
  public void create() {
    super.create();
    skin = new Skin(Gdx.files.internal("commodore64/skin/uiskin.json"));
    font = skin.getFont("commodore-64");
    font.setUseIntegerPositions(false);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    super.draw(batch);
    if (null == font) return;
    Vector2 scale = entity.getScale();
    Vector2 pos = entity.getPosition();

    font.getData().setScale(scale.x / 40);
    font.draw(batch,
        Input.Keys.toString(Keymap.getActionKeyCode(keyText)),
        pos.x - scale.x / 2, pos.y - 0.2f,
        scale.x * 2, Align.center,
        false
    );
  }

  @Override
  public void dispose() {
    super.dispose();
    skin.dispose();
  }
}
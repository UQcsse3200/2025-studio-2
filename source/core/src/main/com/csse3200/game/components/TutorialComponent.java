package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * A UI component that displays a tutorial image and corresponding key as text.
 */
public class TutorialComponent extends UIComponent {

  private final String keyText;
  private Table tutorialTable;
  private CameraComponent camera;
  private final Texture actionTexture;


  /**
   * @param imagePath Path to the texture for the action's image.
   * @param keyText The text to display for the key name
   */
  public TutorialComponent(String imagePath, String keyText) {
    this.actionTexture = ServiceLocator.getResourceService().getAsset(imagePath, Texture.class);
    this.keyText = keyText;
  }

  @Override
  public void create() {
    super.create();
    camera = ServiceLocator.getRenderService().getRenderer().getCamera();

    Image actionImage = new Image(actionTexture);
    Label keyLabel = new Label(keyText, skin, "large");

    tutorialTable = new Table();
    tutorialTable.add(actionImage).size(32, 32).padBottom(10f).row();
    tutorialTable.add(keyLabel);

    stage.addActor(tutorialTable);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector3 screenPos = camera.getCamera().project(new Vector3(entity.getCenterPosition(), 0));
    tutorialTable.setPosition(screenPos.x - tutorialTable.getWidth() / 2, screenPos.y - tutorialTable.getHeight() / 2);
  }

  @Override
  public void dispose() {
    super.dispose();
    if (tutorialTable != null) {
      tutorialTable.remove();
    }
  }

  @Override
  public int getLayer() {
    return Integer.MIN_VALUE;
  }
}

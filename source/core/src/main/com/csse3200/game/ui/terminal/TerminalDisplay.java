package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.csse3200.game.ui.UIComponent;

/**
 * A UI component for displaying the global debug terminal.
 * This class handles the visual representation of the terminal, including its
 * background, text history, and input line with a blinking cursor.
 */
public class TerminalDisplay extends UIComponent {
  private Terminal terminal;
  private Table rootTable;
  private TextArea historyArea;
  private TextArea inputField;

  @Override
  public void create() {
    super.create();
    terminal = entity.getComponent(Terminal.class);
    createActors();
  }

  /**
   * Creates the UI actors for the terminal display.
   * This sets up the visual elements but does not add them to the stage,
   * allowing the TerminalService to manage which stage it's on.
   */
  private void createActors() {
    // Create a semi-transparent background drawable
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(new Color(0, 0, 0, 0.5f)); // Black with 60% opacity
    pixmap.fill();
    Texture backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    Image background = new Image(backgroundTexture);

    TextField.TextFieldStyle defaultStyle = skin.get(TextField.TextFieldStyle.class);
    TextField.TextFieldStyle transparentStyle = new TextField.TextFieldStyle(defaultStyle);
    transparentStyle.background = null;
    transparentStyle.fontColor = Color.WHITE;

    // Create labels for history and current input
    historyArea = new TextArea("", transparentStyle);
    historyArea.setPrefRows(1);
    for (int i = 0; i < 50; i += 1) {
      historyArea.appendText("\n");
    }
    inputField = new TextArea("", transparentStyle);
    clearInput();

    Table inputTable = new Table();
    inputTable.add(new Label("> ", skin, "default")).top();
    inputTable.add(inputField).expandX().fillX().height(100);

    // Main layout table
    Table contentTable = new Table();
    contentTable.add(historyArea).expand().fill().padLeft(2f).padRight(2f);
    contentTable.row();
    contentTable.add(inputTable).padLeft(2f).padRight(2f).expandX().fillX().height(100);

    // Stack the background and content table
    rootTable = new Table();
    rootTable.setFillParent(true);
    Stack stack = new Stack();
    stack.add(background);
    stack.add(contentTable);
    rootTable.add(stack).expand().fill();
  }

  /**
   * Gets the root actor of the terminal display.
   *
   * @return The root actor.
   */
  public Actor getRoot() {
    return rootTable;
  }

  public String getInput() {
    return inputField.getText();
  }

  public TextField getInputField() {
    return inputField;
  }

  public TextArea getHistoryArea() {
    return historyArea;
  }

  public void clearInput() {
    inputField.setText("");
  }

  /**
   * Draws the terminal, setting its visibility based on the terminal's state.
   * The actual drawing is handled by the stage.
   *
   * @param batch Batch to render to.
   */
  @Override
  public void draw(SpriteBatch batch) {
    rootTable.setVisible(terminal.isOpen());
  }

  /**
   * Disposes the current root table to free up resources.
   */
  @Override
  public void dispose() {
    super.dispose();
    if (rootTable != null) {
      rootTable.remove();
    }
  }
}

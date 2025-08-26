package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.terminal.GlobalTerminalInputComponent;
import com.csse3200.game.ui.terminal.Shell;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton service that manages the game's global debug terminal.
 */
public class TerminalService implements Shell.Console {
  private static final Logger logger = LoggerFactory.getLogger(TerminalService.class);
  private final Terminal terminalComponent = new Terminal();
  private final TerminalDisplay terminalDisplay = new TerminalDisplay();
  private Stage stage;
  private final Shell shell;

  /**
   * Called on construction by the ServiceLocator.
   */
  public TerminalService() {
    shell = new Shell(this);
  }

  /**
   * Called by the RenderService when a new Stage is set.
   * This allows the terminal to attach its UI to the current screen's stage.
   * @param stage The newly active Stage.
   */
  public void setStage(Stage stage) {
    this.stage = stage;

    logger.debug("Creating global terminal UI entity");
    Entity terminalEntity = new Entity()
        .addComponent(terminalComponent)
        .addComponent(new GlobalTerminalInputComponent())
        .addComponent(terminalDisplay);

    ServiceLocator.getEntityService().register(terminalEntity);

    // Attach the UI to the new stage. This also works when switching screens.
    if (stage != null) {
      logger.debug("Attaching terminal display to new stage");
      // Remove from old parent in case it's still attached
      terminalDisplay.getRoot().remove();
      stage.addActor(terminalDisplay.getRoot());
    }
  }

  public TerminalDisplay getTerminalDisplay() {
    return terminalDisplay;
  }

  public void toggle() {
    terminalComponent.toggleIsOpen();
    terminalDisplay.getRoot().toFront();
    focusTerminalInput();

    GameTime timeSource = ServiceLocator.getTimeSource();
    if (timeSource != null) {
      if (terminalComponent.isOpen()) {
        timeSource.setTimeScale(0f);
        logger.info("Game paused by terminal");
      } else {
        timeSource.setTimeScale(1f);
        logger.info("Game resumed by terminal");
      }
    }
  }

  public void focusTerminalInput() {
    if (stage != null) {
      if (terminalComponent.isOpen()) {
        stage.setKeyboardFocus(terminalDisplay.getInputField());
      } else {
        stage.setKeyboardFocus(null);
      }
    }
  }

  public void executeCurrentCommand() {
    final String command = terminalDisplay.getInput();
    print("> " + String.join("  \n", command.split("\n")) + "\n");
    try {
      Object result = shell.eval(command);
      if (result != null) {
        print(result + "\n");
      }
    } catch (Exception e) {
      logger.error("Terminal command failed: {}", e.getMessage(), e);
      print("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage() + "\n");
    }
    terminalComponent.addCommandToHistory(command);
    terminalDisplay.clearInput();
  }

  // --- Shell.Console Implementation ---
  @Override
  public void print(Object obj) {
    if (obj != null) {
      terminalDisplay.getHistoryArea().appendText(obj.toString());
    }
  }

  @Override public String next() { return null; }
  @Override public boolean hasNext() { return false; }
  @Override public void close() {}
}

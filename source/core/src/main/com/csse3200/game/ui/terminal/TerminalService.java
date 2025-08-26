package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Static class that manages the game's global debug terminal.
 */
public class TerminalService {
  private static final Logger logger = LoggerFactory.getLogger(TerminalService.class);
  private static final Terminal terminalComponent = new Terminal();
  private static final TerminalDisplay terminalDisplay = new TerminalDisplay();
  private static Stage stage;
  private static final Shell shell = new Shell(new Shell.Console() {
    @Override public void print(Object obj) { TerminalService.print(obj); }
    @Override public String next() { return null; }
    @Override public boolean hasNext() { return false; }
    @Override public void close() {}
  });

  /**
   * Called by the RenderService when a new Stage is set.
   * This allows the terminal to attach its UI to the current screen's stage.
   *
   * @param stageValue The newly active Stage.
   */
  static public void setStage(Stage stageValue) {
    stage = stageValue;

    logger.debug("Creating global terminal UI entity");
    Entity terminalEntity = new Entity()
        .addComponent(terminalComponent)
        .addComponent(new GlobalTerminalInputComponent())
        .addComponent(terminalDisplay);

    ServiceLocator.getEntityService().register(terminalEntity);

    // Attach the UI to the new stage. This also works when switching screens.
    if (stage != null) {
      logger.debug("Attaching terminal display to new stage");
      terminalDisplay.getRoot().remove(); // Remove from old parent
      stage.addActor(terminalDisplay.getRoot());
    }
  }

  /**
   * Returns the current terminal display object
   *
   * @return TerminalDisplay
   */
  static public TerminalDisplay getTerminalDisplay() {
    return terminalDisplay;
  }

  /**
   * Toggle the terminal display on or off
   */
  static public void toggle() {
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

  /**
   * Focus the terminal input (the bottom pane where text is entered)
   */
  static public void focusTerminalInput() {
    if (stage != null) {
      if (terminalComponent.isOpen()) {
        stage.setKeyboardFocus(terminalDisplay.getInputField());
      } else {
        stage.setKeyboardFocus(null);
      }
    }
  }

  /**
   * Execute the given command in the terminal
   */
  static public void executeCurrentCommand() {
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

  /**
   * Print string representation of the give object to the terminal.
   * Nothing is printed if the object is null.
   *
   * @param obj the object to be printed
   */
  static public void print(Object obj) {
    if (obj != null) {
      terminalDisplay.getHistoryArea().appendText(obj.toString());
    }
  }
}

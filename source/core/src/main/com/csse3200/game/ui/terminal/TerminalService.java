package com.csse3200.game.ui.terminal;

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
  private static final Shell shell = Initializer.getInitializedShell();

  /* This field will be used by the shell and therefore must not be final */
  private static Float customTimeScale = 1.0f;

  private TerminalService() {
    throw new IllegalStateException("Instantiating static util class");
  }

  /**
   * This allows the terminal to attach its UI to the current screen's stage.
   **/
  static public void register() {
    logger.debug("Creating global terminal UI entity");
    Entity entity = new Entity()
        .addComponent(terminalComponent)
        .addComponent(new GlobalTerminalInputComponent())
        .addComponent(terminalDisplay);

    ServiceLocator.getEntityService().register(entity);

    // Attach the UI to the new stage. This also works when switching screens.
    logger.debug("Attaching terminal display to new stage");
    terminalDisplay.getRoot().remove(); // Remove from old parent
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
    logger.debug("Toggling terminal");
    terminalComponent.toggleIsOpen();
    terminalDisplay.getRoot().toFront();
    focusTerminalInput();

    GameTime timeSource = ServiceLocator.getTimeSource();
    if (timeSource == null) return;
    if (terminalComponent.isOpen()) {
      timeSource.setTimeScale(0f);
      logger.info("Game paused by terminal");
    } else {
      timeSource.setTimeScale((customTimeScale == null)? 1f: customTimeScale);
      logger.info("Game resumed by terminal");
    }
  }

  /**
   * Focus the terminal input (the bottom pane where text is entered)
   */
  static public void focusTerminalInput() {
    if (terminalComponent.isOpen()) {
      ServiceLocator.getRenderService().getStage().addActor(terminalDisplay.getRoot());
      ServiceLocator.getRenderService().getStage().setKeyboardFocus(terminalDisplay.getInputField());
    } else {
      ServiceLocator.getRenderService().getStage().setKeyboardFocus(null);
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

  /**
   * @return the current Shell instance
   */
  static public Shell getShell() {
    return shell;
  }
}

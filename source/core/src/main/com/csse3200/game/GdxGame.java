package com.csse3200.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.SaveConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.screens.MainMenuScreen;
import com.csse3200.game.screens.SettingsScreen;
import com.csse3200.game.screens.StatisticsScreen;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.TerminalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.Gdx.app;

/**
 * Entry point of the non-platform-specific game logic. Controls which screen is currently running.
 * The current screen triggers transitions to other screens. This works similarly to a finite state
 * machine (See the State Pattern).
 */
public class GdxGame extends Game {
  private static final Logger logger = LoggerFactory.getLogger(GdxGame.class);

  @Override
  public void create() {
    logger.info("Creating game");
    loadSettings();

    // Create keybindings
    logger.info("Assigning keybinds");
    Keymap.setKeyMapDefaults();

    // Sets background to light yellow
    Gdx.gl.glClearColor(248f/255f, 249/255f, 178/255f, 1);

    setScreen(ScreenType.MAIN_MENU);

    TerminalService.getShell().setGlobal("game", this);
  }

  /**
   * Loads the game's settings.
   */
  private void loadSettings() {
    logger.debug("Loading game settings");
    UserSettings.Settings settings = UserSettings.get();
    UserSettings.initialiseKeybinds();
    UserSettings.applySettings(settings);
  }

  public void saveLevel(MainGameScreen.Areas area, Entity player) {
    logger.debug("Saving game level");

    SaveConfig saveConfig = new SaveConfig();
    saveConfig.area = area;

    InventoryComponent inventoryComponent = player.getComponent(InventoryComponent.class);
    saveConfig.inventory = inventoryComponent.getInventoryCopy();
    saveConfig.upgrades = inventoryComponent.getUpgradesCopy();

    FileLoader.writeClass(saveConfig, "configs/save.json", FileLoader.Location.LOCAL);
  }

  /**
   * Sets the game's screen to a new screen of the provided type.
   * @param screenType screen type
   */
  public void setScreen(ScreenType screenType) {
    logger.info("Setting game screen to {}", screenType);
    Screen currentScreen = getScreen();
    if (currentScreen != null) {
      currentScreen.dispose();
    }
    setScreen(newScreen(screenType));
  }

  @Override
  public void dispose() {
    logger.debug("Disposing of current screen");
    getScreen().dispose();
    TerminalService.getShell().setGlobal("game", null);
  }

  /**
   * Create a new screen of the provided type.
   * @param screenType screen type
   * @return new screen
   */
  private Screen newScreen(ScreenType screenType) {
    return switch (screenType) {
      case MAIN_MENU -> new MainMenuScreen(this);
      case MAIN_GAME -> {
        MainGameScreen screen = new MainGameScreen(this, MainGameScreen.Areas.LEVEL_ONE);
        ServiceLocator.registerMainGameScreen(screen);
        yield screen;
      }
      case SETTINGS -> new SettingsScreen(this);
      case STATISTICS -> new StatisticsScreen(this);
      case LOAD_LEVEL -> {
        SaveConfig saveConfig = FileLoader.readClass(SaveConfig.class, "configs/save.json", FileLoader.Location.LOCAL);

        // If we don't already have a saved area, start from level one
        if (saveConfig == null) yield new MainGameScreen(this, MainGameScreen.Areas.LEVEL_ONE);
        else {
          // Check that this is a valid area, if not, start from level 1
          try {
            MainGameScreen.Areas.valueOf(saveConfig.area.toString());
          } catch (IllegalArgumentException e) {
            yield new MainGameScreen(this, MainGameScreen.Areas.LEVEL_ONE);
          }

          System.out.println("We good");
          // Load into the correct area, pass the player the old inventory.
          MainGameScreen game = new MainGameScreen(this, saveConfig.area);

          InventoryComponent inventoryComponent = game.getGameArea().getPlayer().getComponent(InventoryComponent.class);
          inventoryComponent.setInventory(saveConfig.inventory);
          inventoryComponent.setUpgrades(saveConfig.upgrades);

          yield game;
        }
      }
    };
  }

  public enum ScreenType {
    MAIN_MENU, MAIN_GAME, SETTINGS, STATISTICS, LOAD_LEVEL
  }

  /**
   * Exit the game.
   */
  public void exit() {
    app.exit();
  }
}
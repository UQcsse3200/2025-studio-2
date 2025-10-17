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
import com.csse3200.game.screens.*;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.TerminalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static com.badlogic.gdx.Gdx.app;

/**
 * Entry point of the non-platform-specific game logic. Controls which screen is currently running.
 * The current screen triggers transitions to other screens. This works similarly to a finite state
 * machine (See the State Pattern).
 */
public class GdxGame extends Game {
  public static final String SAVE_PATH = "configs/save.json";

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

  public static void saveLevel(MainGameScreen.Areas area, Entity player, String path) {
    logger.debug("Saving game level");

    SaveConfig saveConfig = new SaveConfig();
    saveConfig.area = area;

    InventoryComponent inventoryComponent = player.getComponent(InventoryComponent.class);
    saveConfig.inventory = inventoryComponent.getInventoryCopy();
    saveConfig.upgrades = inventoryComponent.getUpgradesCopy();

    FileLoader.writeClass(saveConfig, path, FileLoader.Location.LOCAL);
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
      case TUTORIAL -> new TutorialMenuScreen(this);
      case SETTINGS -> new SettingsScreen(this);
      case STATISTICS -> new StatisticsScreen(this);
      case LEADERBOARD -> new LeaderboardScreen(this);
      case LOAD_LEVEL -> {
        SaveConfig saveConfig = loadSave(SAVE_PATH);

        // Load into the correct area, pass the player the old inventory.
        MainGameScreen game = new MainGameScreen(this, saveConfig.area);
        ServiceLocator.registerMainGameScreen(game);

        InventoryComponent inventoryComponent = game.getGameArea().getPlayer().getComponent(InventoryComponent.class);
        inventoryComponent.setInventory(saveConfig.inventory);
        inventoryComponent.setUpgrades(saveConfig.upgrades);

        yield game;
      }
    };
  }

    /**
     * Return a valid save config
     *
     * @param path - save file path
     * @return valid SaveConfig
     */
    public static SaveConfig loadSave(String path) {
    SaveConfig save = FileLoader.readClass(SaveConfig.class, path, FileLoader.Location.LOCAL);
    // If the save is null, create a basic one, with default values coming from null checks.
    if (save == null) {
      save = new SaveConfig();
    }

    // Make sure area is valid
    try {
      MainGameScreen.Areas.valueOf(save.area.toString());
    } catch (IllegalArgumentException | NullPointerException e) {
      // IllegalArgumentException: Level is not in the list, start from level 1
      // NullPointerException: No level initialized, start from level 1
      save.area = MainGameScreen.Areas.LEVEL_ONE;
    }

    // Make sure inventory and upgrades exist
    if (save.inventory == null) {
      save.inventory = new HashMap<>();
    }
    if (save.upgrades == null) {
      save.upgrades = new HashMap<>();
    }

    save.area = MainGameScreen.Areas.BOSS_LEVEL;
    return save;
  }

  public enum ScreenType {
    MAIN_MENU, MAIN_GAME, SETTINGS, TUTORIAL, STATISTICS, LOAD_LEVEL, LEADERBOARD
  }

  /**
   * Exit the game.
   */
  public void exit() {
    app.exit();
  }
}
package com.csse3200.game.components.mainmenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.statisticspage.StatsTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the Main Menu Screen and does something when one of the
 * events is triggered.
 */
public class MainMenuActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuActions.class);
  private GdxGame game;

  public MainMenuActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("start", this::onStart);
    entity.getEvents().addListener("load", this::onLoad);
    entity.getEvents().addListener("leaderboard", this::onLeaderboard);
    entity.getEvents().addListener("exit", this::onExit);
    entity.getEvents().addListener("settings", this::onSettings);
    entity.getEvents().addListener("tutorial", this::onTutorial);
    entity.getEvents().addListener("stats", this::onStats);
  }

  /**
   * Swaps to the Main Game screen.
   */
  private void onStart() {
    logger.info("Start game");
    StatsTracker.startSession();
    game.setScreen(GdxGame.ScreenType.MAIN_GAME);
  }

  /**
   * Intended for loading a saved game state.
   */
  private void onLoad() {
    logger.info("Load game");
    StatsTracker.startSession();
    game.setScreen(GdxGame.ScreenType.LOAD_LEVEL);
  }

  /**
   * Displays the leaderboard names and times on screen.
   */
  private void onLeaderboard() {
    logger.info("Leaderboard Displayed");
    game.setScreen(GdxGame.ScreenType.LEADERBOARD);
  }

  /**
   * Exits the game.
   */
  private void onExit() {
    logger.info("Exit game");
    game.exit();
  }

  /**
   * Swaps to the Settings screen.
   */
  private void onSettings() {
    logger.info("Launching settings screen");
    game.setScreen(GdxGame.ScreenType.SETTINGS);
  }

  /**
   * Swaps to the Tutorial screen.
   */
  private void onTutorial() {
    logger.info("Launching tutorial screen");
    game.setScreen(GdxGame.ScreenType.TUTORIAL);
  }

  /**
   * Swaps to the Stats screen.
   */
  private void onStats() {
    logger.info("Launching stats screen");
    StatsTracker.loadStats();
    game.setScreen(GdxGame.ScreenType.STATISTICS);
  }
}

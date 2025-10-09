package com.csse3200.game.components.statisticspage;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the Stats page and does something
 * when one of the events is triggered.
 */
public class StatisticsActions extends Component {
    private GdxGame game;

    public StatisticsActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("exit", this::onExit);
        entity.getEvents().addListener("reset", this::resetStats);
    }

    /**
     * Exits the stats screen.
     */
    private void onExit() {
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

    /**
     * Resets the stats.
     */
    private void resetStats() {
        StatsTracker.resetStatsFile();
    }
}

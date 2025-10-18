package com.csse3200.game.components.leaderboardpage;

import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;

/**
 * This class listens to events relevant to the leaderboard page and does
 * something when one of the events is triggered.
 */
public class LeaderboardActions extends Component {
    private GdxGame game;

    public LeaderboardActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("exit", this::onExit);

    }

    /**
     * Exits the leaderboard screen.
     */
    private void onExit() {
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
    }
}

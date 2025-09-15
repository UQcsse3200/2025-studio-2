package com.csse3200.game.components;

import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that safely disposes of an entity after a delay.
 * This prevents concurrent modification exceptions when disposing during update loops.
 */
public class DisposalComponent extends Component {
    private final float delay;
    private GameTime timeSource;
    private long startTime;
    private boolean started = false;

    /**
     * Create a disposal component with specified delay
     * @param delay Time in seconds before disposal
     */
    public DisposalComponent(float delay) {
        this.delay = delay;
    }

    @Override
    public void create() {
        super.create();
        timeSource = ServiceLocator.getTimeSource();
        entity.getEvents().addListener("scheduleDisposal", this::startDisposal);
    }

    public void startDisposal() {
        if (started) return;
        started = true;
        startTime = timeSource.getTime();
    }

    @Override
    public void update() {
        if (!started) return;

        if (timeSource.getTimeSince(startTime) >= delay * 1000) {
            entity.dispose();
        }
        setEnabled(false);
    }
}
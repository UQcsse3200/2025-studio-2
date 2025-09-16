package com.csse3200.game.components;

/**
 * Component for ladders. Only really needed as of 16/09/25 so that ladders can be found for player movement.
 */
public class LadderComponent extends Component{
    private boolean broken;

    /**
     * Initializer for ladder component.
     */
    public LadderComponent() {
        this.broken = false;
    }

    /**
     * Would set whether the ladder is broken or not.
     * @param broken, value to set this.broken too.
     */
    public void setBroken(boolean broken) {
        this.broken = broken;
    }

    /**
     * @return Whether or not the ladder is broken.
     */
    public boolean getBroken() {
        return broken;
    }
}

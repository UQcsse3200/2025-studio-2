package com.csse3200.game.components;

public class LadderComponent extends Component{
    private boolean broken;

    public LadderComponent() {
        this.broken = false;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }

    public boolean isBroken() {
        return broken;
    }
}

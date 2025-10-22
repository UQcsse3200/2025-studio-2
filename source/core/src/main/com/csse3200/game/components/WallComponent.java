package com.csse3200.game.components;

public class WallComponent extends Component {
    private boolean slippery;

    public WallComponent() {
        this.slippery = false;
    }

    public void setSlippery(boolean slippery) {
        this.slippery = slippery;
    }

    public boolean isSlippery() {
        return slippery;
    }
}

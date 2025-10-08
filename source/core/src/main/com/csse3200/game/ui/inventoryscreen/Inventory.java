package com.csse3200.game.ui.inventoryscreen;

import com.csse3200.game.components.Component;

public class Inventory extends Component {
    public enum Tab { INVENTORY, UPGRADES, MAP }

    private Tab current = Tab.INVENTORY;

    public Tab getTab() { return current; }
    public void setTab(Tab tab) { this.current = tab; }

    @Override
    public void create() {
        // ensure a tab is always selected
        if (current == null) current = Tab.INVENTORY;
    }
}

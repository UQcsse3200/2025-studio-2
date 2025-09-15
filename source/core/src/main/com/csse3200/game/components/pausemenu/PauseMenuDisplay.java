package com.csse3200.game.components.pausemenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.inventory.InventoryNavigationComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.GdxGame;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.inventoryscreen.InventoryTab;
import com.csse3200.game.ui.inventoryscreen.ObjectivesTab;
import com.csse3200.game.ui.inventoryscreen.SettingsTab;
import com.csse3200.game.ui.inventoryscreen.UpgradesTab;

public class PauseMenuDisplay extends UIComponent {
    private MainGameScreen screen;
    private Table rootTable;
    private final GdxGame game;
    private Texture blackTexture;
    private Table tabBar;
    private Table tabContent;
    private Table bottomButtons;
    private Entity player;
    private final InventoryTab inventoryTab;
    private final UpgradesTab upgradesTab;
    private final SettingsTab settingsTab = new SettingsTab();
    private final ObjectivesTab objectivesTab;
    private InventoryNavigationComponent navigationComponent;

    public enum Tab {INVENTORY, UPGRADES, SETTINGS, OBJECTIVES}
    private Tab currentTab = Tab.INVENTORY;

    public PauseMenuDisplay(MainGameScreen screen, Entity player, GdxGame game) {
        this.screen = screen;
        this.player = player;
        this.inventoryTab = new InventoryTab(player, screen);
        this.upgradesTab = new UpgradesTab(player, screen);
        this.objectivesTab = new ObjectivesTab(screen);
        this.game = game;
    }

    @Override
    public void create() {
        super.create();

        // Initialize the navigation component
        navigationComponent = new InventoryNavigationComponent(inventoryTab);
        entity.addComponent(navigationComponent);
        
        // Wire up the navigation component with the inventory tab
        inventoryTab.setNavigationComponent(navigationComponent);

        // Add event listeners for navigation
        entity.getEvents().addListener("refreshInventoryGrid", () -> refreshInventoryGrid());

        rootTable = new Table();
        rootTable.setFillParent(true);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.7f);
        pixmap.fill();
        blackTexture = new Texture(pixmap);
        pixmap.dispose();
        Image background = new Image(blackTexture);

        Stack stack = new Stack();
        stack.add(background);

        tabContent = new Table();
        tabContent.center();
        stack.add(tabContent);

        tabBar = new Table();
        tabBar.top().padTop(10);
        addTabButton("Inventory", Tab.INVENTORY);
        addTabButton("Upgrades", Tab.UPGRADES);
        addTabButton("Objectives", Tab.OBJECTIVES);
        addTabButton("Settings", Tab.SETTINGS);
        stack.add(tabBar);

        bottomButtons = new Table();
        bottomButtons.bottom().padBottom(10);
        addBottomButton("Exit to Desktop", () -> Gdx.app.exit());
        addBottomButton("Exit to Main Menu", () -> game.setScreen(GdxGame.ScreenType.MAIN_MENU));
        addBottomButton("Restart", () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME));
        stack.add(bottomButtons);

        rootTable.add(stack).expand().fill();
        stage.addActor(rootTable);
        updateTabContent();
        setVisible(false);
    }

    // Tab button helper
    private void addTabButton(String name, Tab tab) {
        TextButton button = new TextButton(name, skin);
        button.pad(25);
        button.setWidth(150);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setTab(tab);
                screen.reflectPauseTabClick(tab);
            }
        });

        tabBar.add(button).padRight(100);
    }

    // Bottom button helper
    private void addBottomButton(String name, Runnable action) {
        TextButton button = new TextButton(name, skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        bottomButtons.add(button).padRight(25);
    }

    public void setTab(Tab tab) {
        // Disable navigation on the old tab
        if (currentTab == Tab.INVENTORY) {
            navigationComponent.disableNavigation();
            // Only unregister if the menu is visible (input component was registered)
            if (rootTable.isVisible()) {
                ServiceLocator.getInputService().unregister(navigationComponent);
            }
        }
        
        this.currentTab = tab;
        updateTabContent();
        
        // Enable navigation on the new tab if it's inventory
        if (currentTab == Tab.INVENTORY) {
            navigationComponent.enableNavigation();
            // Only register if the menu is visible
            if (rootTable.isVisible()) {
                ServiceLocator.getInputService().register(navigationComponent);
            }
        }
    }

    private void updateTabContent() {
        tabContent.clear();
        Actor ui = switch (currentTab) {
            case INVENTORY -> inventoryTab.build(skin);
            case UPGRADES -> upgradesTab.build(skin);
            case OBJECTIVES -> objectivesTab.build(skin);
            case SETTINGS -> settingsTab.build(skin);
        };
        // Ensure the returned actor from build() fills the tab content area.
        tabContent.add(ui).expand().fill();
    }

    public void setVisible(boolean visible) {
        rootTable.setVisible(visible);

        Actor minimapActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("minimap");
        if (minimapActor != null && minimapActor.getUserObject() != null && (minimapActor.getUserObject() instanceof MinimapDisplay minimapDisplay)) {
            minimapDisplay.setVisible(!visible);
        }

        if (visible) {
            rootTable.toFront();
            // Enable navigation and register input component when the pause menu becomes visible
            if (currentTab == Tab.INVENTORY) {
                navigationComponent.enableNavigation();
                ServiceLocator.getInputService().register(navigationComponent);
            }
        } else {
            // Disable navigation and unregister input component when the pause menu is hidden
            navigationComponent.disableNavigation();
            ServiceLocator.getInputService().unregister(navigationComponent);
        }
    }

    /**
     * Refreshes the inventory grid to update selection highlighting
     */
    private void refreshInventoryGrid() {
        if (currentTab == Tab.INVENTORY) {
            inventoryTab.refreshGrid();
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {}

    @Override
    public void dispose() {
        blackTexture.dispose();
        if (rootTable != null) {
            rootTable.remove();
        }
        super.dispose();
    }
}

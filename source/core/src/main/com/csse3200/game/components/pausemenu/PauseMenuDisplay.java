package com.csse3200.game.components.pausemenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.inventory.InventoryNavigationComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.statisticspage.StatsTracker;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.GdxGame;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.input.PauseMenuNavigationComponent;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.HoverEffectHelper;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.inventoryscreen.*;

import java.io.Reader;

public class PauseMenuDisplay extends UIComponent {
    private final MainGameScreen screen;
    private Table rootTable;
    private final GdxGame game;
    private Texture blackTexture;
    private Table tabContent;
    private Table bottomButtons;
    private final InventoryTab inventoryTab;
    private final UpgradesTab upgradesTab;
    private final SettingsTab settingsTab = new SettingsTab();
    private final ObjectivesTab objectivesTab;
    private final CodexTab codexTab;
    private InventoryNavigationComponent navigationComponent;
    private PauseMenuNavigationComponent pauseMenuNavigationComponent;
    private Sound buttonClickSound;

    public enum Tab {INVENTORY, UPGRADES, SETTINGS, OBJECTIVES, CODEX}
    private Tab currentTab = Tab.INVENTORY;

    public PauseMenuDisplay(MainGameScreen screen, GdxGame game) {
        this.screen = screen;

        this.inventoryTab = new InventoryTab(screen);
        this.upgradesTab = new UpgradesTab(screen);
        this.objectivesTab = new ObjectivesTab(screen);
        this.codexTab = new CodexTab(this);
        this.game = game;
    }


    /**
     * @return the current stage in use
     */
    public Stage getStage() {
        return stage;
    }

    @Override
    public void create() {
        super.create();

        buttonClickSound = ServiceLocator.getResourceService()
                .getAsset("sounds/buttonsound.mp3", Sound.class);

        // Initialize the inventory navigation component
        navigationComponent = new InventoryNavigationComponent(inventoryTab);
        entity.addComponent(navigationComponent);
        navigationComponent.create();
        navigationComponent.disableNavigation();
        // Wire up the navigation component with the inventory tab
        inventoryTab.setNavigationComponent(navigationComponent);

        // Initialize the pause menu navigation component
        pauseMenuNavigationComponent = new PauseMenuNavigationComponent(this);
        entity.addComponent(pauseMenuNavigationComponent);
        pauseMenuNavigationComponent.create();
        pauseMenuNavigationComponent.setEnabled(false);

        // Add event listeners for navigation
        entity.getEvents().addListener("refreshInventoryGrid", this::refreshInventoryGrid);

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

        Table tabBar = new Table();
        tabBar.top().padTop(10);
        stack.add(tabBar);

        bottomButtons = new Table();
        bottomButtons.bottom().padBottom(10);
        addBottomButton("Settings", Tab.SETTINGS);
        addBottomButton("Exit to Desktop", () -> {
            StatsTracker.endSession();
            Gdx.app.exit();
        });
        addBottomButton("Exit to Main Menu", () -> {
            StatsTracker.endSession();
            game.setScreen(GdxGame.ScreenType.MAIN_MENU);
        });
        addBottomButton("Restart", () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME));
        addBottomButton("Save level", () ->
                game.saveLevel(screen.getAreaEnum(), screen.getGameArea().getPlayer(), GdxGame.savePath));
        stack.add(bottomButtons);

        rootTable.add(stack).expand().fill();
        stage.addActor(rootTable);
        updateTabContent();
        setVisible(false);
    }

    // Bottom button helper
    private void addBottomButton(String name, Runnable action) {
        TextButton button = new TextButton(name, skin, "settingsMenu");
        button.setTransform(true);
        button.setOrigin(Align.center);
        HoverEffectHelper.applyHoverEffects(java.util.Collections.singletonList(button));
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buttonClickSound.play(UserSettings.get().masterVolume);
                action.run();
            }
        });
        bottomButtons.add(button).padRight(25);
    }
    //bottom button helper for Codex Button
    private void addBottomButton(String name, Tab tab) {
        TextButton button = new TextButton(name, skin, "settingsMenu");
        button.setTransform(true);
        button.setOrigin(Align.center);
        HoverEffectHelper.applyHoverEffects(java.util.Collections.singletonList(button));
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buttonClickSound.play(UserSettings.get().masterVolume);
                setTab(tab);
                screen.reflectPauseTabClick(tab);
            }
        });
        bottomButtons.add(button).padRight(25);
    }

    public void setTab(Tab tab) {
        this.currentTab = tab;
        updateTabContent();

        if (currentTab == Tab.INVENTORY) {
            navigationComponent.enableNavigation();
        } else {
            navigationComponent.disableNavigation();
        }
        pauseMenuNavigationComponent.setEnabled(currentTab != Tab.SETTINGS);
    }

    public MainGameScreen getScreen() {
        return this.screen;
    }

    public Tab getNextTab() {
        return switch (currentTab) {
            case INVENTORY -> Tab.UPGRADES;
            case UPGRADES -> Tab.OBJECTIVES;
            case OBJECTIVES -> Tab.INVENTORY;
            case SETTINGS -> Tab.SETTINGS;
            case CODEX -> Tab.CODEX;
        };
    }

    public Tab getPrevTab() {
        return switch (currentTab) {
            case INVENTORY -> Tab.OBJECTIVES;
            case UPGRADES -> Tab.INVENTORY;
            case OBJECTIVES -> Tab.UPGRADES;
            case SETTINGS -> Tab.SETTINGS;
            case CODEX -> Tab.CODEX;
        };
    }

    public Tab getCurrentTab() {
        return currentTab;
    }

    private void updateTabContent() {
        tabContent.clear();
        bottomButtons.clear();
        Actor ui = switch (currentTab) {
            case INVENTORY -> inventoryTab.build(skin);
            case UPGRADES -> upgradesTab.build(skin);
            case OBJECTIVES -> objectivesTab.build(skin);
            case SETTINGS -> settingsTab.build(skin);
            case CODEX -> codexTab.build(skin);
        };
        // Ensure the returned actor from build() fills the tab content area.
        tabContent.add(ui).expand().fill();

        // Only add settings tab while not in settings
        if (currentTab != Tab.SETTINGS) {
            addBottomButton("Settings", Tab.SETTINGS);
        }
        if (currentTab != Tab.CODEX) {
            addBottomButton("Codex", Tab.CODEX);
        }
        addBottomButton("Exit to Desktop", () -> {
            StatsTracker.endSession();
            Gdx.app.exit();
        });
        addBottomButton("Exit to Main Menu", () -> {
            StatsTracker.endSession();
            game.setScreen(GdxGame.ScreenType.MAIN_MENU);
        });
        addBottomButton("Restart", () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME));
        addBottomButton("Save level", () ->
                game.saveLevel(screen.getAreaEnum(), screen.getGameArea().getPlayer(), GdxGame.savePath));
    }

    public void setVisible(boolean visible) {
        rootTable.setVisible(visible);

        Actor minimapActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("minimap");
        if (minimapActor != null && minimapActor.getUserObject() != null && (minimapActor.getUserObject() instanceof MinimapDisplay minimapDisplay)) {
            minimapDisplay.setVisible(!visible);
        }
        Actor healthActor  = ServiceLocator.getRenderService().getStage().getRoot().findActor("health");
        if (healthActor != null) {
            healthActor.setVisible(!visible);
        }
        Actor staminaActor  = ServiceLocator.getRenderService().getStage().getRoot().findActor("stamina");
        if (healthActor != null) {
          staminaActor.setVisible(!visible);
        }
        Actor exitActor =  ServiceLocator.getRenderService().getStage().getRoot().findActor("exit");
        if (exitActor != null) {
            exitActor.setVisible(!visible);
        }
        Actor titleActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("title");
        if (titleActor != null) {
            titleActor.setVisible(!visible);
        }

        Entity player = screen.getGameArea().getPlayer();
        if (visible) {
            rootTable.toFront();
            player.getComponent(KeyboardPlayerInputComponent.class).setEnabled(false);
            pauseMenuNavigationComponent.setEnabled(currentTab != Tab.SETTINGS);

            // Enable navigation and register input component when the pause menu becomes visible
            if (currentTab == Tab.INVENTORY) {
                navigationComponent.enableNavigation();
            } else {
                navigationComponent.disableNavigation();
            }
        } else {
            // Disable navigation and unregister input component when the pause menu is hidden
            navigationComponent.disableNavigation();
            KeyboardPlayerInputComponent playerInputComponent = player.getComponent(KeyboardPlayerInputComponent.class);
            playerInputComponent.setEnabled(true);
            playerInputComponent.resetInputState();

            pauseMenuNavigationComponent.setEnabled(false);

            if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                pauseMenuNavigationComponent.keyUp(Input.Keys.Q);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                pauseMenuNavigationComponent.keyUp(Input.Keys.E);
            }

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

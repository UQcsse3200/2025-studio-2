package com.csse3200.game.components.pausemenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.inventoryscreen.InventoryTab;
import com.csse3200.game.ui.inventoryscreen.MapTab;
import com.csse3200.game.ui.inventoryscreen.UpgradesTab;

public class PauseMenuDisplay extends UIComponent {
    private MainGameScreen screen;
    private Table rootTable;

    private Texture blackTexture;
    private Table tabBar;
    private Table tabContent;
    private Table bottomButtons;

    private Entity player;

    private final InventoryTab inventoryTab;
    private final UpgradesTab upgradesTab = new UpgradesTab();
    private final MapTab mapTab = new MapTab();

    public enum Tab {INVENTORY, UPGRADES, SETTINGS, MAP}
    private Tab currentTab = Tab.INVENTORY;

    public PauseMenuDisplay(MainGameScreen screen, Entity player) {
        this.screen = screen;
        this.player = player;
        this.inventoryTab = new InventoryTab(player);
    }

    @Override
    public void create() {
        super.create();

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
        addTabButton("Settings", Tab.SETTINGS);
        addTabButton("Map", Tab.MAP);
        stack.add(tabBar);

        bottomButtons = new Table();
        bottomButtons.bottom().padBottom(10);
        addBottomButton("Exit to Desktop", () -> Gdx.app.exit());
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
        bottomButtons.add(button).padRight(10);
    }

  /**
   * Switch tabs to the specified tab.
   *
   * @param tab the tab to which we should switch.
   */
  public void setTab(Tab tab) {
        this.currentTab = tab;
        updateTabContent();
    }

    private void updateTabContent() {
        tabContent.clear();
        Actor ui = switch (currentTab) {
            case INVENTORY -> inventoryTab.build(skin);
            case UPGRADES -> upgradesTab.build(skin);
            case MAP -> mapTab.build(skin);
            default -> new Label("SETTINGS", skin);
        };
        tabContent.add(ui).center();
    }

  /**
   * Show/hide overlay.
   *
   * @param visible true makes the pause menu visible, false sets it to invisible.
   */
    public void setVisible(boolean visible) {
        rootTable.setVisible(visible);
        if (visible) {
            rootTable.toFront();
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

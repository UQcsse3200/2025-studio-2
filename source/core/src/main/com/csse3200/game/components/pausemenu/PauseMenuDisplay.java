package com.csse3200.game.components.pausemenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.ui.UIComponent;

public class PauseMenuDisplay extends UIComponent {
    private MainGameScreen screen;
    private boolean visible = false;

    private Texture blackTexture;
    private Image background;

    private Table tabBar;
    private Table tabContent;
    private Table bottomButtons;

    public enum Tab {INVENTORY, UPGRADES, SETTINGS, MAP}
    private Tab currentTab = Tab.INVENTORY;

    public PauseMenuDisplay (MainGameScreen screen) {
        this.screen = screen;
    }

    @Override
    public void create() {
        super.create();

        // Create black overlay
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.7f);
        pixmap.fill();
        pixmap.drawRectangle(1, 1, 30, 30);
        blackTexture = new Texture(pixmap);
        pixmap.dispose();

        background = new Image(blackTexture);
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        background.setVisible(false);
        stage.addActor(background);

        // Tab bar at top
        tabBar = new Table();
        tabBar.top().padTop(10);
        tabBar.setFillParent(true);
        stage.addActor(tabBar);

        addTabButton("Inventory", Tab.INVENTORY);
        addTabButton("Upgrades", Tab.UPGRADES);
        addTabButton("Settings", Tab.SETTINGS);
        addTabButton("Map", Tab.MAP);

        // Tab content area
        tabContent = new Table();
        tabContent.center();
        tabContent.setFillParent(true);
        stage.addActor(tabContent);

        updateTabContent();

        // Bottom buttons
        bottomButtons = new Table();
        bottomButtons.bottom().padBottom(10);
        bottomButtons.setFillParent(true);
        stage.addActor(bottomButtons);

        //addBottomButton("Exit to Menu", () -> screen.exit()));
        addBottomButton("Exit to Desktop", () -> Gdx.app.exit());
        //addBottomButton("Restart", () -> screen.restart());

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

    // Switch tabs
    public void setTab(Tab tab) {
        this.currentTab = tab;
        updateTabContent();
    }

    private void updateTabContent() {
        tabContent.clear();
        Label label = new Label(currentTab.name(), skin);
        tabContent.add(label);
    }

    // Show/hide overlay
    public void setVisible(boolean visible) {
        this.visible = visible;
        background.setVisible(visible);
        tabBar.setVisible(visible);
        tabContent.setVisible(visible);
        bottomButtons.setVisible(visible);
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    protected void draw(SpriteBatch batch) {
    }

    @Override
    public void dispose() {
        blackTexture.dispose();
    }
}

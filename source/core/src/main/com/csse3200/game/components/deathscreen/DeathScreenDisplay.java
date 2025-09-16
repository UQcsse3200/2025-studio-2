package com.csse3200.game.components.deathscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * A UI component for displaying the death screen overlay when the player dies.
 * Shows a semi-transparent background with death message and options to restart or exit.
 * When visible, blocks all other input including pause menu and stops background music.
 */
public class DeathScreenDisplay extends UIComponent {
    private Table rootTable;
    private final GdxGame game;
    private Texture blackTexture;
    private InputComponent inputBlocker;

    public DeathScreenDisplay(MainGameScreen screen, Entity player, GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        super.create();

        rootTable = new Table();
        rootTable.setFillParent(true);

        // Create semi-transparent background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.8f); // Darker than pause menu for dramatic effect
        pixmap.fill();
        blackTexture = new Texture(pixmap);
        pixmap.dispose();
        Image background = new Image(blackTexture);

        Stack stack = new Stack();
        stack.add(background);

        // Create main content table
        Table contentTable = new Table();
        contentTable.center();

        // Death message
        Label deathLabel = new Label("YOU DIED", skin, "large");
        contentTable.add(deathLabel).padBottom(30f);
        contentTable.row();

        // Buttons table
        Table buttonsTable = new Table();
        buttonsTable.center();

        // Restart button
        TextButton restartButton = new TextButton("Restart Level", skin);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(GdxGame.ScreenType.MAIN_GAME);
            }
        });
        buttonsTable.add(restartButton).padRight(20f).minWidth(150f);

        // Main menu button
        TextButton mainMenuButton = new TextButton("Main Menu", skin);
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        });
        buttonsTable.add(mainMenuButton).padRight(20f).minWidth(150f);

        // Exit button
        TextButton exitButton = new TextButton("Exit Game", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        buttonsTable.add(exitButton).minWidth(150f);

        contentTable.add(buttonsTable);
        stack.add(contentTable);

        rootTable.add(stack).expand().fill();
        stage.addActor(rootTable);
        
        // Initially hidden
        setVisible(false);
    }

    /**
     * Shows or hides the death screen overlay
     * @param visible true to show, false to hide
     */
    public void setVisible(boolean visible) {
        rootTable.setVisible(visible);

        if (visible) {
            rootTable.toFront();
            // Hide other UI elements when death screen is shown
            hideOtherUIElements();
            // Block all input except death screen interactions
            blockAllInput();
            // Pause background music
            pauseBackgroundMusic();
        } else {
            // Show other UI elements when death screen is hidden
            showOtherUIElements();
            // Unblock input
            unblockAllInput();
            // Resume background music
            resumeBackgroundMusic();
        }
    }

    private void hideOtherUIElements() {
        // Hide minimap
        Actor minimapActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("minimap");
        if (minimapActor != null) {
            minimapActor.setVisible(false);
        }
        
        // Hide health display
        Actor healthActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("health");
        if (healthActor != null) {
            healthActor.setVisible(false);
        }
        
        // Hide stamina display
        Actor staminaActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("stamina");
        if (staminaActor != null) {
            staminaActor.setVisible(false);
        }

        // Hide exit button
        Actor exitActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("exit");
        if (exitActor != null) {
            exitActor.setVisible(false);
        }

        // Hide title
        Actor titleActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("title");
        if (titleActor != null) {
            titleActor.setVisible(false);
        }
    }

    private void showOtherUIElements() {
        // Show minimap
        Actor minimapActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("minimap");
        if (minimapActor != null) {
            minimapActor.setVisible(true);
        }
        
        // Show health display
        Actor healthActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("health");
        if (healthActor != null) {
            healthActor.setVisible(true);
        }
        
        // Show stamina display
        Actor staminaActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("stamina");
        if (staminaActor != null) {
            staminaActor.setVisible(true);
        }

        // Show exit button
        Actor exitActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("exit");
        if (exitActor != null) {
            exitActor.setVisible(true);
        }

        // Show title
        Actor titleActor = ServiceLocator.getRenderService().getStage().getRoot().findActor("title");
        if (titleActor != null) {
            titleActor.setVisible(true);
        }
    }

    /**
     * Blocks all input except death screen interactions by registering a high-priority input component
     */
    private void blockAllInput() {
        // Create an input component that consumes all keyboard input to prevent pause menu access
        this.inputBlocker = new InputComponent(100) { // High priority to consume input first
            @Override
            public boolean keyDown(int keycode) {
                // Consume all keyboard input to block pause menu and other controls
                return true;
            }
            
            @Override
            public boolean keyUp(int keycode) {
                // Consume all keyboard input to block pause menu and other controls
                return true;
            }
        };
        
        ServiceLocator.getInputService().register(this.inputBlocker);
    }

    /**
     * Removes the input blocker to restore normal input handling
     */
    private void unblockAllInput() {
        // Remove the input blocker if it exists
        if (this.inputBlocker != null) {
            ServiceLocator.getInputService().unregister(this.inputBlocker);
            this.inputBlocker = null;
        }
    }
    
    /**
     * Pauses background music when death screen is shown
     */
    private void pauseBackgroundMusic() {
        try {
            Music music = ServiceLocator.getResourceService().getAsset("sounds/BGM_03_mp3.mp3", Music.class);
            if (music != null && music.isPlaying()) {
                music.pause();
            }
        } catch (Exception e) {
            // Music asset may not be loaded - ignore
        }
    }
    
    /**
     * Resumes background music when death screen is hidden  
     */
    private void resumeBackgroundMusic() {
        try {
            Music music = ServiceLocator.getResourceService().getAsset("sounds/BGM_03_mp3.mp3", Music.class);
            if (music != null && !music.isPlaying()) {
                music.play();
            }
        } catch (Exception e) {
            // Music asset may not be loaded - ignore
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // Drawing is handled by the stage
    }

    @Override
    public void dispose() {
        if (blackTexture != null) {
            blackTexture.dispose();
        }
        if (rootTable != null) {
            rootTable.remove();
        }
        unblockAllInput();
        super.dispose();
    }
}
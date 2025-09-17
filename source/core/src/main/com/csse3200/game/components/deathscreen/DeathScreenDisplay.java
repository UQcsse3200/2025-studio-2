package com.csse3200.game.components.deathscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.TypingListener;

import java.util.Random;
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
    private TypingLabel typewriterLabel;
    private Table buttonsTable;
    private String[] deathPrompts;
    private final Random random = new Random();
    private Container<TypingLabel> typewriterContainer;
    private final MainGameScreen screen;

    public DeathScreenDisplay(MainGameScreen screen, Entity player, GdxGame game) {
        this.game = game;
        this.screen = screen;
        loadDeathPrompts();
    }

    /**
     * Load death prompts from the text file
     */
    private void loadDeathPrompts() {
        try {
            String fileContent = Gdx.files.internal("deathscreen-prompts.txt").readString();
            deathPrompts = fileContent.split("\\r?\\n");

            // Remove empty lines
            java.util.List<String> validPrompts = new java.util.ArrayList<>();
            for (String prompt : deathPrompts) {
                String trimmed = prompt.trim();
                if (trimmed.length() > 0) {
                    validPrompts.add(trimmed);
                }
            }
            deathPrompts = validPrompts.toArray(new String[0]);
        } catch (Exception e) {
            // Fallback to default message if file can't be read
            deathPrompts = new String[]{"Your journey ends here..."};
        }
    }

    /**
     * Get a random death prompt
     */
    private String getRandomDeathPrompt() {
        if (deathPrompts == null || deathPrompts.length == 0) {
            return "Your journey ends here...";
        }
        return deathPrompts[random.nextInt(deathPrompts.length)];
    }

    /**
     * Strips TypingLabel markup tags from text for accurate width measurement
     */
    private String stripMarkupTags(String text) {
        // Remove all TypingLabel markup tags like {SICK}, {RAINBOW}, {COLOR}, etc.
        return text.replaceAll("\\{[^}]*\\}", "");
    }

    /**
     * Calculate the offset needed to center the typewriter effect
     */
    private float calculateCenterOffset(String text) {
        // Strip markup tags to get the actual rendered text
        String cleanText = stripMarkupTags(text);
        
        // Get the font from the label style
        Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);
        BitmapFont font = labelStyle.font;
        
        // Measure the clean text width (without markup tags)
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, cleanText);
        float textWidth = layout.width;
        
        // Return half the width as the offset (negative to move left)
        return -textWidth / 2f;
    }

    @Override
    public void create() {
        super.create();

        rootTable = new Table();
        rootTable.setFillParent(true);

        // Create semi-transparent background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.2f); // Darker than pause menu for dramatic effect
        pixmap.fill();
        blackTexture = new Texture(pixmap);
        pixmap.dispose();
        Image background = new Image(blackTexture);

        Stack stack = new Stack();
        stack.add(background);

        // Create main content table
        Table contentTable = new Table();
        contentTable.center();

        // Death message - larger, red text with more separation
        Label deathLabel = new Label("YOU DIED", skin, "large");
        deathLabel.setColor(Color.RED); // Make it red
        deathLabel.setFontScale(1.5f); // Make it bigger
        contentTable.add(deathLabel).padBottom(50f); // More separation
        contentTable.row();

        // Typewriter text (will be set to random prompt when visible)
        typewriterLabel = new TypingLabel("", skin);
        typewriterLabel.setAlignment(com.badlogic.gdx.utils.Align.left); // Left align for typewriter effect
        typewriterLabel.pause(); // Start paused, will restart when visible

        // Put the label in a container so we can offset it
        typewriterContainer = new Container<>(typewriterLabel);
        typewriterContainer.left(); // Align container contents to left
        typewriterContainer.fillX(); // Fill available width so we can position properly

        // Add listener to detect when typewriter finishes
        typewriterLabel.setTypingListener(new TypingListener() {
            @Override
            public void event(String event) {
                // When typing finishes, start button fade-in
                if ("{ENDCOLOR}".equals(event) || event.equals("{END}")) {
                    if (buttonsTable != null) {
                        buttonsTable.addAction(Actions.fadeIn(1f));
                    }
                }
            }

            @Override
            public void end() {
                // This is called when typing animation completes
                if (buttonsTable != null) {
                    buttonsTable.addAction(Actions.fadeIn(1.5f));
                }
            }

            @Override
            public void onChar(long ch) {
                // Character typed - not needed for our use case
            }

            @Override
            public String replaceVariable(String str) {
                // Variable replacement - not needed for our use case
                return str;
            }
        });

        contentTable.add(typewriterContainer).center().width(400f).padBottom(30f);
        contentTable.row();

        // Buttons table - initially invisible, moved down with more spacing
        this.buttonsTable = new Table();
        this.buttonsTable.center();
        this.buttonsTable.getColor().a = 0f; // Start transparent for fade-in effect

        // Restart button - taller and more spaced
        TextButton restartButton = new TextButton("Restart Level", skin);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screen.reset();
                setVisible(false);
            }
        });
        this.buttonsTable.add(restartButton).padRight(30f).minWidth(180f).minHeight(50f);

        // Main menu button - taller and more spaced
        TextButton mainMenuButton = new TextButton("Main Menu", skin);
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        });
        this.buttonsTable.add(mainMenuButton).padRight(30f).minWidth(180f).minHeight(50f);

        // Exit button - taller
        TextButton exitButton = new TextButton("Exit Game", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        this.buttonsTable.add(exitButton).minWidth(180f).minHeight(50f);

        contentTable.add(this.buttonsTable).padTop(30f); // Move buttons down
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
            
            // Set random death prompt and calculate center offset for typewriter effect
            if (typewriterLabel != null && typewriterContainer != null) {
                String randomPrompt = getRandomDeathPrompt();
                typewriterLabel.setText(randomPrompt);
                
                // Calculate offset to center the final text position
                float centerOffset = calculateCenterOffset(randomPrompt);
                
                // Reset all padding and apply new offset
                typewriterContainer.pad(0f);
                typewriterContainer.padLeft(200f + centerOffset);
                
                // Force layout recalculation
                typewriterContainer.invalidate();
                typewriterContainer.validate();
                
                typewriterLabel.restart();
                typewriterLabel.resume();
            }
            
            // Reset button animation - no delay, wait for typewriter listener
            if (buttonsTable != null) {
                buttonsTable.getColor().a = 0f;
                buttonsTable.clearActions();
                // Animation will be triggered by typewriter listener
            }
        } else {
            // Re-enable player input
            screen.getGameArea().getPlayer().getComponent(KeyboardPlayerInputComponent.class).setEnabled(true);

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
      screen.getGameArea().getPlayer().getComponent(KeyboardPlayerInputComponent.class).setEnabled(false);
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
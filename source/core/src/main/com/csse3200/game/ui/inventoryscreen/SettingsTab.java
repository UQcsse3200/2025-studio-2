package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Settings tab for the pause menu inventory screen.
 * Provides access to commonly adjusted game settings during gameplay.
 * 
 * Features included:
 * - Master Volume: Control overall game volume (0-100%)
 * - Music Volume: Control background music volume (0-100%)
 * - Key Bindings: Interactive display of all current keybinds with rebinding functionality
 * 
 * This is designed for quick in-game access to essential settings. For advanced display 
 * settings (FPS, fullscreen, VSync, resolution), use the main settings menu instead.
 * 
 * The keybind section dynamically loads all from the Keymap,
 * including player controls and pause menu shortcuts.
 */
public class SettingsTab implements InventoryTabInterface {
    
    // UI Components
    private Slider brightnessSlider;
    private Slider masterVolumeSlider;
    private Slider musicVolumeSlider;
    
    // Labels for real-time value updates
    private Label brightnessValue;
    private Label masterVolumeValue;
    private Label musicVolumeValue;

    private Sound buttonClickSound;

    private static final String[] backgroundSongs = {
            "sounds/gamemusic.mp3",
            "sounds/CircuitGoodness.mp3",
            "sounds/KindaLikeTycho.mp3",
            "sounds/Flow.mp3",
            "sounds/LIKEDACIRCUIT.mp3",
            "sounds/Siiiiiiiiiick bounce 1.mp3"
    };

    // Key binding management  
    private final Map<String, TextButton> keyBindButtons = new HashMap<>();
    
    // Keybind rebinding state
    private String currentlyRebinding = null;
    private String originalButtonText = null;
    private final Map<String, Integer> pendingKeybinds = new HashMap<>();
    private InputListener rebindingListener = null;
    private static final Logger logger = LoggerFactory.getLogger(SettingsTab.class);
    private static final String PERCENTAGE_FORMAT_LITERAL = "%.0f%%";
    @Override
    public Actor build(Skin skin) {
        buttonClickSound = ServiceLocator.getResourceService()
                .getAsset("sounds/buttonsound.mp3", Sound.class);

        // Get current settings
        UserSettings.Settings settings = UserSettings.get();
        
        // Create main container
        Table mainTable = new Table();
        mainTable.center();
        
        // Create settings table
        Table settingsTable = createSettingsTable(skin, settings);
        mainTable.add(settingsTable).expand().fill();
        
        return mainTable;
    }

    private Table createSettingsTable(Skin skin, UserSettings.Settings settings) {
        Table table = new Table();

        // Brightness Setting
        Label brightnessLabel = new Label("Brightness:", skin);
        brightnessSlider = new Slider(0f, 1f, 0.05f, false, skin);
        brightnessSlider.setValue(settings.getBrightnessValue());
        brightnessValue = new Label(String.format(PERCENTAGE_FORMAT_LITERAL, settings.getBrightnessValue() * 100), skin);

        Table brightnessTable = new Table();
        brightnessTable.add(brightnessSlider).width(150).left();
        brightnessTable.add(brightnessValue).left().padLeft(10f);

        table.add(brightnessLabel).right().padRight(15f);
        table.add(brightnessTable).left();
        table.row().padTop(10f);

        // Master Volume Setting
        Label masterVolumeLabel = new Label("Master Volume:", skin);
        masterVolumeSlider = new Slider(0f, 1f, 0.05f, false, skin);
        masterVolumeSlider.setValue(settings.masterVolume);
        masterVolumeValue = new Label(String.format(PERCENTAGE_FORMAT_LITERAL, settings.masterVolume * 100), skin);

        Table masterVolumeTable = new Table();
        masterVolumeTable.add(masterVolumeSlider).width(150).left();
        masterVolumeTable.add(masterVolumeValue).left().padLeft(10f);

        table.add(masterVolumeLabel).right().padRight(15f);
        table.add(masterVolumeTable).left();
        table.row().padTop(10f);

        // Music Volume Setting
        Label musicVolumeLabel = new Label("Music Volume:", skin);
        musicVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicVolumeSlider.setValue(settings.musicVolume);
        musicVolumeValue = new Label(String.format(PERCENTAGE_FORMAT_LITERAL, settings.musicVolume * 100), skin);

        Table musicVolumeTable = new Table();
        musicVolumeTable.add(musicVolumeSlider).width(150).left();
        musicVolumeTable.add(musicVolumeValue).left().padLeft(10f);

        table.add(musicVolumeLabel).right().padRight(15f);
        table.add(musicVolumeTable).left();
        table.row().padTop(20f);

        // Key Bindings Section
        Label keyBindingsLabel = new Label("Key Bindings:", skin, "title");
        table.add(keyBindingsLabel).colspan(2).center().padBottom(10f);
        table.row();

        addKeyBindingControls(table, skin);
        table.row().padTop(20f);

        // Apply button - better styling
        TextButton applyBtn = new TextButton("Apply", skin, "settingsMenu");
        applyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonClickSound.play(UserSettings.get().masterVolume);
                applyChanges();
            }
        });

        table.add(applyBtn).colspan(2).center().width(100).height(40).pad(10f);

        // Add listeners for real-time value updates
        setupListeners();

        return table;
    }
    private void setupListeners() {
        // Brightness slider listener
        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = brightnessSlider.getValue();
                brightnessValue.setText(String.format(PERCENTAGE_FORMAT_LITERAL, value * 100));

                logger.info("[UI] Brightness slider moved -> {} ({}%)", value, (int)(value * 100));
            }
        });

        // Master volume slider listener
        masterVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = masterVolumeSlider.getValue();
                masterVolumeValue.setText(String.format(PERCENTAGE_FORMAT_LITERAL, value * 100));

                //  Apply live change
                updateCurrentMusicVolume();
                logger.info("[UI] Master slider moved -> {} ({}%)", value, (int)(value * 100));
            }
        });

        // Music volume slider listener
        musicVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = musicVolumeSlider.getValue();
                musicVolumeValue.setText(String.format(PERCENTAGE_FORMAT_LITERAL, value * 100));

                //  Apply live change
//                updateCurrentMusicVolume();
//                logger.info("[UI] Music slider moved -> {} ({}%)", value, (int)(value * 100));
            }
        });
    }



    private void addKeyBindingControls(Table table, Skin skin) {
        // Clear existing buttons
        keyBindButtons.clear();
        
        // Get all actions from keymap and create buttons for each
        Map<String, Integer> keyMap = Keymap.getKeyMap();
        
        for (Map.Entry<String, Integer> entry : keyMap.entrySet()) {
            String actionName = entry.getKey();
            int currentKeyCode = entry.getValue();
            
            table.row().padTop(5f);
            
            // Action name label
            String displayName = formatActionName(actionName);
            Label actionLabel = new Label(displayName + ":", skin);
            table.add(actionLabel).right().padRight(15f);
            
            // Current key display button
            TextButton keyButton = new TextButton(Input.Keys.toString(currentKeyCode), skin, "settingsMenu");
            keyButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    buttonClickSound.play(UserSettings.get().masterVolume);
                    startRebinding(actionName, keyButton);
                }
            });
            
            table.add(keyButton).width(200).height(30).left();
            
            // Store the button with the action name as key
            keyBindButtons.put(actionName, keyButton);
        }
        
        // Add reset to defaults button
        table.row().padTop(15f);
        TextButton resetButton = new TextButton("Restore Defaults", skin, "settingsMenu");
        resetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonClickSound.play(UserSettings.get().masterVolume);
                resetKeybindsToDefaults();
            }
        });
        table.add(resetButton).colspan(2).center().width(300).height(30);
    }
    
    /**
     * Start the rebinding process for a specific action
     * @param actionName The action to rebind
     * @param button The button that was clicked
     */
    private void startRebinding(String actionName, TextButton button) {
        // Cancel any existing rebinding
        if (currentlyRebinding != null) {
            cancelRebinding();
        }
        
        currentlyRebinding = actionName;
        originalButtonText = button.getText().toString();
        button.setText("Press Key");
        
        // Set up a temporary input processor to capture the next key press
        setupRebindingInput();
    }
    
    /**
     * Cancel the current rebinding process
     */
    private void cancelRebinding() {
        if (currentlyRebinding != null && keyBindButtons.containsKey(currentlyRebinding)) {
            TextButton button = keyBindButtons.get(currentlyRebinding);
            button.setText(originalButtonText);
            
            currentlyRebinding = null;
            originalButtonText = null;
        }
        clearRebindingInput();
    }
    
    /**
     * Complete the rebinding process with a new key
     * @param newKeyCode The key code to bind to the current action
     */
    private void completeRebinding(int newKeyCode) {
        if (currentlyRebinding == null) return;
        
        TextButton button = keyBindButtons.get(currentlyRebinding);
        
        // Check if the key is already being used by another pending keybind
        for (Map.Entry<String, Integer> entry : pendingKeybinds.entrySet()) {
            if (!entry.getKey().equals(currentlyRebinding) && entry.getValue() == newKeyCode) {
                // Key already in use, cancel rebinding
                cancelRebinding();
                return;
            }
        }
        
        // Check if the key is already bound in the current keymap (excluding the current action)
        for (Map.Entry<String, Integer> entry : Keymap.getKeyMap().entrySet()) {
            if (!entry.getKey().equals(currentlyRebinding) && entry.getValue() == newKeyCode) {
                // Key already in use, cancel rebinding
                cancelRebinding();
                return;
            }
        }
        
        // Store the pending keybind
        pendingKeybinds.put(currentlyRebinding, newKeyCode);
        
        // Update button text
        String keyName = Input.Keys.toString(newKeyCode);
        button.setText(keyName + " *");  // Asterisk indicates pending change
        
        currentlyRebinding = null;
        originalButtonText = null;
        clearRebindingInput();
    }
    
    /**
     * Sets up temporary input handling for keybind rebinding
     */
    private void setupRebindingInput() {
        rebindingListener = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    // Cancel rebinding on escape
                    cancelRebinding();
                } else if (keycode != Input.Keys.UNKNOWN) {
                    // Complete rebinding with the pressed key
                    completeRebinding(keycode);
                }
                return true; // Consume the input
            }
        };
        
        // Add the listener to the stage to capture all key events
        ServiceLocator.getRenderService().getStage().addListener(rebindingListener);
    }
    
    /**
     * Clears temporary input handling for keybind rebinding
     */
    private void clearRebindingInput() {
        if (rebindingListener != null) {
            ServiceLocator.getRenderService().getStage().removeListener(rebindingListener);
            rebindingListener = null;
        }
    }
    
    /**
     * Reset all keybinds to their default values
     */
    private void resetKeybindsToDefaults() {
        // Clear pending changes
        pendingKeybinds.clear();
        
        // Use the reset method from UserSettings
        UserSettings.resetKeybindsToDefaults();
        
        // Update buttons to show default values
        updateAllKeybindButtons();
    }
    
    /**
     * Updates all keybind buttons to reflect current or pending values
     */
    private void updateAllKeybindButtons() {
        Map<String, Integer> currentKeyMap = Keymap.getKeyMap();
        
        for (Map.Entry<String, Integer> entry : currentKeyMap.entrySet()) {
            String actionName = entry.getKey();
            int keyCode = entry.getValue();
            
            // Check if there's a pending change for this action
            if (pendingKeybinds.containsKey(actionName)) {
                keyCode = pendingKeybinds.get(actionName);
            }
            
            TextButton button = keyBindButtons.get(actionName);
            if (button != null) {
                String keyText = Input.Keys.toString(keyCode);
                if (pendingKeybinds.containsKey(actionName)) {
                    keyText += " *";  // Indicate pending change
                }
                button.setText(keyText);
            }
        }
    }
    
    /**
     * Updates the display text of a key bind button after rebinding
     * @param actionName The action that was rebound
     * @param newKeyCode The new key code
     */
    public void updateKeyBindButton(String actionName, int newKeyCode) {
        TextButton button = keyBindButtons.get(actionName);
        if (button != null) {
            button.setText(Input.Keys.toString(newKeyCode));
        }
    }
    
    /**
     * Formats action names to be more user-friendly
     * Converts camelCase to space-separated words and removes prefixes
     * @param actionName the action name to format
     * @return the formatted display name
     */
    private String formatActionName(String actionName) {
        // Convert camelCase to formatted words
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < actionName.length(); i++) {
            char c = actionName.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                formatted.append(' ');
            }
            formatted.append(c);
        }
        
        // Remove prefixes and trim
        String result = formatted.toString()
            .replace("Player ", "")
            .replace("Terminal ", "Terminal ")
            .replace("Pause", "")
            .trim();
            
        return result;
    }

    private void applyChanges() {
        UserSettings.Settings settings = UserSettings.get();

        // Set Brightness
        updateBrightness(settings);

        // Apply volume settings
        settings.masterVolume = masterVolumeSlider.getValue();
        settings.musicVolume = musicVolumeSlider.getValue();

        // Apply pending keybind changes
        for (Map.Entry<String, Integer> entry : pendingKeybinds.entrySet()) {
            Keymap.setActionKeyCode(entry.getKey(), entry.getValue());
        }
        
        // Update the settings object with current keybinds BEFORE saving
        if (settings.keyBindSettings == null) {
            settings.keyBindSettings = new UserSettings.KeyBindSettings();
        }
        settings.keyBindSettings.customKeybinds = new HashMap<>(Keymap.getKeyMap());

        // Save and apply immediately with updated keybinds
        UserSettings.set(settings, true);

        // Update currently playing music volume immediately
        updateCurrentMusicVolume();
        
        // Clear pending changes and update display
        pendingKeybinds.clear();
        updateAllKeybindButtons();

        logger.info("[Apply] Saved settings: master={} music={}", settings.masterVolume, settings.musicVolume);
    }

    private void updateCurrentMusicVolume() {
        try {
            float musicVol = musicVolumeSlider.getValue();
            for (String i : backgroundSongs) {
                if (!ServiceLocator.getResourceService().containsAsset(i, Music.class)) {
                    continue;
                }
                Music music = ServiceLocator.getResourceService().getAsset(i, Music.class);

                if (music != null && music.isPlaying()) {
                    music.setVolume(musicVol);
                } else {
                    logger.warn("[Audio] Tried to update music volume, but no music is playing.");
                }
            }
        } catch (Exception e) {
            logger.error("[Audio] Failed to update music volume", e);
        }
    }

    /**
     * Updates Brightness instantly
     * @param settings
     */
    private void updateBrightness(UserSettings.Settings settings) {
        settings.setBrightnessValue(brightnessSlider.getValue());
        LightingEngine lightingEngine = ServiceLocator.getLightingService().getEngine();
        lightingEngine.setAmbientLight(settings.getBrightnessValue());
    }


    /**
     * Cleanup method to call when the settings tab is closed
     * This ensures any active rebinding is cancelled
     */
    public void dispose() {
        if (currentlyRebinding != null) {
            cancelRebinding();
        }
    }
}
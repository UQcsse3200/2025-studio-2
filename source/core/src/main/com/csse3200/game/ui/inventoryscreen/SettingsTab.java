package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.services.ServiceLocator;

import java.util.Map;

/**
 * Settings tab for the pause menu inventory screen.
 * Provides access to commonly adjusted game settings during gameplay.
 * 
 * Features included:
 * - Master Volume: Control overall game volume (0-100%) with real-time updates
 * - Music Volume: Control background music volume (0-100%) with immediate effect
 * - Key Bindings: Interactive display of all current keybinds with placeholder rebinding functionality
 * 
 * This is designed for quick in-game access to essential settings. For advanced display 
 * settings (FPS, fullscreen, VSync, resolution), use the main settings menu instead.
 * 
 * The keybind section dynamically loads all actions from the Keymap, ensuring 
 * comprehensive coverage including player controls, terminal keys, and pause menu shortcuts.
 */
public class SettingsTab implements InventoryTabInterface {
    
    // UI Components
    private Slider masterVolumeSlider;
    private Slider musicVolumeSlider;
    
    // Labels for real-time value updates
    private Label masterVolumeValue;
    private Label musicVolumeValue;

    @Override
    public Actor build(Skin skin) {
        // Get current settings
        UserSettings.Settings settings = UserSettings.get();
        
        // Create main container
        Table mainTable = new Table();
        mainTable.center();
        
        // Create title
        Label title = new Label("Game Settings", skin, "title");
        mainTable.add(title).colspan(2).padBottom(20f);
        mainTable.row();
        
        // Create settings table
        Table settingsTable = createSettingsTable(skin, settings);
        mainTable.add(settingsTable).expand().fill();
        
        return mainTable;
    }
    
    private Table createSettingsTable(Skin skin, UserSettings.Settings settings) {
        Table table = new Table();
        
        // Master Volume Setting
        Label masterVolumeLabel = new Label("Master Volume:", skin);
        masterVolumeSlider = new Slider(0f, 1f, 0.05f, false, skin);
        masterVolumeSlider.setValue(settings.masterVolume);
        masterVolumeValue = new Label(String.format("%.0f%%", settings.masterVolume * 100), skin);
        
        Table masterVolumeTable = new Table();
        masterVolumeTable.add(masterVolumeSlider).width(150).left();
        masterVolumeTable.add(masterVolumeValue).left().padLeft(10f);
        
        table.add(masterVolumeLabel).right().padRight(15f);
        table.add(masterVolumeTable).left();
        table.row().padTop(10f);
        
        // Music Volume Setting
        Label musicVolumeLabel = new Label("Music Volume:", skin);
        musicVolumeSlider = new Slider(0f, 1f, 0.05f, false, skin);
        musicVolumeSlider.setValue(settings.musicVolume);
        musicVolumeValue = new Label(String.format("%.0f%%", settings.musicVolume * 100), skin);
        
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
        TextButton applyBtn = new TextButton("Apply", skin);
        applyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                applyChanges();
            }
        });
        
        table.add(applyBtn).colspan(2).center().width(100).height(40).pad(10f);
        
        // Add listeners for real-time value updates
        setupListeners();
        
        return table;
    }
    
    private void setupListeners() {
        // Master volume slider listener
        masterVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = masterVolumeSlider.getValue();
                masterVolumeValue.setText(String.format("%.0f%%", value * 100));
            }
        });
        
        // Music volume slider listener
        musicVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = musicVolumeSlider.getValue();
                musicVolumeValue.setText(String.format("%.0f%%", value * 100));
            }
        });
    }
    
    private void addKeyBindingControls(Table table, Skin skin) {
        // Get all actions from the keymap and iterate through them
        Map<String, Integer> keyMap = Keymap.getKeyMap();
        
        // Define preferred order for display (player actions first, then terminal, then pause)
        String[] preferredOrder = {
            "PlayerUp", "PlayerLeft", "PlayerDown", "PlayerRight", 
            "PlayerAttack", "PlayerInteract",
            "TerminalModifier", "TerminalModifierAlt", "TerminalToggle",
            "PauseSettings", "PauseInventory", "PauseMap", "PauseUpgrades"
        };
        
        // First, add actions in preferred order if they exist
        for (String actionName : preferredOrder) {
            if (keyMap.containsKey(actionName)) {
                addKeyBindingRow(table, skin, actionName, keyMap.get(actionName));
            }
        }
        
        // Then add any remaining actions not in the preferred order
        for (Map.Entry<String, Integer> entry : keyMap.entrySet()) {
            String actionName = entry.getKey();
            // Skip if already added in preferred order
            boolean alreadyAdded = false;
            for (String preferredAction : preferredOrder) {
                if (preferredAction.equals(actionName)) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                addKeyBindingRow(table, skin, actionName, entry.getValue());
            }
        }
    }
    
    private void addKeyBindingRow(Table table, Skin skin, String actionName, int keyCode) {
        // Create a readable label for the action
        String displayName = formatActionName(actionName);
        Label actionLabel = new Label(displayName + ":", skin);
        table.add(actionLabel).right().padRight(15f);

        // Create interactive button showing current key
        String keyName = Input.Keys.toString(keyCode);
        TextButton keyButton = new TextButton(keyName, skin);
        
        // Add click listener for key rebinding (placeholder functionality)
        keyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Placeholder for key rebinding - show feedback that it's clickable
                String originalText = keyButton.getText().toString();
                keyButton.setText("Bind..");
                
                // Reset text after a short delay (simulated)
                // In a full implementation, this would wait for a new key press
                new Thread(() -> {
                    try {
                        Thread.sleep(1500); // 1.5 second delay
                        keyButton.setText(originalText);
                    } catch (InterruptedException e) {
                        keyButton.setText(originalText);
                    }
                }).start();
            }
        });
        
        table.add(keyButton).left().padLeft(10f).width(140).height(30);
        table.row().padTop(5f);
    }
    
    private String formatActionName(String actionName) {
        switch (actionName) {
            // Player movement and actions
            case "PlayerUp": return "Move Up";
            case "PlayerLeft": return "Move Left";
            case "PlayerDown": return "Move Down";
            case "PlayerRight": return "Move Right";
            case "PlayerAttack": return "Attack";
            case "PlayerInteract": return "Interact";
            
            // Terminal controls
            case "TerminalModifier": return "Terminal Ctrl (Left)";
            case "TerminalModifierAlt": return "Terminal Ctrl (Right)";
            case "TerminalToggle": return "Toggle Terminal";
            
            // Pause menu controls
            case "PauseSettings": return "Pause/Settings";
            case "PauseInventory": return "Open Inventory";
            case "PauseMap": return "Open Map";
            case "PauseUpgrades": return "Open Upgrades";
            
            default:
                // Fallback to camelCase conversion for any new actions
                return actionName.replaceAll("([A-Z])", " $1")
                    .replaceFirst("^\\s", "")
                    .replace("Player", "")
                    .replace("Terminal", "Term")
                    .replace("Pause", "")
                    .trim();
        }
    }
    
    private void applyChanges() {
        UserSettings.Settings settings = UserSettings.get();
        
        // Apply volume settings
        settings.masterVolume = masterVolumeSlider.getValue();
        settings.musicVolume = musicVolumeSlider.getValue();
        
        // Save and apply immediately
        UserSettings.set(settings, true);
        
        // Update currently playing music volume immediately
        updateCurrentMusicVolume();
    }
    
    private void updateCurrentMusicVolume() {
        try {
            // Try to update ForestGameArea background music
            String forestMusic = "sounds/BGM_03_mp3.mp3";
            Music music = ServiceLocator.getResourceService().getAsset(forestMusic, Music.class);
            if (music != null) {
                music.setVolume(UserSettings.getMusicVolumeNormalized());
            }
        } catch (Exception e) {
            // Music asset may not be loaded or playing, which is fine
            // This just means we're not in the forest area or music isn't playing
        }
        
        // Could add more music tracks here if there are other areas with different music
    }
}
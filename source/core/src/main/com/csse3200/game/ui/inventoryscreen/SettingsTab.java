package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.services.ServiceLocator;

/**
 * Settings tab for the pause menu inventory screen.
 * Provides access to commonly adjusted game settings during gameplay.
 * 
 * Features included:
 * - Master Volume: Control overall game volume (0-100%) with real-time updates
 * - Music Volume: Control background music volume (0-100%) with immediate effect
 * - Key Bindings: Interactive buttons showing current player controls (placeholder rebinding)
 * 
 * Note: Display settings (FPS, fullscreen, VSync, resolution) are commented out
 * but can be easily re-enabled by uncommenting the relevant code sections.
 * 
 * Settings are applied immediately when "Apply" is clicked.
 * Volume settings are persisted and update currently playing music in real-time.
 */
public class SettingsTab implements InventoryTabInterface {
    
    // UI Components
    // private TextField fpsText;  // COMMENTED OUT - Display settings
    // private CheckBox fullScreenCheck;  // COMMENTED OUT - Display settings
    // private CheckBox vsyncCheck;  // COMMENTED OUT - Display settings
    private Slider masterVolumeSlider;
    private Slider musicVolumeSlider;
    // private SelectBox<StringDecorator<DisplayMode>> displayModeSelect;  // COMMENTED OUT - Display settings
    
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
        
        // === COMMENTED OUT DISPLAY SETTINGS ===
        // FPS Setting
        // Label fpsLabel = new Label("FPS Cap:", skin);
        // fpsText = new TextField(Integer.toString(settings.fps), skin);
        // fpsText.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        // 
        // table.add(fpsLabel).right().padRight(15f);
        // table.add(fpsText).width(100).left();
        // table.row().padTop(10f);
        // 
        // // Fullscreen Setting
        // Label fullScreenLabel = new Label("Fullscreen:", skin);
        // fullScreenCheck = new CheckBox("", skin);
        // fullScreenCheck.setChecked(settings.fullscreen);
        // 
        // table.add(fullScreenLabel).right().padRight(15f);
        // table.add(fullScreenCheck).left();
        // table.row().padTop(10f);
        // 
        // // VSync Setting
        // Label vsyncLabel = new Label("VSync:", skin);
        // vsyncCheck = new CheckBox("", skin);
        // vsyncCheck.setChecked(settings.vsync);
        // 
        // table.add(vsyncLabel).right().padRight(15f);
        // table.add(vsyncCheck).left();
        // table.row().padTop(10f);
        // 
        // // Resolution Setting
        // Label displayModeLabel = new Label("Resolution:", skin);
        // displayModeSelect = new SelectBox<>(skin);
        // Monitor selectedMonitor = Gdx.graphics.getMonitor();
        // displayModeSelect.setItems(getDisplayModes(selectedMonitor));
        // displayModeSelect.setSelected(getActiveMode(displayModeSelect.getItems()));
        // 
        // table.add(displayModeLabel).right().padRight(15f);
        // table.add(displayModeSelect).left();
        // table.row().padTop(10f);
        // === END COMMENTED DISPLAY SETTINGS ===
        
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
        // Order for keys to appear
        String[] keyOrder = {
            "PlayerUp",
            "PlayerLeft", 
            "PlayerDown",
            "PlayerRight",
            "PlayerAttack",
            "PlayerInteract"
        };

        for (String actionName : keyOrder) {
            int keyCode = Keymap.getActionKeyCode(actionName);

            // Skip if the action doesn't exist in the keymap
            if (keyCode == -1) continue;

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
    }
    
    private String formatActionName(String actionName) {
        switch (actionName) {
            case "PlayerUp": return "Move Up";
            case "PlayerLeft": return "Move Left";
            case "PlayerDown": return "Move Down";
            case "PlayerRight": return "Move Right";
            case "PlayerAttack": return "Attack";
            case "PlayerInteract": return "Interact";
            default:
                // Fallback to camelCase conversion
                return actionName.replaceAll("([A-Z])", " $1")
                    .replaceFirst("^\\s", "")
                    .replace("Player", "")
                    .trim();
        }
    }
    
    private void applyChanges() {
        UserSettings.Settings settings = UserSettings.get();
        
        // === COMMENTED OUT DISPLAY SETTINGS ===
        // Apply FPS setting
        // try {
        //     int fpsVal = Integer.parseInt(fpsText.getText());
        //     if (fpsVal > 0 && fpsVal <= 300) { // Reasonable FPS range
        //         settings.fps = fpsVal;
        //     }
        // } catch (NumberFormatException e) {
        //     // Keep existing FPS if invalid input
        // }
        // 
        // // Apply display settings
        // settings.fullscreen = fullScreenCheck.isChecked();
        // settings.vsync = vsyncCheck.isChecked();
        // settings.displayMode = new DisplaySettings(displayModeSelect.getSelected().object);
        // === END COMMENTED DISPLAY SETTINGS ===
        
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
    
    // === COMMENTED OUT DISPLAY METHODS ===
    // private Array<StringDecorator<DisplayMode>> getDisplayModes(Monitor monitor) {
    //     DisplayMode[] displayModes = Gdx.graphics.getDisplayModes(monitor);
    //     Array<StringDecorator<DisplayMode>> arr = new Array<>();
    //     
    //     for (DisplayMode displayMode : displayModes) {
    //         arr.add(new StringDecorator<>(displayMode, this::prettyPrint));
    //     }
    //     
    //     return arr;
    // }
    // 
    // private String prettyPrint(DisplayMode displayMode) {
    //     return displayMode.width + "x" + displayMode.height + ", " + displayMode.refreshRate + "Hz";
    // }
    // 
    // private StringDecorator<DisplayMode> getActiveMode(Array<StringDecorator<DisplayMode>> modes) {
    //     DisplayMode activeMode = Gdx.graphics.getDisplayMode();
    //     
    //     for (StringDecorator<DisplayMode> mode : modes) {
    //         DisplayMode dm = mode.object;
    //         if (dm.width == activeMode.width && dm.height == activeMode.height 
    //             && dm.refreshRate == activeMode.refreshRate) {
    //             return mode;
    //         }
    //     }
    //     
    //     return modes.first(); // Fallback to first mode
    // }
    // === END COMMENTED DISPLAY METHODS ===
}

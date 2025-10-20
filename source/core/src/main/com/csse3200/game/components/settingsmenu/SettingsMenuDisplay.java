package com.csse3200.game.components.settingsmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.files.UserSettings.DisplaySettings;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.input.SettingsInputComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.utils.StringDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Settings menu display and logic. If you bork the settings, they can be changed manually in
 * CSSE3200Game/settings.json under your home directory (This is C:/users/[username] on Windows).
 */
public class SettingsMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(SettingsMenuDisplay.class);
  private final GdxGame game;

  private Table rootTable;
  private TextField fpsText;
  private CheckBox fullScreenCheck;
  private CheckBox vsyncCheck;
  //  private Slider uiScaleSlider;
  private Slider masterVolumeSlider;
  private Slider musicVolumeSlider;
  private SelectBox<StringDecorator<DisplayMode>> displayModeSelect;

  private Map<String, TextButton> keyBindButtons = new HashMap<>();

  private SettingsInputComponent settingsInputComponent;

  private Sound buttonClickSound;


  public SettingsMenuDisplay(GdxGame game) {
    super();
    this.game = game;
  }

  /**
   * Creates the settings menu UI components and initializes the input handling system.
   * Sets up the key binding buttons and passes them to the input component for rebinding functionality.
   */
  @Override
  public void create() {
    super.create();

    buttonClickSound = ServiceLocator.getResourceService()
            .getAsset("sounds/buttonsound.mp3", Sound.class);

    // Create and add the settings input component
    settingsInputComponent = entity.getComponent(SettingsInputComponent.class);
    addActors();

    // Pass the key bind buttons to the input component
    settingsInputComponent.setKeyBindButtons(keyBindButtons);

    stage.setKeyboardFocus(stage.getRoot());
  }

  /**
   * Creates and configures all UI actors for the settings menu, including background,
   * title, settings table, and menu buttons. Arranges them in the root table layout.
   */
  private void addActors() {
    Image background =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/superintelligence_menu_background.png", Texture.class));

    background.setFillParent(true);
    stage.addActor(background);

    Label title = new Label("Settings", skin);
    Table settingsTable = makeSettingsTable();
    Table menuButtons = makeMenuButtons();

    rootTable = new Table();
    rootTable.setFillParent(true);

    rootTable.add(title).expandX().top().padTop(20f);

    rootTable.row().padTop(30f);
    rootTable.add(settingsTable).expandX().expandY();

    rootTable.row();
    rootTable.add(menuButtons).fillX();

    stage.addActor(rootTable);
  }

  /**
   * Creates the main settings configuration table containing all user preference controls
   * including display settings, audio controls, and key binding options.
   *
   * @return Table containing all settings UI components
   */
  private Table makeSettingsTable() {
      // Get current values
      UserSettings.Settings settings = UserSettings.get();

      // Root table that stacks sections vertically
      Table root = new Table();
      root.top().pad(20f);

      // --- Display Section ---
      Table displaySection = new Table();
      displaySection.columnDefaults(0).right().padRight(15f);
      displaySection.columnDefaults(1).left();

      Label displayHeader = new Label("Display", skin, "title");
      displaySection.add(displayHeader).colspan(2).center().padBottom(10f);
      displaySection.row().padTop(10f);

      // Create components
      Label fpsLabel = new Label("FPS Cap:", skin);
      fpsText = new TextField(Integer.toString(settings.fps), skin);

      Label fullScreenLabel = new Label("Fullscreen:", skin);
      fullScreenCheck = new CheckBox("", skin);
      fullScreenCheck.setChecked(settings.fullscreen);

      Label vsyncLabel = new Label("VSync:", skin);
      vsyncCheck = new CheckBox("", skin);
      vsyncCheck.setChecked(settings.vsync);

//    Label uiScaleLabel = new Label("ui Scale (Unused):", skin);
//    uiScaleSlider = new Slider(0.2f, 2f, 0.1f, false, skin);
//    uiScaleSlider.setValue(settings.uiScale);
//    Label uiScaleValue = new Label(String.format("%.2fx", settings.uiScale), skin);

      Label displayModeLabel = new Label("Resolution:", skin);
      displayModeSelect = new SelectBox<>(skin);
      Monitor selectedMonitor = Gdx.graphics.getMonitor();
      displayModeSelect.setItems(getDisplayModes(selectedMonitor));

      // Set current display mode properly
      StringDecorator<DisplayMode> currentMode =
              getCurrentDisplayMode(displayModeSelect.getItems(), settings);
      if (currentMode != null) {
          displayModeSelect.setSelected(currentMode);
      }

      displaySection.add(fpsLabel).right().padRight(15f);
      displaySection.add(fpsText).width(100).left();

      displaySection.row().padTop(10f);
      displaySection.add(fullScreenLabel).right().padRight(15f);
      displaySection.add(fullScreenCheck).left();

      displaySection.row().padTop(10f);
      displaySection.add(vsyncLabel).right().padRight(15f);
      displaySection.add(vsyncCheck).left();

//    displaySection.row().padTop(10f);
//    Table uiScaleTable = new Table();
//    uiScaleTable.add(uiScaleSlider).width(100).left();
//    uiScaleTable.add(uiScaleValue).left().padLeft(5f).expandX();

//    displaySection.add(uiScaleLabel).right().padRight(15f);
//    displaySection.add(uiScaleTable).left();

      displaySection.row().padTop(10f);
      displaySection.add(displayModeLabel).right().padRight(15f);
      displaySection.add(displayModeSelect).left();

      root.add(displaySection).padBottom(20f);
      root.row();

      // --- Audio Section ---
      Table audioSection = new Table();
      audioSection.columnDefaults(0).right().padRight(15f);
      audioSection.columnDefaults(1).left();

      Label audioHeader = new Label("Audio", skin, "title");
      audioSection.add(audioHeader).colspan(2).center().padBottom(10f);
      audioSection.row().padTop(10f);

      // Master volume + music volume rows follow here
      Label masterVolumeLabel = new Label("Master Volume:", skin);
      masterVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
      masterVolumeSlider.setValue(settings.masterVolume);
      Label masterVolumeValue = new Label((int)(settings.masterVolume * 100) + "%", skin);

      Label musicVolumeLabel = new Label("Music Volume:", skin);
      musicVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
      musicVolumeSlider.setValue(settings.musicVolume);
      Label musicVolumeValue = new Label((int)(settings.musicVolume * 100) + "%", skin);

      // Create master volume slider
      Table masterVolumeTable = new Table();
      masterVolumeTable.add(masterVolumeSlider).width(100).left();
      masterVolumeTable.add(masterVolumeValue).left().padLeft(5f).expandX();
      audioSection.add(masterVolumeLabel).right().padRight(15f);
      audioSection.add(masterVolumeTable).left();

      // Create music volume slider
      audioSection.row().padTop(10f);
      Table musicVolumeTable = new Table();
      musicVolumeTable.add(musicVolumeSlider).width(100).left();
      musicVolumeTable.add(musicVolumeValue).left().padLeft(5f).expandX();
      audioSection.add(musicVolumeLabel).right().padRight(15f);
      audioSection.add(musicVolumeTable).left();

      // Events on inputs
//    uiScaleSlider.addListener(
//        (Event event) -> {
//          float value = uiScaleSlider.getValue();
//          uiScaleValue.setText(String.format("%.2fx", value));
//          return true;
//        });

      // Handle slider events
      masterVolumeSlider.addListener((Event event) -> {
          float value = masterVolumeSlider.getValue();
          int percent = (int)(masterVolumeSlider.getValue() * 100);
          masterVolumeValue.setText(percent + "%");
          return true;
      });

      musicVolumeSlider.addListener((Event event) -> {
          int percent = (int)(musicVolumeSlider.getValue() * 100);
          musicVolumeValue.setText(percent + "%");
          return true;
      });

      root.add(audioSection).padBottom(20f);
      root.row();

      // --- Controls Section ---
      Table controlsSection = new Table();
      controlsSection.columnDefaults(0).right().padRight(15f);
      controlsSection.columnDefaults(1).left();

      Label controlsHeader = new Label("Controls", skin, "title");
      controlsSection.add(controlsHeader).colspan(2).center().padBottom(10f);
      controlsSection.row().padTop(10f);

      // Key bindings + restore defaults follow here
      Label keyBindLabel = new Label("Key Bindings:", skin);
      controlsSection.add(keyBindLabel).colspan(2).center();

      addKeyBindingControls(controlsSection);

      root.add(controlsSection).row();

      return root;
  }


    /**
   * Creates UI controls for key binding configuration, allowing users to rebind game actions
   * to different keys. Generates buttons for each action in the keymap and includes a reset option.
   *
   * @param table The table to add key binding controls to
   */
  private void addKeyBindingControls(Table table) {
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
          settingsInputComponent.startRebinding(actionName, keyButton);
          keyButton.setText("Press Key");
        }
      });

      table.add(keyButton).width(170).height(25).left();

      // Store the button with the action name as key
      keyBindButtons.put(actionName, keyButton);
    }

    // Create reset to default button
    table.row().padTop(15f);
    TextButton defaultButton = new TextButton("Restore Defaults", skin, "settingsMenu");
    defaultButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        buttonClickSound.play(UserSettings.get().masterVolume);
        UserSettings.resetKeybindsToDefaults();
        updateAllKeybindButtons();
      }
    });

    table.add(defaultButton).colspan(2).height(25).center();
  }

  /**
   * Updates all key binding button displays to reflect the current keymap state.
   * Used after resetting to defaults or loading saved settings.
   */
  private void updateAllKeybindButtons() {
    Map<String, Integer> currentKeyMap = Keymap.getKeyMap();

    for (Map.Entry<String, Integer> entry : currentKeyMap.entrySet()) {
      String actionName = entry.getKey();
      int keyCode = entry.getValue();

      TextButton button = keyBindButtons.get(actionName);
      if (button != null) {
        button.setText(Input.Keys.toString(keyCode));
      }
    }
  }

  /**
   * Gets the current display mode from the available options, checking both
   * the actual current display mode and the saved settings.
   *
   * @param modes Available display modes to choose from
   * @param settings Current user settings containing saved display preferences
   * @return The matching display mode decorator, or first available if no match found
   */
  private StringDecorator<DisplayMode> getCurrentDisplayMode(
      Array<StringDecorator<DisplayMode>> modes,
      UserSettings.Settings settings) {

    DisplayMode targetMode;

    // Try to use the saved display mode from settings
    if (settings.displayMode != null) {
      // Create a DisplayMode from saved settings
      DisplayMode savedMode = findMatchingDisplayMode(settings.displayMode);
      if (savedMode != null) {
        targetMode = savedMode;
      } else {
        // Fall back to current display mode if saved mode doesn't exist
        targetMode = Gdx.graphics.getDisplayMode();
      }
    } else {
      // No saved settings, use current display mode
      targetMode = Gdx.graphics.getDisplayMode();
    }

    // Find the matching mode in dropdown
    for (StringDecorator<DisplayMode> stringMode : modes) {
      DisplayMode mode = stringMode.object;
      if (targetMode.width == mode.width
          && targetMode.height == mode.height
          && targetMode.refreshRate == mode.refreshRate) {
        return stringMode;
      }
    }

    // If no exact match found, return the first item
    return modes.size > 0 ? modes.first() : null;
  }

  /**
   * Finds a DisplayMode that matches the saved DisplaySettings.
   *
   * @param displaySettings The saved display settings to match against
   * @return Matching DisplayMode or null if no match found
   */
  private DisplayMode findMatchingDisplayMode(UserSettings.DisplaySettings displaySettings) {
    for (DisplayMode mode : Gdx.graphics.getDisplayModes()) {
      if (mode.width == displaySettings.width
          && mode.height == displaySettings.height
          && mode.refreshRate == displaySettings.refreshRate) {
        return mode;
      }
    }
    return null;
  }

  /**
   * Formats action names to be more user-friendly
   * Converts camelCase to space-separated words and removes 'Player' prefix
   * @param actionName the action name to format
   * @return the formatted display name
   */
  private String formatActionName(String actionName) {
    // convert camelCase to formatted words
    StringBuilder formatted = new StringBuilder();
      for (int i = 0; i < actionName.length(); i++) {
        char c = actionName.charAt(i);
        if (i > 0 && Character.isUpperCase(c)) {
          formatted.append(' ');
        }
        formatted.append(c);
      }

      // Remove "Player" prefix and trim
      String result = formatted.toString()
          .replace("Player ", "")
          .replace("Terminal ", "Terminal ")
          .replace("Pause", "")
          .trim();

      return result;
  }

  /**
   * Retrieves and formats all available display modes for the specified monitor.
   *
   * @param monitor The monitor to get display modes for
   * @return Array of decorated display modes for UI selection
   */
  private Array<StringDecorator<DisplayMode>> getDisplayModes(Monitor monitor) {
    DisplayMode[] displayModes = Gdx.graphics.getDisplayModes(monitor);
    Array<StringDecorator<DisplayMode>> arr = new Array<>();

    for (DisplayMode displayMode : displayModes) {
      arr.add(new StringDecorator<>(displayMode, this::prettyPrint));
    }

    return arr;
  }

  /**
   * Formats a DisplayMode into a user-friendly string representation.
   *
   * @param displayMode The display mode to format
   * @return Formatted string showing resolution and refresh rate
   */
  private String prettyPrint(DisplayMode displayMode) {
    return displayMode.width + "x" + displayMode.height + ", " + displayMode.refreshRate + "hz";
  }

  /**
   * Creates the bottom menu buttons (Exit and Apply) with their respective click handlers.
   *
   * @return Table containing the menu navigation buttons
   */
  private Table makeMenuButtons() {
    TextButton exitBtn = new TextButton("Exit", skin,"settingsMenu");
    TextButton applyBtn = new TextButton("Apply", skin, "settingsMenu");

    exitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Exit button clicked");
            buttonClickSound.play(UserSettings.get().masterVolume);
            exitMenu();
          }
        });

    applyBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Apply button clicked");
            buttonClickSound.play(UserSettings.get().masterVolume);
            applyChanges();
          }
        });

    Table table = new Table();
    table.add(exitBtn).expandX().left().pad(0f, 15f, 15f, 0f);
    table.add(applyBtn).expandX().right().pad(0f, 0f, 15f, 15f);
    return table;
  }

  /**
   * Applies all current UI settings to the user preferences and saves them.
   * Validates input values and updates display, audio, and control settings.
   */
  private void applyChanges() {
    UserSettings.Settings settings = UserSettings.get();

    Integer fpsVal = parseOrNull(fpsText.getText());
    if (fpsVal != null) {
      settings.fps = fpsVal;
    }
    settings.fullscreen = fullScreenCheck.isChecked();
//    settings.uiScale = uiScaleSlider.getValue();
    settings.displayMode = new DisplaySettings(displayModeSelect.getSelected().object);
    settings.vsync = vsyncCheck.isChecked();

    // Set volume
    settings.masterVolume = masterVolumeSlider.getValue();
    settings.musicVolume = musicVolumeSlider.getValue();

    // Save the settings without applying them immediately
    UserSettings.set(settings, false);

    // Save current keybinds
    UserSettings.saveCurrentKeybinds();

    UserSettings.applySettings(UserSettings.get());

    updateAllKeybindButtons();
  }

  /**
   * Exits the settings menu and returns to the main menu screen.
   */
  private void exitMenu() {
    game.setScreen(ScreenType.MAIN_MENU);
  }

  /**
   * Safely parses a string to an integer, returning null if parsing fails.
   *
   * @param num String to parse
   * @return Parsed integer or null if invalid
   */
  private Integer parseOrNull(String num) {
    try {
      return Integer.parseInt(num, 10);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  protected void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  @Override
  public void update() {
    stage.act(ServiceLocator.getTimeSource().getDeltaTime());
  }

  @Override
  public void dispose() {
    if (settingsInputComponent != null && settingsInputComponent.isRebinding()) {
      settingsInputComponent.cancelRebinding();
    }
    rootTable.clear();
    super.dispose();
  }
}
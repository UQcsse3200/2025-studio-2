package com.csse3200.game.components.settingsmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
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
import com.csse3200.game.input.SettingsInputComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.utils.StringDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.Input;
import com.csse3200.game.input.Keymap;

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

  public SettingsMenuDisplay(GdxGame game) {
    super();
    this.game = game;
  }

  @Override
  public void create() {
    super.create();

    // Create and add the settings input component
    settingsInputComponent = entity.getComponent(SettingsInputComponent.class);
    addActors();

    // Pass the key bind buttons to the input component
    settingsInputComponent.setKeyBindButtons(keyBindButtons);

    stage.setKeyboardFocus(stage.getRoot());
  }

  private void addActors() {
    Image background =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/superintelligence_menu_background.png", Texture.class));

    background.setFillParent(true);
    stage.addActor(background);

    Label title = new Label("Settings", skin, "title");
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

  private Table makeSettingsTable() {
    // Get current values
    UserSettings.Settings settings = UserSettings.get();

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

    Label masterVolumeLabel = new Label("Master Volume:", skin);
    masterVolumeSlider = new Slider(0f, 1f, 0.1f, false, skin);
    masterVolumeSlider.setValue(settings.masterVolume);
    Label masterVolumeValue = new Label(String.format("%.2fx", settings.masterVolume), skin);

    Label musicVolumeLabel = new Label("Music Volume:", skin);
    musicVolumeSlider = new Slider(0f, 1f, 0.1f, false, skin);
    musicVolumeSlider.setValue(settings.musicVolume);
    Label musicVolumeValue = new Label(String.format("%.2fx", settings.musicVolume), skin);

    Label displayModeLabel = new Label("Resolution:", skin);
    displayModeSelect = new SelectBox<>(skin);
    Monitor selectedMonitor = Gdx.graphics.getMonitor();
    displayModeSelect.setItems(getDisplayModes(selectedMonitor));
    displayModeSelect.setSelected(getActiveMode(displayModeSelect.getItems()));

    // Position Components on table
    Table table = new Table();

    table.add(fpsLabel).right().padRight(15f);
    table.add(fpsText).width(100).left();

    table.row().padTop(10f);
    table.add(fullScreenLabel).right().padRight(15f);
    table.add(fullScreenCheck).left();

    table.row().padTop(10f);
    table.add(vsyncLabel).right().padRight(15f);
    table.add(vsyncCheck).left();

//    table.row().padTop(10f);
//    Table uiScaleTable = new Table();
//    uiScaleTable.add(uiScaleSlider).width(100).left();
//    uiScaleTable.add(uiScaleValue).left().padLeft(5f).expandX();

//    table.add(uiScaleLabel).right().padRight(15f);
//    table.add(uiScaleTable).left();

    table.row().padTop(10f);
    table.add(displayModeLabel).right().padRight(15f);
    table.add(displayModeSelect).left();

    // Create master volume slider
    table.row().padTop(10f);
    Table masterVolumeTable = new Table();
    masterVolumeTable.add(masterVolumeSlider).width(100).left();
    masterVolumeTable.add(masterVolumeValue).left().padLeft(5f).expandX();
    table.add(masterVolumeLabel).right().padRight(15f);
    table.add(masterVolumeTable).left();

    // Create music volume slider
    table.row().padTop(10f);
    Table musicVolumeTable = new Table();
    musicVolumeTable.add(musicVolumeSlider).width(100).left();
    musicVolumeTable.add(musicVolumeValue).left().padLeft(5f).expandX();
    table.add(musicVolumeLabel).right().padRight(15f);
    table.add(musicVolumeTable).left();

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
      masterVolumeValue.setText(String.format("%.2fx", value));
      return true;
    });

    musicVolumeSlider.addListener((Event event) -> {
      float value = musicVolumeSlider.getValue();
      musicVolumeValue.setText(String.format("%.2fx", value));
      return true;
    });

    table.row().padTop(20f);
    Label keyBindLabel = new Label("Key Bindings:", skin, "title");
    table.add(keyBindLabel).colspan(2).center();

    addKeyBindingControls(table);

    return table;
  }

  /**
   * TODO
   * @param table
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
      TextButton keyButton = new TextButton(Input.Keys.toString(currentKeyCode), skin);
      keyButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          settingsInputComponent.startRebinding(actionName, keyButton);
          keyButton.setText("Press Key");
        }
      });

      table.add(keyButton).width(170).height(25).left();

      // Store the button with the action name as key
      keyBindButtons.put(actionName, keyButton);
    }


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
   * Updates the display text of a key bind button after rebinding
   * @param actionName The action that was rebound
   * @param newKeyCode The new key code
   */
  public void updateKeyBindButton(String actionName, int newKeyCode) {
    TextButton button = keyBindButtons.get(actionName);
    button.setText(Input.Keys.toString(newKeyCode));
  }

  private StringDecorator<DisplayMode> getActiveMode(Array<StringDecorator<DisplayMode>> modes) {
    DisplayMode active = Gdx.graphics.getDisplayMode();

    for (StringDecorator<DisplayMode> stringMode : modes) {
      DisplayMode mode = stringMode.object;
      if (active.width == mode.width
          && active.height == mode.height
          && active.refreshRate == mode.refreshRate) {
        return stringMode;
      }
    }
    return null;
  }

  private Array<StringDecorator<DisplayMode>> getDisplayModes(Monitor monitor) {
    DisplayMode[] displayModes = Gdx.graphics.getDisplayModes(monitor);
    Array<StringDecorator<DisplayMode>> arr = new Array<>();

    for (DisplayMode displayMode : displayModes) {
      arr.add(new StringDecorator<>(displayMode, this::prettyPrint));
    }

    return arr;
  }

  private String prettyPrint(DisplayMode displayMode) {
    return displayMode.width + "x" + displayMode.height + ", " + displayMode.refreshRate + "hz";
  }

  private Table makeMenuButtons() {
    TextButton exitBtn = new TextButton("Exit", skin);
    TextButton applyBtn = new TextButton("Apply", skin);

    exitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Exit button clicked");
            exitMenu();
          }
        });

    applyBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Apply button clicked");
            applyChanges();
          }
        });

    Table table = new Table();
    table.add(exitBtn).expandX().left().pad(0f, 15f, 15f, 0f);
    table.add(applyBtn).expandX().right().pad(0f, 0f, 15f, 15f);
    return table;
  }

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

    UserSettings.set(settings, true);
  }

  private void exitMenu() {
    game.setScreen(ScreenType.MAIN_MENU);
  }

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
    // The input handler will be disposed automatically when the entity is disposed
    rootTable.clear();
    super.dispose();
  }
}
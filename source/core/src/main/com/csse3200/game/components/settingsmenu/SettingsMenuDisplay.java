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
import com.csse3200.game.input.InputComponent;

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
//    Gdx.input.setInputProcessor(stage);
  }

  private void addActors() {
    Image background =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/superintelligence_menu_background.png", Texture.class));

    background.setFillParent(true);
    stage.addActor(background);

    background.setFillParent(true);
    stage.addActor(background);

    Label title = new Label("Settings", skin, "title");
    Table settingsTable = makeSettingsTable();
    Table menuBtns = makeMenuBtns();

    rootTable = new Table();
    rootTable.setFillParent(true);

    rootTable.add(title).expandX().top().padTop(20f);

    rootTable.row().padTop(30f);
    rootTable.add(settingsTable).expandX().expandY();

    rootTable.row();
    rootTable.add(menuBtns).fillX();

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
    // Order for keys to appear
    String[] keyOrder = {
        "PlayerUp",
        "PlayerLeft",
        "PlayerDown",
        "PlayerRight",
        "PlayerAttack",
        "PlayerInteract",
        "TerminalModifier",
        "TerminalModifierAlt",
        "TerminalToggle"
    };

    for (String actionName : keyOrder) {
      int keyCode = Keymap.getActionKeyCode(actionName);

      // Skip if the action doesn't exist in the keymap
      if (keyCode == -1) continue;

      table.row().padTop(5f);

      // Create a readable label for the action
      String displayName = formatActionName(actionName);
      Label actionLabel = new Label(displayName + ":", skin);
      table.add(actionLabel).right().padRight(15f);

      // Create button showing current key
      String keyName = Input.Keys.toString(keyCode);
      TextButton keyButton = new TextButton(keyName, skin);
      keyButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          if (settingsInputComponent != null) {
            settingsInputComponent.startRebinding(actionName, keyButton);
          }
        }
      });

      keyBindButtons.put(actionName, keyButton);
      table.add(keyButton).width(170).height(25).left();
    }


  }

  /**
   *
   * @param actionName
   * @return
   */
  private String formatActionName(String actionName) {
    switch (actionName) {
      case "PlayerUp": return "Up";
      case "PlayerLeft": return "Left";
      case "PlayerDown": return "Down";
      case "PlayerRight": return "Right";
      case "PlayerAttack": return "Attack";
      case "PlayerInteract": return "Interact";
      case "TerminalModifier": return "Terminal Modifier";
      case "TerminalModifierAlt": return "Terminal Modifier Alt";
      case "TerminalToggle": return "Terminal Toggle";
      default:
        // Fallback to camelCase conversion
        return actionName.replaceAll("([A-Z])", " $1")
            .replaceFirst("^\\s", "")
            .replace("Player", "")
            .trim();
    }
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

  private Table makeMenuBtns() {
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
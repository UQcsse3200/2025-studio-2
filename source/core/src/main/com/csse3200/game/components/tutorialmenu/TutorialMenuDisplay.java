package com.csse3200.game.components.tutorialmenu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.GdxGame;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UI component for displaying the tutorial menu with game controls and information.
 */
public class TutorialMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(TutorialMenuDisplay.class);
  private final GdxGame game;
  private Table contentTable;
  private String currentSection = "basics";
  
  // Track buttons to highlight the active one
  private TextButton basicsBtn;
  private TextButton itemsBtn;
  private TextButton upgradesBtn;
  private TextButton levelMechanicsBtn;
  private TextButton loreBtn;

  public TutorialMenuDisplay(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    // Background image
    Image background = new Image(
        ServiceLocator.getResourceService()
            .getAsset("images/superintelligence_menu_background.png", Texture.class));
    background.setFillParent(true);
    stage.addActor(background);

    // Main layout container
    Table rootTable = new Table();
    rootTable.setFillParent(true);
    rootTable.pad(50);

    // left sidebar with category buttons
    Table sidebar = createSidebar();
    
    // content area with dark background
    Table contentContainer = new Table();
    contentContainer.top().left();
    
    // Create border background
    Pixmap borderPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    borderPixmap.setColor(new Color(0.3f, 0.3f, 0.3f, 0.8f));
    borderPixmap.fill();
    Texture borderTexture = new Texture(borderPixmap);
    borderPixmap.dispose();
    
    // Create semi-transparent dark background for content area
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(new Color(0, 0, 0, 0.7f));
    pixmap.fill();
    Texture backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    
    // Set border as background with padding to create border effect
    contentContainer.setBackground(new TextureRegionDrawable(borderTexture));
    contentContainer.pad(3); // Border width
    
    // Inner table with dark background
    Table innerContent = new Table();
    innerContent.setBackground(new TextureRegionDrawable(backgroundTexture));
    innerContent.top().left();
    
    // actual content table
    contentTable = new Table();
    contentTable.top().left();
    contentTable.pad(20); // Add padding inside the dark background
    updateContent(currentSection);
    
    innerContent.add(contentTable).expand().fill();
    contentContainer.add(innerContent).expand().fill();

    // Add sidebar and content to root table
    rootTable.add(sidebar).width(280).expandY().fillY().padRight(40);
    rootTable.add(contentContainer).expand().fill();
    
    stage.addActor(rootTable);
  }

  /**
   * Creates the sidebar with category buttons and back button
   */
  private Table createSidebar() {
      Table sidebar = new Table();
      sidebar.top();

      // Title
      Label titleLabel = new Label("Tutorial", skin);
      titleLabel.setFontScale(2f);
      titleLabel.setColor(Color.BLACK);
      sidebar.add(titleLabel).padBottom(60).row();

      // Category buttons
      basicsBtn = createSidebarButton("The Basics", () -> {
          currentSection = "basics";
          updateContent(currentSection);
          updateButtonHighlight();
      });

      itemsBtn = createSidebarButton("Items", () -> {
          currentSection = "items";
          updateContent(currentSection);
          updateButtonHighlight();
      });

      upgradesBtn = createSidebarButton("Upgrades", () -> {
          currentSection = "upgrades";
          updateContent(currentSection);
          updateButtonHighlight();
      });

      levelMechanicsBtn = createSidebarButton("Level Mechanics", () -> {
          currentSection = "levelmechanics";
          updateContent(currentSection);
          updateButtonHighlight();
      });
      levelMechanicsBtn.getLabel().setFontScale(0.85f);

      loreBtn = createSidebarButton("Lore", () -> {
          currentSection = "lore";
          updateContent(currentSection);
          updateButtonHighlight();
      });

      sidebar.add(basicsBtn).width(240).height(70).padBottom(30).row();
      sidebar.add(itemsBtn).width(240).height(70).padBottom(30).row();
      sidebar.add(upgradesBtn).width(240).height(70).padBottom(30).row();
      sidebar.add(levelMechanicsBtn).width(240).height(70).padBottom(30).row();
      sidebar.add(loreBtn).width(240).height(70).padBottom(30).row();

      // Set initial highlight
      updateButtonHighlight();

      // Add spacer to push practice and back buttons to bottom
      sidebar.row().expandY();

      // Practice button
      TextButton practiceBtn = new TextButton("Practice!", skin);
      practiceBtn.setTransform(true);
      practiceBtn.setOrigin(Align.center);

      // Click action
      practiceBtn.addListener(new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
              logger.debug("Practice Level button clicked");
              launchPracticeLevel(MainGameScreen.Areas.TUTORIAL);
          }
      });

      // Hover effects
      practiceBtn.addListener(new ClickListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
              // Pulse animation + green tint
              practiceBtn.addAction(Actions.forever(
                      Actions.sequence(
                              Actions.scaleTo(1.1f, 1.1f, 0.3f),
                              Actions.scaleTo(1f, 1f, 0.3f)
                      )
              ));
              practiceBtn.addAction(Actions.color(Color.GREEN, 0.2f));
          }

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
              // Stop pulsing, reset to normal
              practiceBtn.clearActions();
              practiceBtn.addAction(Actions.scaleTo(1f, 1f, 0.1f));
              practiceBtn.addAction(Actions.color(Color.WHITE, 0.2f));
          }
      });

      sidebar.add(practiceBtn).width(240).height(70).padBottom(20).bottom().row();

      // Back button (separate style, not using createSidebarButton)
      TextButton backBtn = new TextButton("Back", skin, "redButton");
      backBtn.setTransform(true);
      backBtn.setOrigin(Align.center);

      // Hover effect: subtle scale + darker red
      backBtn.addListener(new ClickListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
              backBtn.addAction(Actions.parallel(
                      Actions.scaleTo(1.05f, 1.05f, 0.1f),
                      Actions.color(new Color(0.7f, 0f, 0f, 1f), 0.1f) // dark red
              ));
          }

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
              backBtn.addAction(Actions.parallel(
                      Actions.scaleTo(1f, 1f, 0.1f),
                      Actions.color(Color.RED, 0.1f)
              ));
          }

          @Override
          public void clicked(InputEvent event, float x, float y) {
              logger.debug("Back button clicked");
              game.setScreen(GdxGame.ScreenType.MAIN_MENU);
          }
      });

      backBtn.setColor(Color.RED);
      sidebar.add(backBtn).width(240).height(70).bottom();

      return sidebar;
  }


    /**
   * Updates button highlighting based on current section
   */
    private void updateButtonHighlight() {
        basicsBtn.setColor(Color.WHITE);
        itemsBtn.setColor(Color.WHITE);
        upgradesBtn.setColor(Color.WHITE);
        levelMechanicsBtn.setColor(Color.WHITE);
        loreBtn.setColor(Color.WHITE);

        switch (currentSection) {
            case "basics" -> basicsBtn.setColor(new Color(0f, 1f, 0f, 1f));
            case "items" -> itemsBtn.setColor(new Color(0f, 1f, 0f, 1f));
            case "upgrades" -> upgradesBtn.setColor(new Color(0f, 1f, 0f, 1f));
            case "levelmechanics" -> levelMechanicsBtn.setColor(new Color(0f, 1f, 0f, 1f));
            case "lore" -> loreBtn.setColor(new Color(0f, 1f, 0f, 1f));
        }
    }

    private boolean isActiveButton(TextButton button) {
        return (currentSection.equals("basics") && button == basicsBtn)
                || (currentSection.equals("items") && button == itemsBtn)
                || (currentSection.equals("upgrades") && button == upgradesBtn)
                || (currentSection.equals("levelmechanics") && button == levelMechanicsBtn)
                || (currentSection.equals("lore") && button == loreBtn);
    }




    /**
   * Updates the content area based on the selected section
   */
  private void updateContent(String section) {
    contentTable.clear();
    
    switch (section) {
      case "basics":
        showBasicsContent();
        break;
      case "items":
        showItemsContent();
        break;
      case "upgrades":
        showUpgradesContent();
        break;
      case "levelmechanics":
        showLevelMechanicsContent();
        break;
      case "lore":
        showLoreContent();
        break;
      default:
        showBasicsContent();
    }
  }

    /**
     * Displays content for "The Basics" section
     */
    private void showBasicsContent() {
        Label sectionTitle = new Label("The Basics", skin);
        sectionTitle.setFontScale(1.5f);
        sectionTitle.setColor(Color.GREEN);
        contentTable.add(sectionTitle).padBottom(20).left().colspan(2).row();

        Table spriteTable = new Table();

        // Add each movement control with animated sprites and keybinds
        addDisplayColumn(spriteTable, "images/PLAYER.atlas", true, "LEFT",
                "Move Left", getKeybindText("PlayerLeft") + "\nMove your character to the left.",
                288, 216, false, 35, 35);
        addDisplayColumn(spriteTable, "images/PLAYER.atlas", true, "RIGHT",
                "Move Right", getKeybindText("PlayerRight") + "\nMove your character to the right.",
                288, 216, false, 35, 35);
        addDisplayColumn(spriteTable, "images/PLAYER.atlas", true, "CROUCH",
                "Crouch", getKeybindText("PlayerCrouch") + "\nFit through tight spaces.",
                288, 216, false, 35, 35);
        addDisplayColumn(spriteTable, "images/PLAYER.atlas", true, "JUMP",
                "Jump", getKeybindText("PlayerJump") + "\nReach higher platforms. Double-tap for double-jump!",
                288, 216, false, 35, 35);

        contentTable.add(spriteTable).left().colspan(2).row();

        // Informational text with markup
        String markedUpText =
                """
                These are the basic movement controls. Practice combining them to navigate the world effectively!
                """;

        Label infoText = new Label(markedUpText, skin);
        infoText.setFontScale(1.2f);
        infoText.setWrap(true);
        Label.LabelStyle markupStyle = new Label.LabelStyle(infoText.getStyle());
        markupStyle.fontColor = Color.WHITE;
        infoText.setStyle(markupStyle);

        contentTable.add(infoText).fillX().padTop(30).left().colspan(2).row();
    }

    /**
     * Helper method to get a formatted keybind string with red color markup.
     * 
     * @param actionKey The action key name (e.g., "PlayerJump", "PlayerDash")
     * @return Formatted string with red color markup (e.g., "[RED]SPACE[]")
     */
    private String getKeybindText(String actionKey) {
        int keyCode = Keymap.getActionKeyCode(actionKey);
        String keyName = Input.Keys.toString(keyCode);
        return "[RED]" + keyName + "[]";
    }

    /**
     * Unified helper to add a display column (sprite + name + description).
     * Handles both animated atlases and static textures.
     * Used for all tutorial panes (basics, items, upgrades, and level mechanics).
     * 
     * @param table The table to add the column to
     * @param assetPath Path to the asset (atlas or texture)
     * @param isAnimated Whether the asset is an animated atlas
     * @param animationRegion Animation region name (only used if isAnimated is true)
     * @param title The title text (displayed in yellow)
     * @param description The description text
     * @param spriteWidth Sprite width
     * @param spriteHeight Sprite height
     * @param preserveAspectRatio Whether to preserve aspect ratio (for different shaped sprites)
     * @param padLeft Left padding
     * @param padRight Right padding
     */
    private void addDisplayColumn(Table table, String assetPath, boolean isAnimated, String animationRegion,
                                   String title, String description,
                                   float spriteWidth, float spriteHeight, boolean preserveAspectRatio,
                                   float padLeft, float padRight) {
        Actor sprite;
        
        if (isAnimated) {
            TextureAtlas atlas = ServiceLocator.getResourceService()
                    .getAsset(assetPath, TextureAtlas.class);
            Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions(animationRegion);
            if (frames.size == 0) return;
            
            Animation<TextureRegion> animation = new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);
            sprite = new AnimatedImage(animation);
        } else {
            Texture texture = ServiceLocator.getResourceService()
                    .getAsset(assetPath, Texture.class);
            Image image = new Image(texture);
            if (preserveAspectRatio) {
                image.setScaling(Scaling.fit);
            }
            sprite = image;
        }

        Table column = new Table();
        if (preserveAspectRatio) {
            column.add(sprite).prefSize(spriteWidth, spriteHeight).padBottom(15).center().row();
        } else {
            column.add(sprite).size(spriteWidth, spriteHeight).padBottom(15).center().row();
        }

        Label nameLabel = new Label(title, skin);
        nameLabel.setFontScale(1.3f);
        nameLabel.setColor(Color.YELLOW);
        column.add(nameLabel).center().padTop(5).row();

        Label descLabel = new Label(description, skin);
        descLabel.setFontScale(1.0f);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        column.add(descLabel).width(250).center().padTop(5).row();

        table.add(column).padLeft(padLeft).padRight(padRight).padBottom(10).expandX().fillX();
    }

    /**
     * Creates a styled sidebar button with hover scaling and click handling.
     */
    private TextButton createSidebarButton(String text, Runnable onClick) {
        TextButton button = new TextButton(text, skin, "tutorialButton");
        button.setTransform(true);
        button.setOrigin(Align.center);

        // Default color
        button.setColor(Color.WHITE);

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // Hover: scale + green tint
                button.addAction(Actions.parallel(
                        Actions.scaleTo(1.05f, 1.05f, 0.1f),
                        Actions.color(Color.GREEN, 0.1f)
                ));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // Reset if not the active section
                if (!isActiveButton(button)) {
                    button.addAction(Actions.parallel(
                            Actions.scaleTo(1f, 1f, 0.1f),
                            Actions.color(Color.WHITE, 0.1f)
                    ));
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) onClick.run();
            }
        });

        return button;
    }



    /**
   * Launches a practice tutorial level by starting the main game at a specific area.
   * @param area The area ID to start in
   */
  private void launchPracticeLevel(MainGameScreen.Areas area) {
    logger.info("Launching practice level: {}", area);
    game.setScreen(new MainGameScreen(game, area));
  }

  /**
   * Displays content for "Items" section
   */
  private void showItemsContent() {
    Label sectionTitle = new Label("Items", skin);
    sectionTitle.setFontScale(1.5f);
    sectionTitle.setColor(Color.GREEN);
    contentTable.add(sectionTitle).padBottom(20).left().colspan(2).row();
    
    // Create table for item sprites
    Table itemsTable = new Table();
    
    // Add each item column (animated atlases and static texture)
    addDisplayColumn(itemsTable, "images/health-potion.atlas", true, "collectable-spin",
            "Health Potion", "Restores HP when collected.", 216, 216, false, 35, 70);
    addDisplayColumn(itemsTable, "images/speed-potion.atlas", true, "collectable-spin",
            "Speed Boost", "Temporarily increases your movement speed.", 216, 216, false, 35, 70);
    addDisplayColumn(itemsTable, "images/slow-potion.atlas", true, "collectable-spin",
            "Slow Potion", "Slows down nearby enemies temporarily.", 216, 216, false, 35, 70);
    addDisplayColumn(itemsTable, "images/key.png", false, null,
            "Key Card", "Required to unlock doors and progress.", 216, 216, false, 35, 70);
    
    contentTable.add(itemsTable).left().colspan(2).row();
    
    // Informational text with markup
    String markedUpText =
            """
            Collect items throughout your journey to aid your survival!
    
            Some items are [RED]auto-consumed[] when collected, while others can be stored in your inventory.
            
            You can view collected items by accessing your inventory with """ + " " + getKeybindText("PauseInventory") + ".";
    
    Label infoText = new Label(markedUpText, skin);
    infoText.setFontScale(1.2f);
    infoText.setWrap(true);
    Label.LabelStyle markupStyle = new Label.LabelStyle(infoText.getStyle());
    markupStyle.fontColor = Color.WHITE;
    infoText.setStyle(markupStyle);
    
    contentTable.add(infoText).fillX().padTop(30).left().colspan(2).row();
  }

  /**
   * Displays content for "Upgrades" section
   */
  private void showUpgradesContent() {
    Label sectionTitle = new Label("Upgrades", skin);
    sectionTitle.setFontScale(1.5f);
    sectionTitle.setColor(Color.GREEN);
    contentTable.add(sectionTitle).padBottom(20).left().colspan(2).row();
    
    // Create table for upgrade sprites
    Table upgradesTable = new Table();
    
    // Add each upgrade column with powerup sprites (static textures)
    addDisplayColumn(upgradesTable, "images/dash_powerup.png", false, null,
        "Dash", getKeybindText("PlayerDash") + "\nQuickly dash forward to dodge enemies and manoeuvre past obstacles.",
        216, 216, false, 100, 120);
    addDisplayColumn(upgradesTable, "images/glide_powerup.png", false, null,
        "Glider", getKeybindText("Glide") + " (hold)\nGlide through the air and reach distant platforms.",
        216, 216, false, 100, 120);
    addDisplayColumn(upgradesTable, "images/jetpack_powerup.png", false, null,
        "Jetpack", getKeybindText("PlayerJump") + " (double tap)\nFly through the air with enhanced vertical mobility.",
        216, 216, false, 100, 120);
    
    contentTable.add(upgradesTable).left().colspan(2).row();
    
    // Informational text with markup
    String markedUpText =
            """
            Unlock upgrades to enhance your movement abilities! These upgrades can be collected throughout the world.
            
            You can view collected upgrades by pressing """ + " " + getKeybindText("PauseUpgrades") + ".";
    
    Label infoText = new Label(markedUpText, skin);
    infoText.setFontScale(1.2f);
    infoText.setWrap(true);
    Label.LabelStyle markupStyle = new Label.LabelStyle(infoText.getStyle());
    markupStyle.fontColor = Color.WHITE;
    infoText.setStyle(markupStyle);
    
    contentTable.add(infoText).fillX().padTop(30).left().colspan(2).row();
  }

  /**
   * Displays content for "Level Mechanics" section
   */
  private void showLevelMechanicsContent() {
    Label sectionTitle = new Label("Level Mechanics", skin);
    sectionTitle.setFontScale(1.5f);
    sectionTitle.setColor(Color.GREEN);
    contentTable.add(sectionTitle).padBottom(20).left().colspan(2).row();
    
    // Create table for first row of mechanics
    Table mechanicsRow1 = new Table();
    
    // First row: Buttons, Moveable Boxes, Pressure Plates, Ladders (preserve aspect ratio)
    addDisplayColumn(mechanicsRow1, "images/button.png", false, null,
        "Buttons", getKeybindText("PlayerInteract") + "\nInteract to activate mechanisms.",
        175, 175, true, 40, 40);
    addDisplayColumn(mechanicsRow1, "images/cube.png", false, null,
        "Moveable Boxes", getKeybindText("PlayerInteract") + "\nPick up and place to solve puzzles.",
        175, 175, true, 40, 40);
    addDisplayColumn(mechanicsRow1, "images/plate.png", false, null,
        "Pressure Plates", "Press with player or box to activate.",
        175, 175, true, 40, 40);
    addDisplayColumn(mechanicsRow1, "images/ladder.png", false, null,
        "Ladders", getKeybindText("PlayerUp") + " (hold)\nClimb to reach higher areas.",
        175, 175, true, 40, 40);
    
    contentTable.add(mechanicsRow1).left().colspan(2).row();
    
    // Create table for second row of mechanics
    Table mechanicsRow2 = new Table();
    
    // Second row: Lasers, Spikes, Minigame Terminals, Doors (preserve aspect ratio)
    addDisplayColumn(mechanicsRow2, "images/laser.atlas", true, "laser-on",
        "Lasers", "Damages player. Can be blocked with boxes.",
        175, 175, true, 40, 40);
    addDisplayColumn(mechanicsRow2, "images/spikes_sprite.png", false, null,
        "Spikes", "Deals damage and knocks player back.",
        175, 175, true, 40, 40);
    addDisplayColumn(mechanicsRow2, "images/terminal_on.png", false, null,
        "Minigame Terminals", getKeybindText("PlayerInteract") + "\nComplete minigames to progress.",
        175, 175, true, 40, 40);
    addDisplayColumn(mechanicsRow2, "images/doors.atlas", true, "door_closed",
        "Doors", getKeybindText("PlayerInteract") + "\nRequires key. Level exit.",
        175, 175, true, 40, 40);
    
    contentTable.add(mechanicsRow2).left().colspan(2).row();
    
    // Informational text with markup
    String markedUpText =
            """
            Master these mechanics to navigate through the facility and overcome obstacles!
            """;
    
    Label infoText = new Label(markedUpText, skin);
    infoText.setFontScale(1.2f);
    infoText.setWrap(true);
    Label.LabelStyle markupStyle = new Label.LabelStyle(infoText.getStyle());
    markupStyle.fontColor = Color.WHITE;
    infoText.setStyle(markupStyle);
    
    contentTable.add(infoText).fillX().padTop(30).left().colspan(2).row();
  }

  /**
   * Displays content for "Lore" section
   */
  private void showLoreContent() {
    Label sectionTitle = new Label("Lore", skin);
    sectionTitle.setFontScale(1.5f);
    sectionTitle.setColor(Color.GREEN);
    contentTable.add(sectionTitle).padBottom(20).left().colspan(2).row();
    
    // Placeholder content
    Label placeholder = new Label("lore", skin);
    contentTable.add(placeholder).left().colspan(2).row();
  }

  @Override
  protected void draw(SpriteBatch batch) {
  }

  @Override
  public void dispose() {
    super.dispose();
  }
  
  /**
   * Custom actor for displaying animated sprites in the UI
   */
  private static class AnimatedImage extends Image {
    private final Animation<TextureRegion> animation;
    private float stateTime = 0;
    
    public AnimatedImage(Animation<TextureRegion> animation) {
      super(animation.getKeyFrame(0));
      this.animation = animation;
    }
    
    @Override
    public void act(float delta) {
      super.act(delta);
      stateTime += delta;
      setDrawable(new TextureRegionDrawable(animation.getKeyFrame(stateTime)));
    }
  }
}

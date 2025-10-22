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
  private TextButton mechanicsBtn;

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

      mechanicsBtn = createSidebarButton("Mechanics", () -> {
          currentSection = "mechanics";
          updateContent(currentSection);
          updateButtonHighlight();
      });

      sidebar.add(basicsBtn).width(240).height(70).padBottom(30).row();
      sidebar.add(itemsBtn).width(240).height(70).padBottom(30).row();
      sidebar.add(mechanicsBtn).width(240).height(70).padBottom(30).row();

      // Set initial highlight
      updateButtonHighlight();

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
      sidebar.add(backBtn).width(240).height(70).bottom().padTop(20);

      return sidebar;
  }


    /**
   * Updates button highlighting based on current section
   */
    private void updateButtonHighlight() {
        basicsBtn.setColor(Color.WHITE);
        itemsBtn.setColor(Color.WHITE);
        mechanicsBtn.setColor(Color.WHITE);

        switch (currentSection) {
            case "basics" -> basicsBtn.setColor(new Color(0f, 1f, 0f, 1f)); // green
            case "items" -> itemsBtn.setColor(new Color(0f, 1f, 0f, 1f));
            case "mechanics" -> mechanicsBtn.setColor(new Color(0f, 1f, 0f, 1f));
        }
    }

    private boolean isActiveButton(TextButton button) {
        return (currentSection.equals("basics") && button == basicsBtn)
                || (currentSection.equals("items") && button == itemsBtn)
                || (currentSection.equals("mechanics") && button == mechanicsBtn);
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
      case "mechanics":
        showMechanicsContent();
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

        // Get the player atlas
        TextureAtlas playerAtlas = ServiceLocator.getResourceService()
                .getAsset("images/PLAYER.atlas", TextureAtlas.class);

        Table spriteTable = new Table();

        // Add each control column if frames exist
        addControlColumn(spriteTable, playerAtlas.findRegions("LEFT"), "PlayerLeft", "Move Left");
        addControlColumn(spriteTable, playerAtlas.findRegions("RIGHT"), "PlayerRight", "Move Right");
        addControlColumn(spriteTable, playerAtlas.findRegions("CROUCH"), "PlayerCrouch", "Crouch");
        addControlColumn(spriteTable, playerAtlas.findRegions("JUMP"), "PlayerJump", "Jump");

        contentTable.add(spriteTable).left().colspan(2).row();

        // Informational text with markup
        String markedUpText =
                """
                These are the basic movement controls. Practice combining them to navigate the world effectively!
        
                [RED]Crouch[] to fit through tight spaces.
        
                Use [RED]jump[] to reach higher platforms. 
        
                You can [CYAN]double-jump[] by pressing [RED]jump[] again while in the air!
                """;

        Label infoText = new Label(markedUpText, skin);
        infoText.setFontScale(1.2f);
        infoText.setWrap(true);
        Label.LabelStyle markupStyle = new Label.LabelStyle(infoText.getStyle());
        markupStyle.fontColor = Color.WHITE;
        infoText.setStyle(markupStyle);

        contentTable.add(infoText).fillX().padTop(30).left().colspan(2).row();

        // Practice button
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

        contentTable.add(practiceBtn).width(300).height(60).padTop(40).center().colspan(2).row();
    }

    /**
     * Helper to add a control column (sprite + key + description) to the sprite table.
     */
    private void addControlColumn(Table spriteTable, Array<TextureAtlas.AtlasRegion> frames,
                                  String actionKey, String description) {
        if (frames.size == 0) return;

        Animation<TextureRegion> animation = new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);
        AnimatedImage sprite = new AnimatedImage(animation);

        Table column = new Table();
        column.add(sprite).size(288, 216).padTop(-50).padBottom(15).center().row();

        int keyCode = Keymap.getActionKeyCode(actionKey);
        String keyName = Input.Keys.toString(keyCode);
        Label keyLabel = new Label(keyName, skin);
        keyLabel.setFontScale(1.5f);
        keyLabel.setColor(Color.RED);
        column.add(keyLabel).center().padTop(5).row();

        Label descLabel = new Label(description, skin);
        descLabel.setFontScale(1.0f);
        column.add(descLabel).center().padTop(5).row();

        spriteTable.add(column).padLeft(35).padRight(35).padBottom(10);
    }

    /**
     * Helper to add an item column (sprite + name + description) to the items table.
     */
    private void addItemColumn(Table itemsTable, String atlasPath, String itemName, String description) {
        TextureAtlas itemAtlas = ServiceLocator.getResourceService()
                .getAsset(atlasPath, TextureAtlas.class);
        
        Array<TextureAtlas.AtlasRegion> frames = itemAtlas.findRegions("collectable-spin");
        if (frames.size == 0) return;

        Animation<TextureRegion> animation = new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);
        AnimatedImage sprite = new AnimatedImage(animation);

        Table column = new Table();
        column.add(sprite).size(216, 216).padBottom(15).center().row();

        Label nameLabel = new Label(itemName, skin);
        nameLabel.setFontScale(1.3f);
        nameLabel.setColor(Color.YELLOW);
        column.add(nameLabel).center().padTop(5).row();

        Label descLabel = new Label(description, skin);
        descLabel.setFontScale(1.0f);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        column.add(descLabel).width(250).center().padTop(5).row();

        itemsTable.add(column).padLeft(35).padRight(70).padBottom(10).expandX().fillX();
    }

    /**
     * Helper to add a static item column (texture + name + description) to the items table.
     */
    private void addStaticItemColumn(Table itemsTable, String texturePath, String itemName, String description) {
        Texture itemTexture = ServiceLocator.getResourceService()
                .getAsset(texturePath, Texture.class);
        
        Image sprite = new Image(itemTexture);

        Table column = new Table();
        column.add(sprite).size(216, 216).padBottom(15).center().row();

        Label nameLabel = new Label(itemName, skin);
        nameLabel.setFontScale(1.3f);
        nameLabel.setColor(Color.YELLOW);
        column.add(nameLabel).center().padTop(5).row();

        Label descLabel = new Label(description, skin);
        descLabel.setFontScale(1.0f);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        column.add(descLabel).width(250).center().padTop(5).row();

        itemsTable.add(column).padLeft(35).padRight(70).padBottom(10).expandX().fillX();
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
    
    // Add each item column
    addItemColumn(itemsTable, "images/health-potion.atlas", "Health Potion", "Restores HP when collected.");
    addItemColumn(itemsTable, "images/speed-potion.atlas", "Speed Boost", "Temporarily increases your movement speed.");
    addItemColumn(itemsTable, "images/slow-potion.atlas", "Slow Potion", "Slows down nearby enemies temporarily.");
    addStaticItemColumn(itemsTable, "images/key.png", "Key Card", "Required to unlock doors and progress.");
    
    contentTable.add(itemsTable).left().colspan(2).row();
    
    // Informational text with markup
    String markedUpText =
            """
            Collect items throughout your journey to aid your survival!
    
            Some items are [RED]auto-consumed[] when collected, while others can be stored in your inventory.
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
   * Displays content for "Mechanics" section
   */
  private void showMechanicsContent() {
    Label sectionTitle = new Label("Mechanics", skin);
    sectionTitle.setFontScale(1.5f);
    sectionTitle.setColor(Color.GREEN);
    contentTable.add(sectionTitle).padBottom(20).left().colspan(2).row();
    
    // Placeholder content
    Label placeholder = new Label("Game mechanics and advanced features will go here...", skin);
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

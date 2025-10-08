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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.GdxGame;
import com.csse3200.game.input.Keymap;
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
    
    // semi-transparent dark background for content area
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(new Color(0, 0, 0, 0.7f)); // Black with 70% opacity
    pixmap.fill();
    Texture backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    
    contentContainer.setBackground(new TextureRegionDrawable(backgroundTexture));
    
    // actual content table
    contentTable = new Table();
    contentTable.top().left();
    contentTable.pad(20); // Add padding inside the dark background
    updateContent(currentSection);
    
    contentContainer.add(contentTable).expand().fill();

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
    sidebar.add(titleLabel).padBottom(60).row();
    
    // Category buttons - save as instance variables
    basicsBtn = new TextButton("The Basics", skin);
    itemsBtn = new TextButton("Items", skin);
    mechanicsBtn = new TextButton("Mechanics", skin);
    
    // Button listeners
    basicsBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        logger.debug("The Basics selected");
        currentSection = "basics";
        updateContent(currentSection);
        updateButtonHighlight();
      }
    });
    
    itemsBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        logger.debug("Items selected");
        currentSection = "items";
        updateContent(currentSection);
        updateButtonHighlight();
      }
    });
    
    mechanicsBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        logger.debug("Mechanics selected");
        currentSection = "mechanics";
        updateContent(currentSection);
        updateButtonHighlight();
      }
    });
    
    // Add buttons to sidebar with much larger size and spacing
    sidebar.add(basicsBtn).width(240).height(70).padBottom(30).row();
    sidebar.add(itemsBtn).width(240).height(70).padBottom(30).row();
    sidebar.add(mechanicsBtn).width(240).height(70).padBottom(30).row();
    
    // Set initial highlight
    updateButtonHighlight();
    
    // Add some spacing before back button
    sidebar.row().expandY();
    
    // Back button at bottom
    TextButton backBtn = new TextButton("Back", skin);
    backBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        logger.debug("Back button clicked");
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
      }
    });
    
    sidebar.add(backBtn).width(240).height(70).bottom().padTop(20);
    
    return sidebar;
  }
  
  /**
   * Updates button highlighting based on current section
   */
  private void updateButtonHighlight() {
    // Reset all buttons to default style
    basicsBtn.setColor(Color.WHITE);
    itemsBtn.setColor(Color.WHITE);
    mechanicsBtn.setColor(Color.WHITE);
    
    // Highlight the active button
    switch (currentSection) {
      case "basics":
        basicsBtn.setColor(Color.GREEN);
        break;
      case "items":
        itemsBtn.setColor(Color.GREEN);
        break;
      case "mechanics":
        mechanicsBtn.setColor(Color.GREEN);
        break;
    }
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
    
    // Movement controls section
    Label movementTitle = new Label("Movement Controls:", skin);
    movementTitle.setFontScale(1.2f);
    contentTable.add(movementTitle).padBottom(15).left().colspan(2).row();
    
    // Create a table for the sprite + keybind display
    Table spriteTable = new Table();
    
    // Create animated sprite for walking left
    Array<TextureAtlas.AtlasRegion> walkLeftFrames = playerAtlas.findRegions("LEFT");
    if (walkLeftFrames.size > 0) {
      Animation<TextureRegion> walkLeftAnimation = new Animation<>(0.1f, walkLeftFrames, Animation.PlayMode.LOOP);
      AnimatedImage leftSprite = new AnimatedImage(walkLeftAnimation);
      
      // Create vertical layout: sprite on top, keybind below
      Table leftColumn = new Table();
      leftColumn.add(leftSprite).size(288, 216).padTop(-50).padBottom(15).center().row();
      
      // Get the actual keybind from user settings
      int leftKeyCode = Keymap.getActionKeyCode("PlayerLeft");
      String leftKeyName = Input.Keys.toString(leftKeyCode);
      Label leftKeyLabel = new Label(leftKeyName, skin);
      leftKeyLabel.setFontScale(1.5f);  // Bigger text
      leftKeyLabel.setColor(Color.RED);  // Red color
      
      // Add padding to shift right and center under the actual sprite visual
      leftColumn.add(leftKeyLabel).center().padLeft(22).padTop(5).row();
      
      // Add description label
      Label leftDescLabel = new Label("Move Left", skin);
      leftDescLabel.setFontScale(1.0f);
      leftColumn.add(leftDescLabel).center().padLeft(22).padTop(5).row();
      
      spriteTable.add(leftColumn).padLeft(10).padRight(10).padBottom(10);
    }
    
    // Create animated sprite for walking right
    Array<TextureAtlas.AtlasRegion> walkRightFrames = playerAtlas.findRegions("RIGHT");
    if (walkRightFrames.size > 0) {
      Animation<TextureRegion> walkRightAnimation = new Animation<>(0.1f, walkRightFrames, Animation.PlayMode.LOOP);
      AnimatedImage rightSprite = new AnimatedImage(walkRightAnimation);
      
      // Create vertical layout: sprite on top, keybind below
      Table rightColumn = new Table();
      rightColumn.add(rightSprite).size(288, 216).padTop(-50).padBottom(15).center().row();
      
      // Get the actual keybind from user settings
      int rightKeyCode = Keymap.getActionKeyCode("PlayerRight");
      String rightKeyName = Input.Keys.toString(rightKeyCode);
      Label rightKeyLabel = new Label(rightKeyName, skin);
      rightKeyLabel.setFontScale(1.5f);  // Bigger text
      rightKeyLabel.setColor(Color.RED);  // Red color
      
      // Add padding to shift right and center under the actual sprite visual
      rightColumn.add(rightKeyLabel).center().padLeft(22).padTop(5).row();
      
      // Add description label
      Label rightDescLabel = new Label("Move Right", skin);
      rightDescLabel.setFontScale(1.0f);
      rightColumn.add(rightDescLabel).center().padLeft(22).padTop(5).row();
      
      spriteTable.add(rightColumn).padLeft(10).padRight(10).padBottom(10);
    }
    
    contentTable.add(spriteTable).left().colspan(2).row();
    
    // Placeholder for more controls
    Label placeholder = new Label("\nMore controls will be added here...", skin);
    contentTable.add(placeholder).left().colspan(2).padTop(20).row();
  }

  /**
   * Displays content for "Items" section
   */
  private void showItemsContent() {
    Label sectionTitle = new Label("Items", skin);
    sectionTitle.setFontScale(1.5f);
    sectionTitle.setColor(Color.GREEN);
    contentTable.add(sectionTitle).padBottom(20).left().colspan(2).row();
    
    // Placeholder content
    Label placeholder = new Label("Item descriptions and usage will go here...", skin);
    contentTable.add(placeholder).left().colspan(2).row();
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

package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.services.CodexEntry;
import com.csse3200.game.services.ServiceLocator;

/**
 * Inventory tab that displays all unlocked codex entries
 */
public class CodexTab implements InventoryTabInterface {
    private final PauseMenuDisplay display;
    /**
     * Reference to background textures that needs to be disposed
     */
    private Texture tableBgTexture;
    private Texture entryBgTexture;
    private Texture titleBgTexture;

    /**
     * Reference to drawables used to give tables a background
     */
    private TextureRegionDrawable tableBgDrawable;
    private TextureRegionDrawable entryBgDrawable;
    private TextureRegionDrawable titleBgDrawable;

    public CodexTab(PauseMenuDisplay display) {
        this.display = display;
    }

    /**
     * Build actor for displaying the UI for the codex
     *
     * @param skin The skin to render all UI with.
     * @return The UI's root actor.
     */
    public Actor build(Skin skin) {
        // Create root table that fills screen
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        // Create table holder that will contain all UI elements
        Table tableHolder = new Table();

        // Create background for tables
        createBgDrawables();
        tableHolder.setBackground(tableBgDrawable);

        // Create logical table (child of scroll pane)
        Table logicalTable = new Table();
        // Add each unlocked entry
        addEntriesToLogicalTable(logicalTable, skin);

        // Need to create a scrollbar style as it is invisible by default
        ScrollPaneStyle scrollPaneStyle = new ScrollPaneStyle();
        // Then make scroll bar
        createScrollbar(scrollPaneStyle);

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(logicalTable.top().left().pad(15f), scrollPaneStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsVisible(true);

        // Add scroll pane and title to table holder
        float canvasH = Gdx.graphics.getHeight() * (3f / 7f);
        float canvasW = Gdx.graphics.getWidth() * (2f / 5f);
        tableHolder.add(new Label("Codex", skin)).pad(20f);
        tableHolder.row();
        tableHolder.add(scrollPane).width(canvasW).height(canvasH);

        // Add the tableHolder directly to the rootTable
        rootTable.add(tableHolder);
        display.getStage().setScrollFocus(scrollPane);

        // Return root table
        return rootTable;
    }

    private void createScrollbar(ScrollPaneStyle scrollPaneStyle) {
        // Scrollbar's background
        Pixmap scrollbarBgPixmap = new Pixmap(2, 1, Pixmap.Format.RGB888);
        scrollbarBgPixmap.setColor(Color.DARK_GRAY);
        scrollbarBgPixmap.fill();
        scrollPaneStyle.vScroll = new TextureRegionDrawable(new Texture(scrollbarBgPixmap));

        // Scrollbar's bar
        Pixmap scrollbarKnobPixmap = new Pixmap(2, 1, Pixmap.Format.RGB888);
        scrollbarKnobPixmap.setColor(Color.LIGHT_GRAY);
        scrollbarKnobPixmap.fill();
        scrollPaneStyle.vScrollKnob = new TextureRegionDrawable(new Texture(scrollbarKnobPixmap));
    }

    private void createBgDrawables() {
        // Create background for table
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0.1f, 0.1f, 0.1f, 1f); // Very dark grey
        bgPixmap.fill();
        tableBgTexture = new Texture(bgPixmap);
        tableBgDrawable = new TextureRegionDrawable(tableBgTexture);
        bgPixmap.dispose();

        // Create background for entry
        Pixmap entryBgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        entryBgPixmap.setColor(0.2f, 0.2f, 0.2f, 1f); // Dark grey
        entryBgPixmap.fill();
        entryBgTexture = new Texture(entryBgPixmap);
        entryBgDrawable = new TextureRegionDrawable(entryBgTexture);
        entryBgPixmap.dispose();

        // Create background for title
        Pixmap titleBgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        titleBgPixmap.setColor(0.3f, 0.3f, 0.3f, 1f); // Medium grey
        titleBgPixmap.fill();
        titleBgTexture = new Texture(titleBgPixmap);
        titleBgDrawable = new TextureRegionDrawable(titleBgTexture);
        entryBgPixmap.dispose();
    }

    private void addEntriesToLogicalTable(Table logicalTable, Skin skin) {
        for (CodexEntry entry : ServiceLocator.getCodexService().getEntries(true)) {
            // Create table for entry
            Table entryTable = new Table();
            entryTable.setBackground(entryBgDrawable);

            // Create table for entry title
            Table titleTable = new Table();
            titleTable.setBackground(titleBgDrawable);
            Label entryTitle = new Label(entry.getTitle(), skin);
            entryTitle.setWrap(true);
            titleTable.add(entryTitle).pad(15f).growX();

            // Add title to entry table
            entryTable.add(titleTable).growX();
            entryTable.row();

            // Add entry text to entry table
            Label entryText = new Label(entry.getText(), skin);
            entryText.setWrap(true);
            entryTable.add(entryText).growX().pad(15f);
            entryTable.row();

            // Add entry table to the logical table with space below it
            logicalTable.add(entryTable).growX().padBottom(15f);
            logicalTable.row();
        }

        // Add some text if user has not found any entries yet
        if (ServiceLocator.getCodexService().getEntries(true).isEmpty()) {
            logicalTable.add(new Label("No entries found yet.", skin)).left().pad(20f).fillX().expandX();
            logicalTable.row();
        }
    }

    /**
     * Dispose of background texture
     */
    public void dispose() {
        tableBgTexture.dispose();
        entryBgTexture.dispose();
        titleBgTexture.dispose();
    }
}
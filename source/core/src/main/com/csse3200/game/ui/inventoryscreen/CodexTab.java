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
    /**
     * Reference to background texture that needs to be disposed
     */
    private Texture bgTexture;
    private final PauseMenuDisplay display;

    public CodexTab(PauseMenuDisplay display) {
        this.display = display;
    }

    /**
     * Build actor for displaying the UI for the codex
     * @param skin The skin to render all UI with.
     * @return The UI's root actor.
     */
    public Actor build(Skin skin) {
        // Create root table that fills screen
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        // Create table holder that will contain all UI elements
        Table tableHolder = new Table();

        // Create background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.1f, 0.1f, 0.1f, 1f); // Grey color
        pixmap.fill();
        bgTexture = new Texture(pixmap);
        tableHolder.setBackground(new TextureRegionDrawable(bgTexture));
        pixmap.dispose();

        // Create logical table (child of scroll pane)
        Table logicalTable = new Table();

        // Add each unlocked entry
        for (CodexEntry entry : ServiceLocator.getCodexService().getEntries(true)) {
            // Add entry title
            String title = entry.getTitle();
            int titleLen = title.length();
            Label entryTitle = new Label(entry.getTitle() + "\n" + "=".repeat(titleLen), skin);
            logicalTable.add(entryTitle).fillX().expandX();
            logicalTable.row();

            // Add entry text
            Label entryText = new Label(entry.getText(), skin);
            entryText.setWrap(true);
            logicalTable.add(entryText).left().pad(20f).padBottom(35f).fillX().expandX();
            logicalTable.row();
        }

        // Add some text if user has not found any entries yet
        if (ServiceLocator.getCodexService().getEntries(true).isEmpty()) {
            logicalTable.add(new Label("No entries found yet.", skin)).left().pad(20f).fillX().expandX();
            logicalTable.row();
        }

        // Need to create a scrollbar style as it is invisible by default
        ScrollPaneStyle scrollPaneStyle = new ScrollPaneStyle();

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

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(logicalTable.top().left().pad(15f), scrollPaneStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsVisible(true);

        // Add scroll pane and title to table holder
        float canvasH = Gdx.graphics.getHeight() * (3f / 7f);
        float canvasW = Gdx.graphics.getWidth() * (2f / 5f);
        tableHolder.add(new Label("=== Codex ===", skin)).pad(20f);
        tableHolder.row();
        tableHolder.add(scrollPane).width(canvasW).height(canvasH);

        // Add the tableHolder directly to the rootTable
        rootTable.add(tableHolder);
        display.getStage().setScrollFocus(scrollPane);

        // Return root table
        return rootTable;
    }

    /**
     * Dispose of background texture
     */
    public void dispose() {
        bgTexture.dispose();
    }
}
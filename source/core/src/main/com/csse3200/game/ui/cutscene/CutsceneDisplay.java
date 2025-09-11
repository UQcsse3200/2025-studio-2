package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.cutscene.CutsceneReaderComponent.TextBox;
import com.github.tommyettinger.textra.TypingLabel;

import java.util.List;

public class CutsceneDisplay extends UIComponent {
    /**
     * Ordered list of text boxes to be displayed in cutscene
     */
    private final List<TextBox> textBoxList;
    /**
     * A reference to what text box is currently being displayed
     */
    private int curTextBox = 0;
    /**
     * Root table to be rendered
     */
    private Table rootTable;
    /**
     * Text table inside of root table
     */
    private Table textTable;
    /**
     * Texture for background behind the text
     */
    private Texture textBgTexture;

    /**
     * Initialises display with text box information
     * @param textBoxList A list of all text boxes created from a CutsceneRenderComponent
     */
    public CutsceneDisplay(List<TextBox> textBoxList) {
        this.textBoxList = textBoxList;
    }

    @Override
    public void create() {
        super.create();

        // Create root table - fills screen
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom();

        // Create table for text box
        Table textBox = new Table();

        // Add text table to text box
        textTable = new Table();
        textBox.add(textTable).expandX().fillX().pad(20f);

        // Create button and add to text box
        TextButton progressButton = new TextButton("Next", skin);
        rootTable.add(progressButton).pad(20f);

        // Create texture for background behind text
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.5f);
        pixmap.fill();
        textBgTexture = new Texture(pixmap);
        pixmap.dispose();
        textBox.setBackground(new Image(textBgTexture).getDrawable());

        // Add text box to root table
        rootTable.add(textBox).expandX().fillX().pad(10f);

        // Create listener for progress button
        progressButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        // Do nothing for now if we are on last index
                        if (curTextBox == textBoxList.size() - 1) {
                            return;
                        }
                        nextTextBox();
                    }
                }
        );

        // Draw the first text box
        setTextBox();

        // Add table to scene
        stage.addActor(rootTable);
    }

    private void setTextBox() {
        // Clear text table
        if (textTable != null) {
            textTable.clear();
        }
        // Get text box from list
        TextBox textBox = textBoxList.get(curTextBox);

        // Generate label
        TypingLabel text = new TypingLabel(textBox.text(), skin);
        // Add label to text table
        textTable.add(text).expandX().fillX().pad(15f);
    }

    private void nextTextBox() {
        curTextBox++;
        setTextBox();
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // Handled by Scene2D
    }

    @Override
    public void dispose() {
        super.dispose();
        if (rootTable != null) {
            rootTable.remove();
        }
        if (textBgTexture != null) {
            textBgTexture.dispose();
        }
    }
}

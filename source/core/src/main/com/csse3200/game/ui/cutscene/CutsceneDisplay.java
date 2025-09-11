package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.cutscene.CutsceneReaderComponent.TextBox;
import com.github.tommyettinger.textra.TypingAdapter;
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
     * Boolean representing whether the current text box can be progressed
     */
    private boolean canProgress = false;
    /**
     * Root table to be rendered
     */
    private Table rootTable;
    /**
     * Text table inside of root table
     */
    private Table textTable;
    /**
     * Button for managing current text box
     */
    private TextButton progressButton;

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

        // Add text table to root table
        textTable = new Table();
        rootTable.add(textTable);

        // Draw the first text box
        setTextBoxTyping();

        // Add table to scene
        stage.addActor(rootTable);

        // Create button for skipping/going to next text box
        createButton();
    }

    private void createButton() {
        // Create button and add to root table
        progressButton = new TextButton("Skip", skin);
        rootTable.add(progressButton);

        progressButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (canProgress) {
                        // Change to next text box
                        nextTextBox();
                        progressButton.setText("Skip");
                    } else {
                        // Skip current text box
                        setTextBoxStatic();
                        progressButton.setText("Next");
                    }

                    // Flip value of progress value
                    canProgress = !canProgress;
                }
            }
        );
    }

    private void setTextBoxTyping() {
        // Clear text table
        if (textTable != null) {
            textTable.clear();
        }
        // Get text box from list
        TextBox textBox = textBoxList.get(curTextBox);

        // Generate label
        TypingLabel text = new TypingLabel(textBox.text(), skin);
        text.setWrap(true);
        // Add label to text table
        textTable.add(text);

        // Create listener for when text is finished typing, allow next text box when done
        text.setTypingListener(new TypingAdapter() {
            public void end() {
                canProgress = true;
                progressButton.setText("Next");
            }
        });
    }

    private void setTextBoxStatic() {
        // Clear text table
        if (textTable != null) {
            textTable.clear();
        }
        // Get text box from list
        TextBox textBox = textBoxList.get(curTextBox);

        // Generate label
        Label text = new Label(textBox.text(), skin);
        text.setWrap(true);
        // Add label to table
        textTable.add(text);
    }

    private void nextTextBox() {
        curTextBox++;
        setTextBoxTyping();
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
    }
}

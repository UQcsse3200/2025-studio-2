package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.cutscene.CutsceneReaderComponent.TextBox;

import java.util.List;

public class CutsceneDisplay extends UIComponent {
    /**
     * Ordered list of text boxes to be displayed in cutscene
     */
    private final List<TextBox> textBoxList;
    /**
     * Root table to be rendered
     */
    private Table rootTable;

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

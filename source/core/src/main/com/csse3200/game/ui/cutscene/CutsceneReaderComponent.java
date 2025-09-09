package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;

/**
 * Component that can be attached to an entity representing a cutscene. Reads a script file for a
 * cutscene and generates relevant structures for displaying the cutscene.
 */
public class CutsceneReaderComponent extends Component {
    /**
     * The file path to the cutscene script from relative to the resources root
     */
    private final String scriptPath;

    public CutsceneReaderComponent(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public void create() {
        // Read contents of the script file into a string
        String scriptContents = Gdx.files.internal(scriptPath).readString();
    }
}

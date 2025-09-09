package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component that can be attached to an entity representing a cutscene. Reads a script file for a
 * cutscene and generates relevant structures for displaying the cutscene.
 */
public class CutsceneReaderComponent extends Component {
    /**
     * Logger for sending debug messages
     */
    private static final Logger logger = LoggerFactory.getLogger(CutsceneReaderComponent.class);
    /**
     * The file path to the cutscene script from relative to the resources root
     */
    private final String scriptPath;
    /**
     * Ordered list of all text boxes found in script file. Text boxes contain original formatting
     */
    private final List<String> textBoxes = new ArrayList<>();
    /**
     * Mapping for text box indexes to desired background's file path. If a mapping does not
     * exist, current background is re-used for index
     */
    private final Map<Integer, String> backgroundMap = new HashMap<>();

    public CutsceneReaderComponent(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public void create() {
        // Read contents of the script file into a string
        String scriptContents = Gdx.files.internal(scriptPath).readString();
        logger.debug("Script Contents:\n{}", scriptContents);
    }
}

package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private final List<TextBox> textBoxes = new ArrayList<>();

    /**
     * Constructs the component with a specified path to the script file it shall read.
     *
     * @param scriptPath The path to the script file being read
     */
    public CutsceneReaderComponent(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public void create() {
        // Read contents of the script file into a string
        String scriptContents = Gdx.files.internal(scriptPath).readString();
        logger.debug("Script Contents:\n{}", scriptContents);
        try {
            parseScript(scriptContents);
        } catch (IOException e) {
            logger.error("Failed to parse cutscene script {}", scriptPath, e);
        }
    }

    /**
     * Helper method for iterating through the contents of a script file (as a string) and creating
     * a list of text boxes containing information about background being set.
     * Text boxes in the list are in the same order as they are parsed.
     *
     * @param scriptContents The contents of the script file as a string.
     * @throws IOException If the script file is empty, or the first line does not set a background.
     */
    private void parseScript(String scriptContents) throws IOException {
        // Split the scriptContents into lines (works on windows and unix systems)
        String[] scriptLines = scriptContents.split(System.lineSeparator());

        // Throw error if file is empty
        if (scriptLines.length == 0) {
            throw new IOException("Script file '" + scriptPath + "' is empty.");
        }

        int lineIndex = 0;
        String lastCommand = null;
        for (String line : scriptLines) {
            // Check if we need to interpret line as a text box or command
            boolean isText = line.isEmpty() || line.charAt(0) != '#';

            // Throw error if first line in file does not set a background
            if (lineIndex == 0 && isText) {
                throw new IOException("First line in script file '" + scriptPath + "' does not " +
                        "set background.");
            }

            // Parse line as text box or command
            if (isText) {
                textBoxes.add(new TextBox(line, lastCommand));
                lastCommand = null;
            } else {
                lastCommand = line;
            }

            lineIndex++;
        }

    }

    /**
     * Getter method that returns the parsed text boxes from the parsed script file.
     *
     * @return A list containing text boxes in the order they were parsed.
     */
    public List<TextBox> getTextBoxes() {
        return textBoxes;
    }

    /**
     * Record object that represents information about a text box.
     *
     * @param text       The text to be drawn inside the text box.
     * @param background null means use the current background. Otherwise, can contain path to
     *                   background asset to be used for this text box.
     */
    public record TextBox(String text, String background) {
    }
}

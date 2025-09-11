package com.csse3200.game.areas;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.cutscene.CutsceneDisplay;
import com.csse3200.game.ui.cutscene.CutsceneReaderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CutsceneArea extends GameArea {
    /**
     * Logger for cutscene area
     */
    private static final Logger logger = LoggerFactory.getLogger(CutsceneArea.class);
    /**
     * Dummy assets that need to be loaded in a GameArea
     */
    private static final String[] dummyAssets = {
            "images/box_boy_leaf.png",
            "images/minimap_player_marker.png"
    };
    /**
     * Cutscene script path
     */
    private final String scriptPath;
    /**
     * The ID of the next area to load after cutscene finishes
     */
    private final String nextAreaID;
    /**
     * Reader entity that can read a script file
     */
    private Entity reader;
    /**
     * Cutscene backgrounds
     */
    private String[] backgrounds;

    /**
     * Constructor that initialises game area. Creates cutscene entity using a provided script file.
     *
     * @param scriptPath The path to the cutscene script file relative to the resources root.
     */
    public CutsceneArea(String scriptPath, String nextAreaID) {
        this.scriptPath = scriptPath;
        this.nextAreaID = nextAreaID;
    }

    @Override
    public void create() {
        // Create reader and read
        reader = new Entity();
        reader.addComponent(new CutsceneReaderComponent(scriptPath));
        spawnEntity(reader);

        // Load background assets from script
        loadAssets();

        // Create cutscene UI
        Entity cutscene = new Entity();
        cutscene.addComponent(new CutsceneDisplay(reader.getComponent(CutsceneReaderComponent.class).getTextBoxes(), this, nextAreaID));
        spawnEntity(cutscene);

        // Create dummy player
        player = PlayerFactory.createPlayer();
    }

    private void loadAssets() {
        logger.debug("Attempting to load background assets");

        // Get resource service
        ResourceService resourceService = ServiceLocator.getResourceService();

        // Get reader component, and send background asset paths to resource service
        CutsceneReaderComponent readerComp = reader.getComponent(CutsceneReaderComponent.class);
        resourceService.loadTextures(readerComp.getBackgrounds());

        // Need to load some dummy assets to prevent crashing
        resourceService.loadTextures(dummyAssets);

        // Show loading progress in logs
        while (!resourceService.loadForMillis(10)) {
            logger.info("Loading... {}%", resourceService.getProgress());
        }
    }

    private void unloadAssets() {
        logger.debug("Attempting to unload background assets");

        // Get resource service
        ResourceService resourceService = ServiceLocator.getResourceService();

        // Unload background assets from reader component
        CutsceneReaderComponent readerComp = reader.getComponent(CutsceneReaderComponent.class);
        resourceService.unloadAssets(readerComp.getBackgrounds());

        // Unload dummy assets
        resourceService.unloadAssets(dummyAssets);

    }


    @Override
    protected void reset() {
        // Reset does nothing for cutscene
    }

    @Override
    public void dispose() {
        // Remove dummy player
        ServiceLocator.getEntityService().unregister(player);

        unloadAssets();
        super.dispose();
    }
}

package com.csse3200.game.ui.cutscene;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special game area that can be used to display a cutscene. Works as a game level, so can be
 * transitioned into from another.
 */
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
     * Constructor for creating the game area representing cutscene.
     * @param scriptPath The file path to the cutscene script to be given to the reader component
     * @param nextAreaID The ID for the next game area to redirect to after cutscene finishes
     */
    public CutsceneArea(String scriptPath, String nextAreaID) {
        this.scriptPath = scriptPath;
        this.nextAreaID = nextAreaID;
    }

    /**
     * Creates a reader entity with parses a script file, then an entity for displaying/handling UI.
     * Additionally, this method creates a dummy player (requirement of GameArea)
     */
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

    /**
     * Private method for loading all assets into the resource service. This includes dummy assets
     * for the minimap and player, as well as backgrounds for the cutscene
     */
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

    /**
     * Unloads previously created dummy assets and backgrounds from the resource service
     */
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

    /**
     * Unused - area is never reset (requirement of GameArea)
     */
    @Override
    protected void reset() {
        // Reset does nothing for cutscene
    }

    /**
     * Correctly unregisters the player, any assets loaded, and entities created for the cutscene
     */
    @Override
    public void dispose() {
        // Remove dummy player
        ServiceLocator.getEntityService().unregister(player);

        unloadAssets();
        super.dispose();
    }
}

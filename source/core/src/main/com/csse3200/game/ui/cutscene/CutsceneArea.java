package com.csse3200.game.ui.cutscene;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
     * Cutscene script path
     */
    private final String scriptPath;
    /**
     * Reader entity that can read a script file
     */
    private Entity reader;
    /**
     * Dummy assets that need to be loaded for the game area (sadly)
     */
    private static final String[] dummyTextures = {
            "images/box_boy_leaf.png",
            "images/minimap_player_marker.png"
    };
    private static final String[] dummyAtlases = {
            "images/PLAYER.atlas"
    };
    private static final String[] dummySounds = {
            "sounds/Impact4.ogg",
            "sounds/jetpacksound.mp3",
            "sounds/walksound.mp3",
            "sounds/buttonsound.mp3"
    };

    /**
     * Constructor for creating the game area representing cutscene.
     * @param scriptPath The file path to the cutscene script to be given to the reader component
     */
    public CutsceneArea(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    /**
     * There are no pre-reqs for cutscene areas
     */
    @Override
    protected void loadPrerequisites() {
        // Does nothing for cutscene
    }

    /**
     * Creates a reader entity with parses a script file, then an entity for displaying/handling UI.
     * Additionally, this method creates a dummy player (requirement of GameArea)
     */
    @Override
    protected void loadEntities() {
        // Create reader and read
        reader = new Entity();
        reader.addComponent(new CutsceneReaderComponent(scriptPath));
        spawnEntity(reader);

        // Load backgrounds from the reader
        loadBackgroundsFromReader();

        // Create cutscene UI
        Entity cutscene = new Entity();
        cutscene.addComponent(new CutsceneDisplay(reader.getComponent(CutsceneReaderComponent.class).getTextBoxes(), this));
        spawnEntity(cutscene);
    }

    /**
     * Private method for loading all assets into the resource service. This includes dummy assets
     * for the minimap and player, as well as backgrounds for the cutscene
     */
    protected void loadAssets() {
        logger.debug("Loading assets");
        // Get resource service
        ResourceService resourceService = ServiceLocator.getResourceService();

        // Load in the dummy assets
        resourceService.loadTextures(dummyTextures);
        resourceService.loadTextureAtlases(dummyAtlases);
        resourceService.loadSounds(dummySounds);

        // Show loading progress in logs
        while (!resourceService.loadForMillis(10)) {
            logger.info("Loading... {}%", resourceService.getProgress());
        }
    }

    private void loadBackgroundsFromReader() {
        logger.debug("Loading background assets for cutscene");
        // Get resource service
        ResourceService resourceService = ServiceLocator.getResourceService();

        // Get reader component, and send background asset paths to resource service
        CutsceneReaderComponent readerComp = reader.getComponent(CutsceneReaderComponent.class);
        resourceService.loadTextures(readerComp.getBackgrounds());

        // Show loading progress in logs
        while (!resourceService.loadForMillis(10)) {
            logger.info("Loading... {}%", resourceService.getProgress());
        }
    }

    /**
     * Unloads previously created dummy assets and backgrounds from the resource service
     */
    private void unloadAssets() {
        logger.debug("Attempting to unload background assets and dummy assets");

        // Get resource service
        ResourceService resourceService = ServiceLocator.getResourceService();

        // Unload background assets from reader component
        CutsceneReaderComponent readerComp = reader.getComponent(CutsceneReaderComponent.class);
        resourceService.unloadAssets(readerComp.getBackgrounds());

        // Unload dummy assets
        resourceService.unloadAssets(dummyTextures);
        resourceService.unloadAssets(dummyAtlases);
        resourceService.unloadAssets(dummySounds);
    }

    /**
     * Cutscene areas cannot be reset
     */
    @Override
    public void reset() {
        // Reset does nothing for cutscene
    }

    /**
     * Create dummy player for cutscene
     */
    @Override
    protected Entity spawnPlayer() {
        Entity dummyPlayer = PlayerFactory.createPlayer(new ArrayList<>());
        dummyPlayer.getEvents().addListener("reset", this::reset);
        return dummyPlayer;
    }

    /**
     * Create dummy player for cutscene
     */
    @Override
    protected Entity spawnPlayer(List<Component> componentList) {
        Entity dummyPlayer = PlayerFactory.createPlayer(componentList);
        dummyPlayer.getEvents().addListener("reset", this::reset);
        return dummyPlayer;
    }

    /**
     * Correctly unregisters the player, any assets loaded, and entities created for the cutscene
     */
    @Override
    public void dispose() {
        super.dispose();
        unloadAssets();
    }
}

package com.csse3200.game.areas;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.cutscene.CutsceneReaderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CutsceneArea extends GameArea {
    /**
     * Logger for cutscene area
     */
    private static final Logger logger = LoggerFactory.getLogger(CutsceneArea.class);
    /**
     * Cutscene entity that can read a script file and draw UI
     */
    private Entity cutscene;
    /**
     * Cutscene script path
     */
    private final String scriptPath;
    /**
     * Cutscene backgrounds
     */
    private String[] backgrounds;

    /**
     * Constructor that initialises game area. Creates cutscene entity using a provided script file.
     * @param scriptPath The path to the cutscene script file relative to the resources root.
     */
    public CutsceneArea(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public void create() {
        // Create cutscene entity
        spawnCutsceneEntity();

        // Load background assets from script
        loadAssets();
    }

    private void spawnCutsceneEntity() {
        // Establish entity and add components
        cutscene = new Entity();
        cutscene.addComponent(new CutsceneReaderComponent(scriptPath));

        // Add to list of entities in area and register
        spawnEntity(cutscene);
    }

    private void loadAssets() {
        logger.debug("Attempting to load background assets");

        // Get resource service
        ResourceService resourceService = ServiceLocator.getResourceService();

        // Get reader component, and send background asset paths to resource service
        CutsceneReaderComponent reader = cutscene.getComponent(CutsceneReaderComponent.class);
        resourceService.loadTextures(reader.getBackgrounds());

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
        CutsceneReaderComponent reader = cutscene.getComponent(CutsceneReaderComponent.class);
        resourceService.unloadAssets(reader.getBackgrounds());
    }


    @Override
    protected void reset() {
        // Reset does nothing for cutscene
    }

    @Override
    public void dispose() {
        super.dispose();
        unloadAssets();
    }
}

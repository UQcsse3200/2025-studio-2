package com.csse3200.game.services;

import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards terminal UI events to a registered UI entity
 * (the one that owns TerminalUiComponent).
 */
public class ComputerTerminalService implements Disposable {
    private static final Logger logger = LoggerFactory.getLogger(ComputerTerminalService.class);

    private Entity uiEntity; // the UI entity that owns TerminalUiComponent

    // Register the UI entity once after it’s created
    public void registerUiEntity(Entity uiEntity) {
        this.uiEntity = uiEntity;
        logger.debug("ComputerTerminalService registered UI entity {}", uiEntity);
    }

    // Open the terminal UI for a given terminal entity
    public void open(Entity terminalEntity) {
        if (uiEntity == null) {
            logger.error("ComputerTerminalService: uiEntity not registered; open() ignored");
            return;
        }
        uiEntity.getEvents().trigger("terminal:open", terminalEntity);
    }

    // Close the terminal UI
    public void close() {
        if (uiEntity == null) return;
        uiEntity.getEvents().trigger("terminal:close");
    }

    // Toggle the terminal UI
    public void toggle(Entity terminalEntity) {
        if (uiEntity == null) return;
        uiEntity.getEvents().trigger("terminal:toggle", terminalEntity);
    }

    @Override
    public void dispose() {
        uiEntity = null;
    }
}
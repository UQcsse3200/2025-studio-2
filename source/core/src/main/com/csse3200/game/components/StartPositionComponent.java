package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores position that the entity spawns for reset logic and AI tasks.
 */
public class StartPositionComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(StartPositionComponent.class);
    private Vector2 startPos;

    public StartPositionComponent(Vector2 startPos) {
        this.startPos = new Vector2(startPos);
    }

    public void setStartPos(Vector2 worldPos) {
        if (worldPos != null) {
            startPos = new Vector2(worldPos);
        }
    }

    public Vector2 getStartPos() {
        return new Vector2(startPos);
    }
}
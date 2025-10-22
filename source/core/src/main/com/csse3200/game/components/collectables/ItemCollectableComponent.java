package com.csse3200.game.components.collectables;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.utils.CollectablesSave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemCollectableComponent extends CollectableComponent {
    private static final Logger logger = LoggerFactory.getLogger(ItemCollectableComponent.class);

    private static int count;
    private Vector2[] collected = new Vector2[9];

    public ItemCollectableComponent() {
      initialiseCollected();
    }

    private void initialiseCollected() {
        collected = CollectablesSave.loadCollectedPositions();
        count = CollectablesSave.getCollectedCount();
    }

    public static int getCount() {
        return count;
    }

    @Override
    protected boolean onCollect(Entity collector) {
        Vector2 currentPos = this.entity.getPosition();
        if (!hasBeenCollected(currentPos)) {
            addCollected(currentPos);
            CollectablesSave.incrementCollectedCount();
            count = CollectablesSave.getCollectedCount();
        }
        logger.debug("{}", this.entity.getPosition());
        logger.info("CollectablesSave.getCollectedCount() = {}", count);
        logger.info("CollectablesSave.getCollectedCount() = {}", count);

        collector.getEvents().trigger("updateCollectables", count);
        //CollectablesSave.resetCollectedCount();
        //collector.removeComponent(this);
        return true;
    }

    private boolean hasBeenCollected(Vector2 pos) {
        for (Vector2 v : collected) {
            if (v.x != 0 && v.y != 0 && v.epsilonEquals(pos, 0.01f)) {
                return true;
            }
        }
        return false;
    }

    private void addCollected(Vector2 pos) {
        for (int i = 0; i < collected.length; i++) {
            if (collected[i].x == 0 && collected[i].y == 0) {
                collected[i] = pos;
                CollectablesSave.saveCollectedPositions(i, pos);
                CollectablesSave.saveCollectedPositions(collected);
                break;
            }
        }
    }
}

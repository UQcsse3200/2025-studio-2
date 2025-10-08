package com.csse3200.game.components.collectables;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.LevelOneGameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.utils.CollectableCounter;
import com.csse3200.game.utils.CollectablesSave;

public class ItemCollectableComponent extends CollectableComponent {
    private int count;
    private GameArea gameArea;
    private Vector2[] collected = new Vector2[9];

    public ItemCollectableComponent(GameArea gameArea) {
        this.gameArea = gameArea;
        initialiseCollected();
    }

    private void initialiseCollected() {
        collected = CollectablesSave.loadCollectedPositions();
        count = CollectablesSave.getCollectedCount();
    }

    @Override
    protected boolean onCollect(Entity collector) {
        Vector2 currentPos = this.entity.getPosition();
        if (!hasBeenCollected(currentPos)) {
            addCollected(currentPos);
            CollectablesSave.incrementCollectedCount();
            count = CollectablesSave.getCollectedCount();
        }
        System.out.println(this.entity.getPosition());
        System.out.println("CollectablesSave.getCollectedCount() = " + count);
        System.out.println("CollectablesSave.getCollectedCount() = " + count);
        //spawnCollectable(new Vector2(33.5f, -1.5f));
        //spawnCollectable(new Vector2(0f, 23f));
        //spawnCollectable(new Vector2(39.5f, 30f));

        //spawnCollectable(new Vector2(30.5f, 32.75f));
        //spawnCollectable(new Vector2(47.5f, 18f));
        //spawnCollectable(new Vector2(8.5f, 0.4f));

        collector.getEvents().trigger("updateCollectables", count);
        //CollectablesSave.resetCollectedCount();
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

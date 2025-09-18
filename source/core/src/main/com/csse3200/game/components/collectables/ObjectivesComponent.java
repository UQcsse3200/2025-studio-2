package com.csse3200.game.components.collectables;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;

public class ObjectivesComponent extends CollectableComponent {

    public final String objectiveId;

    public ObjectivesComponent(String objectiveId) {
        this.objectiveId = objectiveId;
    }

    @Override
    protected boolean onCollect(Entity collector) {
        // Add to the OBJECTIVES bag
        InventoryComponent inv = collector.getComponent(InventoryComponent.class);
        if (inv == null) {
            Gdx.app.error("Objective", "Collector has no InventoryComponent");
            return false;
        }
        inv.addItem(InventoryComponent.Bag.OBJECTIVES, objectiveId);

        // Branch per objective id (hook up UI/toasts/etc. here)
        switch (objectiveId) {
            case "keycard" -> Gdx.app.log("Objective", "Collected Security Keycard");
            case "door"    -> Gdx.app.log("Objective", "Found the door objective");
            case "tutorial"-> Gdx.app.log("Objective", "Collected tutorial objective");
            default         -> Gdx.app.log("Objective", "Collected: " + objectiveId);
        }

        collector.getEvents().trigger("objectiveCollected", objectiveId);

        return true;
    }

    public String getObjectivesId() {return objectiveId;}
}

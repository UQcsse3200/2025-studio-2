package com.csse3200.game.components;

import org.junit.Test;
import static org.junit.Assert.*;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

public class PressurePlateComponentTest {
    @Test
    public void pressesWhenColliderProvided() {
        Entity plate = new Entity();
        PressurePlateComponent comp = new PressurePlateComponent();
        plate.addComponent(comp);
        plate.create();

        final boolean[] pressedEvent = {false};
        plate.getEvents().addListener("plateToggled", (Boolean p) -> pressedEvent[0] = p);

        // textures optional; updateTexture() no-ops if no renderer
        comp.setTextures("images/plate.png", "images/plate-pressed.png");

        // simulate stepping on the plate
        comp.setPlayerOnPlate(new ColliderComponent());

        assertTrue("Plate should emit pressed=true when stepped on", pressedEvent[0]);
    }
}
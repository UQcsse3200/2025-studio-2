package com.csse3200.game.components;

import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/** Lets a door react to "openDoor"/"closeDoor" events. */
public class DoorControlComponent extends Component {
    private TextureRenderComponent render;
    private ColliderComponent collider;

    @Override
    public void create() {
        render = entity.getComponent(TextureRenderComponent.class);
        collider = entity.getComponent(ColliderComponent.class);
        entity.getEvents().addListener("openDoor", this::open);
        entity.getEvents().addListener("closeDoor", this::close);
        close(); // start closed
    }

    private void open() {
        if (render != null) render.setTexture("images/door_open.png");
        if (collider != null) collider.setSensor(true);   // pass-through
    }

    private void close() {
        if (render != null) render.setTexture("images/door_closed.png");
        if (collider != null) collider.setSensor(false);  // solid
    }
}
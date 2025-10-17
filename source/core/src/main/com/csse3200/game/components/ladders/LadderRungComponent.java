package com.csse3200.game.components.ladders;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;

public class LadderRungComponent extends Component {
    private final String id;
    private final int rungIdx;
    private final Vector2 visiblePos = new Vector2();

    private static final Vector2 HIDDEN_POS = new Vector2(-100f, -100f);
    private PhysicsComponent physics;

    public LadderRungComponent(String id, int rungIdx) {
        this.id = id;
        this.rungIdx = rungIdx;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);

        // on first create capture world pos as the "visible" position
        visiblePos.set(entity.getPosition());
    }

    public void show() {
        // restore to visible pos
        entity.setPosition(visiblePos);

        if (physics != null) {
            physics.getBody().setActive(true);
        }
    }

    public void hide() {
        // hide rung by teleporting away
        entity.setPosition(HIDDEN_POS);

        if (physics != null) {
            physics.getBody().setActive(false);
        }
    }

    public String getId() {
        return id;
    }

    public int getRungIdx() {
        return rungIdx;
    }

    public Vector2 getVisiblePos() {
        return visiblePos;
    }

    public void setVisiblePos(Vector2 visiblePos) {
        this.visiblePos.set(visiblePos);
    }
}

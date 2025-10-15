package com.csse3200.game.components.ladders;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.LadderFactory;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

public class AnchorLadderComponent extends Component {
    private final int height;
    private final int offset;
    private final String id;

    private final List<Entity> spawnedRungs =  new ArrayList<>();

    public AnchorLadderComponent(String id, int height, int offset) {
        this.height = height;
        this.offset = offset;
        this.id = id;
    }

    @Override
    public void create() {
        Vector2 initPos = entity.getPosition();

        for (int i = 0; i < height; i++) {
            Entity rung = LadderFactory.createStaticLadder();
            LadderRungComponent rungComp = new LadderRungComponent(id, i);
            rung.addComponent(rungComp);

            rung.setScale(1f, 1f);

            rung.setPosition(initPos.x, initPos.y + (i / 2f));
            ServiceLocator.getEntityService().register(rung);
            spawnedRungs.add(rung);

            // hide initial rungs until the plate reveals them
            if (i < offset) {
                rungComp.hide();
            }
        }
    }

    @Override
    public void dispose() {
        for (Entity rung : spawnedRungs) {
            rung.dispose();
        }
    }
}

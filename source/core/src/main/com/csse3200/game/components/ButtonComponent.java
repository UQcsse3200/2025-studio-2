package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ButtonFactory;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class ButtonComponent extends Component {
    private boolean isPushed = false;

    @Override
    public void create() {

        entity.getEvents().addListener("push", this::onPush);

    }

    private void onPush(Object other) {
        if (other instanceof ColliderComponent) {
            Entity otherEntity = ((ColliderComponent) other).getEntity();

            if (otherEntity.getComponent(PlayerActions.class) != null) {
                toggleButton();
            }
        }
    }

    private void toggleButton() {
        isPushed = !isPushed;

        Vector2 pos = entity.getPosition().cpy();

        entity.dispose();
        Entity newButton = ButtonFactory.createButton(isPushed);
        newButton.setPosition(pos);
        ServiceLocator.getEntityService().register(newButton);
    }


    public void setPushed(boolean pushed) { this.isPushed = pushed; }
}

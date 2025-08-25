package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class ButtonComponent extends Component {
    private boolean isPushed = false;
    private float cooldown = 0f;
    private String type;

    @Override
    public void create() {

        entity.getEvents().addListener("push", this::onPush);

    }

    @Override
    public void update() {
        if(cooldown > 0) {
            cooldown -= ServiceLocator.getTimeSource().getDeltaTime();
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    private void onPush(Object other) {
        if(cooldown > 0) {
            return;
        }
        if (other instanceof ColliderComponent) {
            Entity otherEntity = ((ColliderComponent) other).getEntity();

            if (otherEntity.getComponent(PlayerActions.class) != null) {
                Vector2 playerPos = otherEntity.getPosition();
                Vector2 buttonPos = entity.getPosition();

                if(playerPos.x < buttonPos.x - 0.5f) {
                    toggleButton();
                    cooldown = 0.5f;
                }
            }
        }
    }

    private void toggleButton() {
        isPushed = !isPushed;
        entity.getEvents().trigger("buttonToggled", isPushed);

        if("platform".equals(type)) {
            String texture = isPushed ? "images/blue_button_pushed.png" : "images/blue_button.png";
            TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
            if (render != null) {
                render.setTexture(texture);
            }
        }else if("door".equals(type)) {
            String texture = isPushed ? "images/red_button_pushed.png" : "images/red_button.png";
            TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
            if (render != null) {
                render.setTexture(texture);
            }
        }else {
            String texture = isPushed ? "images/button_pushed.png" : "images/button.png";
            TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
            if (render != null) {
                render.setTexture(texture);
            }
        }
    }


    public void setPushed(boolean pushed) { this.isPushed = pushed; }
}

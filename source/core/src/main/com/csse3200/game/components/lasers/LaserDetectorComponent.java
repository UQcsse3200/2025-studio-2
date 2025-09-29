package com.csse3200.game.components.lasers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class LaserDetectorComponent extends Component {

    private ColliderComponent collider;
    private TextureRenderComponent texture;
    private ConeLightComponent light;

    private boolean detecting = false;

    private static final String[] textures = {"images/laser-detector-off.png", "images/laser-detector-on.png"};

    public LaserDetectorComponent() {

    }

    @Override
    public void create() {
        collider = entity.getComponent(ColliderComponent.class);
        texture = entity.getComponent(TextureRenderComponent.class);
        light = entity.getComponent(ConeLightComponent.class);

        if (texture == null) {
            throw new RuntimeException("Laser detector texture has not been set");
        }
        if (collider == null) {
            throw new IllegalStateException("Collider needs to be set on Laser Detector");
        }
        if (light == null) {
            throw new IllegalStateException("Cone light needs to be set on Laser Detector");
        }
        light.setActive(false);
        texture.setOrigin(0f, 0f);

        Body body = entity.getComponent(PhysicsComponent.class).getBody();
        float angle = (float) texture.getRotation();

        float w = entity.getScale().x;
        float h = entity.getScale().y;
        float offX = w / 2f;
        float offY = h / 2f;

        light.getLight().attachToBody(body, offX, offY);
        body.setTransform(body.getPosition(), angle * MathUtils.degreesToRadians);
        light.setDirectionDeg(angle);


        entity.getEvents().addListener("updateDetection", this::updateDetection);
    }

    private void updateDetection(boolean detecting) {
        if (detecting == this.detecting) return;

        // idk if this looks better with or without but its here if its wanted
        //texture.setTexture(textures[detecting ? 1 : 0]);
        light.setActive(detecting);
        entity.getEvents().trigger(detecting ? "detectingStart" : "detectingEnd");
        this.detecting = detecting;
    }
}

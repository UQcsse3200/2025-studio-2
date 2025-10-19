package com.csse3200.game.components.lasers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * This component is used on a laser detector entity and is responsible for initialising the
 * cone light position. It also updates the state of the detector when the entity gets
 * the event trigger from the {@code LaserShowerComponent}.
 */
public class LaserDetectorComponent extends Component {

    private PhysicsComponent physics;
    private TextureRenderComponent texture;
    private ConeLightComponent light;

    private boolean detecting = false;
    private boolean init = false;

    private Entity child;

    private static final String[] textures = {"images/laser-detector-off.png", "images/laser-detector-on.png"};

    public LaserDetectorComponent() {

    }

    public LaserDetectorComponent registerChild(Entity child) {
        this.child = child;
        return this;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        texture = entity.getComponent(TextureRenderComponent.class);
        light = entity.getComponent(ConeLightComponent.class);

        if (texture == null || light == null || physics == null) {
            throw new IllegalStateException("LaserDetectorComponent does not have the required components");
        }

        // init config: turn light off and set texture to rotate about (0,0) so the collider isn't offset
        texture.setOrigin(0f, 0f);

        Body body = physics.getBody();
        float angle = (float) texture.getRotation();

        // get the offset (center pos) of the entity
        float w = entity.getScale().x;
        float h = entity.getScale().y;
        float offX = w / 2f;
        float offY = h / 2f;

        // attach the light to the physics body in the center
        light.getLight().attachToBody(body, offX, offY);
        body.setTransform(body.getPosition(), angle * MathUtils.degreesToRadians);

        // setup child
        if (child != null) {
            ServiceLocator.getEntityService().register(child);
            child.setPosition(entity.getPosition());
            Body childBody = child.getComponent(PhysicsComponent.class).getBody();
            childBody.setTransform(body.getPosition(), angle * MathUtils.degreesToRadians);
        }

        entity.getEvents().addListener("updateDetection", this::updateDetection);
    }

    /**
     * Changes the state of the entity to represent if it is detecting the laser
     * or not. This method is only run by the event trigger {@code "updateDetection"}
     *
     * @param detecting
     */
    private void updateDetection(boolean detecting) {
        if (detecting == this.detecting) return;

        // idk if this looks better with or without but its here if its wanted
        //texture.setTexture(textures[detecting ? 1 : 0]);
        light.setActive(detecting);
        entity.getEvents().trigger(detecting ? "detectingStart" : "detectingEnd");
        this.detecting = detecting;
    }

    @Override
    public void update() {
        /*
        * for whatever reason it start crashing when setting the cone light in the create()
        * method. i know its because of the component creation order (the cone light has to be
        * created before the laser detector comp) but i just cant be bothered to debug it rn and
        * figure out how to change the component creation order.
        * */
        if (!init) {
            light.setActive(false);
            init = true;
        }
    }

    @Override
    public void dispose() {
        if (child != null) {
            child.dispose();
        }
    }
}

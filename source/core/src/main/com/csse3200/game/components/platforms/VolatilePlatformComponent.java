package com.csse3200.game.components.platforms;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A platform that disappears a few seconds after a player stands on it,
 * then respawns after a delay. Uses collider disabling and texture swapping
 * instead of scaling.
 */
public class VolatilePlatformComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(VolatilePlatformComponent.class);

    private final float lifetime; // seconds before platform disappears
    private final float respawnDelay;     // seconds before it comes back

    private GameTime timeSource;
    private boolean triggered = false;
    private long triggerTime;

    private boolean disappeared = false;
    private long disappearTime;

    private ColliderComponent collider;
    private TextureRenderComponent texture;

    private boolean linkedToPlate = false;

    private final String visibleTexture = "images/platform.png";
    private final String hiddenTexture = "images/empty.png";

    public VolatilePlatformComponent(float lifetime, float respawnDelay) {
        this.lifetime = lifetime;
        this.respawnDelay = respawnDelay;
    }

    @Override
    public void create() {
        timeSource = ServiceLocator.getTimeSource();
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);

        collider = entity.getComponent(ColliderComponent.class);
        texture = entity.getComponent(TextureRenderComponent.class);

        setVisible(false);

    }

    @Override
    public void update() {
        if(linkedToPlate) return;

        long now = timeSource.getTime();

        // Waiting to disappear
        if (triggered && !disappeared) {
            float elapsed = (now - triggerTime) / 1000f;
            entity.getEvents().trigger("platformBreak");
            texture.setTexture(hiddenTexture);
            if (elapsed >= lifetime) {
                disappear();
                entity.getEvents().trigger("platformBlank");
            }
        }

        // Waiting to respawn
        if (disappeared) {
            float elapsed = (now - disappearTime) / 1000f;
            if (elapsed >= respawnDelay) {
                respawn();
            }
        }
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        if (triggered || disappeared || linkedToPlate) return;

        if (PhysicsLayer.contains(PhysicsLayer.PLAYER, other.getFilterData().categoryBits)) {
            triggered = true;
            triggerTime = timeSource.getTime();
            logger.debug("Volatile platform triggered, will disappear in {}s", lifetime);
        }
    }

    private void disappear() {
        disappeared = true;
        disappearTime = timeSource.getTime();
        triggered = false;

        setVisible(false);

        logger.debug("Volatile platform disappeared, will respawn in {}s", respawnDelay);
    }

    private void respawn() {
        disappeared = false;
        setVisible(true);
        logger.debug("Volatile platform respawned");
    }

    public void linkToPlate(Entity plateEntity) {
        if(plateEntity == null) return;
        linkedToPlate = true;
        plateEntity.getEvents().addListener("platePressed", this::onPlatePressed);
        plateEntity.getEvents().addListener("plateReleased", this::onPlateReleased);
        logger.debug("Volatile platform linking to plate");
    }

    private void onPlatePressed() {
        logger.debug("Plate pressed -> platform visible");
        setVisible(true);
    }

    private void onPlateReleased() {
        logger.debug("Plate released -> platform hidden");
        setVisible(false);
    }

    private void setVisible(boolean visible) {
        if(collider != null) {
            collider.setSensor(!visible);
        }
        if(texture != null) {
            texture.setTexture(visible ? visibleTexture : hiddenTexture);
        }
    }

}

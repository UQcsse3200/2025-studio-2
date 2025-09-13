package com.csse3200.game.components.platforms;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
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
    }

    @Override
    public void update() {
        long now = timeSource.getTime();

        // Waiting to disappear
        if (triggered && !disappeared) {
            float elapsed = (now - triggerTime) / 1000f;
            if (elapsed >= lifetime) {
                disappear();
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
        if (triggered || disappeared) return;

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

        if (collider != null) {
//            collider.setSensor(true);
//            collider.getFixture().getBody().setAwake(true); // force recheck
            collider.getFixture().getBody().destroyFixture(collider.getFixture());
//            collider.setFixture(null); // clear reference in your component
        }

        if (texture != null) {
            texture.setTexture("images/Empty.png");
        }

        logger.debug("Volatile platform disappeared, will respawn in {}s", respawnDelay);
    }

    private void respawn() {
        disappeared = false;

        if (collider != null) {
//            collider.setSensor(false);
//            collider.getFixture().getBody().setAwake(true); // force recheck
            collider.create(); // or however your ColliderComponent builds the fixture
        }

        if (texture != null) {
            texture.setTexture("images/platform.png");
        }

        logger.debug("Volatile platform respawned");
    }

}

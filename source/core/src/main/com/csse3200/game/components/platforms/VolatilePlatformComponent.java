package com.csse3200.game.components.platforms;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A platform that disappears a few seconds after a player stands on it,
 * then respawns after a delay. Uses collider disabling and texture swapping
 * instead of scaling.
 *
 * The platform can also be linked to a pressure plate in which case it appears
 * when plate pressed, and disappears when released
 */
public class VolatilePlatformComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(VolatilePlatformComponent.class);

    private final float lifetime; // seconds before platform disappears
    private final float respawnDelay;     // seconds before it comes back

    private GameTime timeSource;
    private boolean triggered = false;
    private long triggerTime;

    private boolean disappeared = false;
    private long disappearTime = 0;
    private boolean platePressed = false;

    private ColliderComponent collider;
    private TextureRenderComponent texture;

    private boolean linkedToPlate = false; //whether platform controlled by pressure plate

    private final String visibleTexture = "images/platform.png";
    private final String hiddenTexture = "images/empty.png";
    private AnimationRenderComponent animator;
    private boolean breakStarted = false;

    /**
     * Creates a volatile platform with a lifetime and respawn delay
     *
     * @param lifetime time before platform disappears after being triggered (in seconds)
     * @param respawnDelay time before platform respawns after disappearing (in seconds)
     */
    public VolatilePlatformComponent(float lifetime, float respawnDelay) {
        this.lifetime = lifetime;
        this.respawnDelay = respawnDelay;
    }

    /**
     * Initialises volatile platform by getting subcomponents (time, collider and texture) and
     * sets platform to be initially not visible
     */
    @Override
    public void create() {
        timeSource = ServiceLocator.getTimeSource();
        collider = entity.getComponent(ColliderComponent.class);
        texture = entity.getComponent(TextureRenderComponent.class);
        animator = entity.getComponent(AnimationRenderComponent.class);

        if (!linkedToPlate) {
            setVisible(true);
            entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        }
    }

    /**
     * Updates the platform each frame, early return if it is linked to a pressure plate
     * otherwise handles if time has passed enough for platform to appear or disappear
     */
    @Override
    public void update() {
        long now = timeSource.getTime();

        if (linkedToPlate) {
            if (platePressed && disappeared) {
                if (collider != null) collider.create();
                if (texture != null) texture.setTexture(visibleTexture);
                disappeared = false;
            }

            if (!platePressed && !disappeared) {
                if (collider != null && collider.getFixture() != null) {
                    collider.getFixture().getBody().destroyFixture(collider.getFixture());
                }
                if (texture != null) texture.setTexture(hiddenTexture);
                disappeared = true;
            }
            return;
        }

        if (triggered && !disappeared) {
            float elapsed = (now - triggerTime) / 1000f;
            if (!breakStarted && animator != null) {
                animator.startAnimation("break");
                breakStarted = true;
            }
            if (elapsed >= lifetime) {
                disappear();
                triggered = false;
                breakStarted = false;
            }
        }

        if (disappeared) {
            float elapsed = (now - disappearTime) / 1000f;
            if (elapsed >= respawnDelay) {
                respawn();
            }
        }
    }

    /**
     * Handles collision events to trigger the platform disappearing
     *
     * @param me fixture of this platform
     * @param other fixture of the other entity colliding
     */
    public void onCollisionStart(Fixture me, Fixture other) {
        if(linkedToPlate) return;
        if (!disappeared && !triggered && PhysicsLayer.contains(PhysicsLayer.PLAYER, other.getFilterData().categoryBits)) {
            triggered = true;
            triggerTime = timeSource.getTime();
            breakStarted = true;

            if (texture != null) texture.setTexture(hiddenTexture);
            if (animator != null) animator.startAnimation("break");
        }
    }

    /**
     * Makes the platform disappear by disabling collisions and hiding the texture
     */
    private void disappear() {
        disappeared = true;
        disappearTime = timeSource.getTime();

        if (collider != null && collider.getFixture() != null) {
            collider.getFixture().getBody().destroyFixture(collider.getFixture());
        }

        if (texture != null) texture.setTexture(hiddenTexture);
        if (animator != null) animator.startAnimation("blank");
        logger.debug("Volatile platform disappeared, will respawn in {}s", respawnDelay);
    }

    /**
     * Respawns the platform by enabling collisions and showing the texture
     */
    private void respawn() {
        disappeared = false;
        breakStarted = false;

        if (collider != null) collider.create();
        if (texture != null) texture.setTexture(visibleTexture);

        logger.debug("Volatile platform respawned");
    }

    /**
     * Links this volatile platform to a pressure plate entity
     * When plate is pressed, platform becomes visible. When released, it becomes hidden (through
     * event listeners on plate press and release)
     *
     * @param plateEntity the pressure plate entity to link the platform to
     */
    public void linkToPlate(Entity plateEntity) {
        if(plateEntity == null) return;
        linkedToPlate = true;
        setVisible(false);
        plateEntity.getEvents().addListener("platePressed", this::onPlatePressed);
        plateEntity.getEvents().addListener("plateReleased", this::onPlateReleased);
        logger.debug("Volatile platform linking to plate");
    }

    /**
     * Makes the platform visible when linked pressure plate pressed
     */
    private void onPlatePressed() {
        logger.debug("Plate pressed -> platform visible");
        platePressed = true;
    }

    /**
     * Makes the platform hidden when the linked pressure plate is released
     */
    private void onPlateReleased() {
        logger.debug("Plate released -> platform hidden");
        platePressed = false;
    }

    /**
     * Set's the platforms visibility and collision state
     *
     * @param visible true to make platform visible and solid, false to be hidden and disable collisions
     */
    public void setVisible(boolean visible) {
        if(collider != null && collider.getFixture() != null) {
            collider.setSensor(!visible);
        }
        if(texture != null) {
            texture.setTexture(visible ? visibleTexture : hiddenTexture);
        }
    }

}

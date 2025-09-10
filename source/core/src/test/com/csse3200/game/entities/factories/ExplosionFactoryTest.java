package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.npc.ExplosionAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class ExplosionFactoryTest {

    private ResourceService rs;

    @BeforeEach
    void setUp() {
        // Register services
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerEntityService(new EntityService());
        rs = new ResourceService();
        ServiceLocator.registerResourceService(rs);

        // Load assets required by ExplosionFactory
        String[] textures = {"images/drone.atlas","images/SelfDestructionDrone.atlas"};
        rs.loadTextureAtlases(textures);
        rs.loadAll();
    }

    @AfterEach
    void cleanUp() {
        // Unload assets and clear services
        rs.unloadAssets(new String[]{"images/drone.atlas","images/SelfDestructionDrone.atlas"});
        rs.dispose();
        ServiceLocator.clear();
    }

    @Test
    void createExplosion_shouldHaveRequiredComponents() {
        Vector2 position = new Vector2(1f, 1f);
        float radius = 1.5f;

        Entity explosion = ExplosionFactory.createExplosion(position, radius);
        // Registering the entity calls the create() method on its components
        ServiceLocator.getEntityService().register(explosion);

        assertNotNull(explosion.getComponent(AnimationRenderComponent.class),
                "Explosion should have an AnimationRenderComponent");
        assertNotNull(explosion.getComponent(ExplosionAnimationController.class),
                "Explosion should have an ExplosionAnimationController");
    }

    @Test
    void createExplosion_shouldSetCorrectProperties() {
        Vector2 position = new Vector2(5f, 10f);
        float radius = 2f;

        float halfSize = radius / 2f;
        Vector2 bottomLeftPos = new Vector2(position.x - halfSize, position.y - halfSize);

        Entity explosion = ExplosionFactory.createExplosion(position, radius);
        ServiceLocator.getEntityService().register(explosion);

        // Check position is set correctly
        assertEquals(bottomLeftPos, ExplosionFactory.getExplosionPosition(position, radius),
                "Explosion position should match the provided position");

        // Check scale is calculated correctly from the radius
        float expectedScale = radius * 0.5f;
        assertEquals(new Vector2(expectedScale, expectedScale), explosion.getScale(),
                "Explosion scale should be based on radius");

        // Check that the correct animation is available
        AnimationRenderComponent animator = explosion.getComponent(AnimationRenderComponent.class);
        assertTrue(animator.hasAnimation("bomb_effect"),
                "Explosion animator should have 'bomb_effect' animation");
    }
}
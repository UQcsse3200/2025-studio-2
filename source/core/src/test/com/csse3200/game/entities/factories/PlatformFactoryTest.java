package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.platforms.VolatilePlatformComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TiledPlatformComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class PlatformFactoryTest {
    private ResourceService rs;
    @AfterEach
    void cleanUp() {
        rs.unloadAssets(new String[]{"images/volatile_platform.atlas"});
        rs.dispose();
        ServiceLocator.clear();
    }
    @BeforeEach
    void setupGameServices() {

        ServiceLocator.registerPhysicsService(new PhysicsService());
        ResourceService mockResourceService = mock(ResourceService.class);
        Texture mockTexture = mock(Texture.class);
        when(mockTexture.getHeight()).thenReturn(100);
        when(mockTexture.getWidth()).thenReturn(100);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(mockTexture);
        ServiceLocator.registerResourceService(mockResourceService);

        // Resource Service for loading atlases
        rs = new ResourceService();
        ServiceLocator.registerResourceService(rs);
        rs.loadTextureAtlases(new String[]{"images/volatile_platform.atlas"});
        rs.loadTextures(new String[]{"images/platform.png"});
        rs.loadTextures(new String[]{"images/empty.png"});
        rs.loadAll();
    }

    @Test
    void createStaticPlatform_hasAllComponents() {
        Entity staticPlatform = PlatformFactory.createStaticPlatform();

        assertNotNull(staticPlatform.getComponent(TiledPlatformComponent.class),
                "Static platform should have a TextureRendererComponent");
        assertNotNull(staticPlatform.getComponent(PhysicsComponent.class),
                "Static platform should have a PhysicsComponent");
        assertNotNull(staticPlatform.getComponent(ColliderComponent.class),
                "Static platform should have a Collider");
    }

    @Test
    void createStaticPlatform_isStatic() {
        Entity staticPlatform = PlatformFactory.createStaticPlatform();

        PhysicsComponent physics = staticPlatform.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Static Platform PhysicsComponent should have a static body type");
    }

    @Test
    void createVolatilePlatform_hasAllComponents() {
        Entity volatilePlatform = PlatformFactory.createVolatilePlatform(2,3);
        assertNotNull(volatilePlatform.getComponent(TextureRenderComponent.class),
                "Volatile platform should have a TextureRendererComponent");
        assertNotNull(volatilePlatform.getComponent(PhysicsComponent.class),
                "Volatile platform should have a PhysicsComponent");
        assertNotNull(volatilePlatform.getComponent(ColliderComponent.class),
                "Volatile platform should have a ColliderComponent");
        assertNotNull(volatilePlatform.getComponent(VolatilePlatformComponent.class),
                "Volatile platform should have a VolatilePlatformComponent");
        assertNotNull(volatilePlatform.getComponent(AnimationRenderComponent.class),
                "Volatile platform should have a AnimationRenderComponent");
    }

    @Test
    void createVolatilePlatform_isStatic() {
        Entity volatilePlatform = PlatformFactory.createVolatilePlatform(2,2);

        PhysicsComponent physics = volatilePlatform.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Volatile Platform PhysicsComponent should have a static body type");
    }

    @Test
    void createVolatilePlatform_isOnObstacleLayer() {
        Entity volatilePlatform = PlatformFactory.createVolatilePlatform(2,2);

        ColliderComponent collider = volatilePlatform.getComponent(ColliderComponent.class);
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer(),
                "Volatile Platform ColliderComponent should be on OBSTACLE layer");
    }
    @Test
    void createVolatilePlatform_disappears() {
        Entity volatilePlatform = PlatformFactory.createVolatilePlatform(5,5);

    }

    @Test
    void createPressurePlatePlatform_hasAllComponents() {
        Entity platePlatform = PlatformFactory.createPressurePlatePlatform();

        assertNotNull(platePlatform.getComponent(TextureRenderComponent.class),
                "Pressure plate platform should have TextureRenderComponent");
        assertNotNull(platePlatform.getComponent(PhysicsComponent.class),
                "Pressure plate platform should have PhysicsComponent");
        assertNotNull(platePlatform.getComponent(ColliderComponent.class),
                "Pressure plate platform should have ColliderComponent");
        assertNotNull(platePlatform.getComponent(VolatilePlatformComponent.class),
                "Pressure plate platform should have VolatilePlatformComponent");
    }

    @Test
    void createPressurePlatePlatform_isOnObstacleLayer() {
        Entity platePlatform = PlatformFactory.createPressurePlatePlatform();
        ColliderComponent collider = platePlatform.getComponent(ColliderComponent.class);
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer(),
                "Pressure plate platform should be on OBSTACLE layer");
    }

    @Test
    void createPressurePlatePlatform_isStatic() {
        Entity platePlatform = PlatformFactory.createPressurePlatePlatform();
        PhysicsComponent physics = platePlatform.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Pressure plate platform should have static body type");
    }
}

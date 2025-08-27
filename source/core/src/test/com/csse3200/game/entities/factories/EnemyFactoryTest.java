package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.BaseEntityConfig;
import com.csse3200.game.entities.configs.NPCConfigs;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
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
public class EnemyFactoryTest {
    private BaseEntityConfig droneConfig;
    private ResourceService rs;
    @BeforeEach
    void setUp() {
        NPCConfigs configs = FileLoader.readClass(NPCConfigs.class, "configs/NPCs.json");
        droneConfig = configs.drone;

        // Register services needed for entities
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerEntityService(new EntityService());
        // Resource Service for loading atlases
        rs = new ResourceService();
        ServiceLocator.registerResourceService(rs);
        rs.loadTextureAtlases(new String[]{"images/drone.atlas"});
        rs.loadAll();
    }

    @AfterEach
    void cleanUp() {
        rs.unloadAssets(new String[]{"images/drone.atlas"});
        rs.dispose();
    }

    @Test
    void createDrone_hasCombatStatsComponent() {
        Entity drone = EnemyFactory.createDrone(new Entity());
        ServiceLocator.getEntityService().register(drone);

        CombatStatsComponent stats = drone.getComponent(CombatStatsComponent.class);
        assertNotNull(stats, "Drone should have a CombatStatsComponent");
        assertEquals(droneConfig.health, stats.getHealth(), "Drone health mismatch");
        assertEquals(droneConfig.baseAttack, stats.getBaseAttack(), "Drone baseAttack mismatch");
    }

    @Test
    void createDrone_hasAnimations() {
        Entity drone = EnemyFactory.createDrone(new Entity());
        ServiceLocator.getEntityService().register(drone);

        // Future: Update for drone-specific animations
        AnimationRenderComponent anim = drone.getComponent(AnimationRenderComponent.class);
        assertNotNull(anim, "Drone should have an AnimationRenderComponent");
        assertTrue(anim.hasAnimation("float"), "Missing 'float' animation");
        assertTrue(anim.hasAnimation("angry_float"), "Missing 'angry_float' animation");
    }

    @Test
    void createDrone_returnDistinctEntities() {
        Entity a = EnemyFactory.createDrone(new Entity());
        Entity b = EnemyFactory.createDrone(new Entity());
        assertNotSame(a, b, "Factory should create a new instance each time");
    }

}

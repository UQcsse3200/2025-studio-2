package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.projectiles.BombComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class ProjectileFactoryTest {
    private ResourceService rs;
    private static final String[] bombTextures = {"images/bomb.png"};

    @BeforeEach
    void setUp() {
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerEntityService(new EntityService());
        // Register ResourceService for TextureRenderComponent to use
        rs = new ResourceService();
        ServiceLocator.registerResourceService(rs);


        // Load assets required by ExplosionFactory
        rs.loadTextures(bombTextures);
        rs.loadAll();
    }

    @AfterEach
    void cleanUp() {
        rs.unloadAssets(bombTextures);
        rs.dispose();
        ServiceLocator.clear();
    }

    @Test
    void createBomb_shouldHaveCorrectComponents() {
        Entity bomb = ProjectileFactory.createBomb(new Entity(), new Vector2(0, 10), new Vector2(5, 0), 1f, 2f, 10);

        assertNotNull(bomb.getComponent(PhysicsComponent.class),
                "Bomb should have a PhysicsComponent");
        assertNotNull(bomb.getComponent(ColliderComponent.class),
                "Bomb should have a ColliderComponent.class");
        assertEquals(PhysicsLayer.NPC, bomb.getComponent(ColliderComponent.class).getLayer(),
                "Bomb PhysicsLayer should be NPC");
        assertNotNull(bomb.getComponent(CombatStatsComponent.class),
                "Bomb should have a CombatStatsComponent");
        assertNotNull(bomb.getComponent(BombComponent.class),
                "Bomb should have a BombComponent");
        assertNotNull(bomb.getComponent(TextureRenderComponent.class),
                "Bomb should have a TextureRenderComponent");
    }

    @Test
    void createBomb_shouldHaveCorrectProperties() {
        Vector2 spawnPos = new Vector2(0f, 10f);
        int damage = 20;

        Entity bomb = ProjectileFactory.createBomb(new Entity(), spawnPos, new Vector2(5, 0), 1.5f, 2.5f, damage);

        Vector2 expectedBL = new Vector2(spawnPos.x - 0.25f, spawnPos.y - 0.25f);
        assertEquals(expectedBL.x, bomb.getPosition().x, 1e-6);
        assertEquals(expectedBL.y, bomb.getPosition().y, 1e-6);

        PhysicsComponent phys = bomb.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.DynamicBody, phys.getBody().getType());

        assertEquals(0f, phys.getBody().getLinearVelocity().x, 1e-6, "vx should start at 0");
        assertEquals(0f, phys.getBody().getLinearVelocity().y, 1e-6, "vy should start at 0");
        assertEquals(1f, phys.getBody().getGravityScale(), 1e-6, "gravityScale should be 1");

        assertEquals(PhysicsLayer.NPC, bomb.getComponent(ColliderComponent.class).getLayer());
        assertEquals(damage, bomb.getComponent(CombatStatsComponent.class).getBaseAttack());
    }

    @Test
    void createProjectile_shouldHaveCorrectComponents() {
        Entity projectile = ProjectileFactory.createProjectile(new Entity(), new Vector2(1, 0), 5f, 15);
        ServiceLocator.getEntityService().register(projectile);

        assertNotNull(projectile.getComponent(PhysicsComponent.class),
                "Projectile should have a PhysicsComponent");
        assertNotNull(projectile.getComponent(ColliderComponent.class),
                "Projectile should have a ColliderComponent.class");
        assertNotNull(projectile.getComponent(HitboxComponent.class),
                "Projectile should have a HitboxComponent");
        assertEquals(PhysicsLayer.NPC, projectile.getComponent(HitboxComponent.class).getLayer(),
                "Projectile PhysicsLayer should be NPC");
        assertNotNull(projectile.getComponent(CombatStatsComponent.class),
                "Projectile should have a CombatStatsComponent");
    }

    @Test
    void createProjectile_shouldHaveCorrectProperties() {
        Entity source = new Entity();
        source.setPosition(2f, 3f);
        ServiceLocator.getEntityService().register(source);

        Vector2 direction = new Vector2(0f, 1f);
        float speed = 7f;
        int damage = 25;

        Entity projectile = ProjectileFactory.createProjectile(source, direction, speed, damage);
        ServiceLocator.getEntityService().register(projectile);

        assertEquals(source.getCenterPosition(), projectile.getPosition());
        assertEquals(BodyDef.BodyType.DynamicBody, projectile.getComponent(PhysicsComponent.class).getBody().getType());
        assertEquals(PhysicsLayer.NPC, projectile.getComponent(HitboxComponent.class).getLayer());
        assertEquals(damage, projectile.getComponent(CombatStatsComponent.class).getBaseAttack());

        Vector2 expectedVelocity = direction.cpy().nor().scl(speed);
        assertEquals(expectedVelocity, projectile.getComponent(PhysicsComponent.class).getBody().getLinearVelocity());
    }
}
package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.AutonomousBoxComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class BoxFactoryTest {

    @BeforeEach
    void setupGameServices() {
        // Register PhysicsService to initialise a box's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);
    }

    @Test
    void createStaticBox_hasAllComponents() {
        Entity staticBox = BoxFactory.createStaticBox();
        assertNotNull(staticBox.getComponent(TextureRenderComponent.class),
                "Static Box should have a TextureRendererComponent");
        assertNotNull(staticBox.getComponent(PhysicsComponent.class),
                "Static Box should have a PhysicsComponent");
        assertNotNull(staticBox.getComponent(ColliderComponent.class),
                "Static Box should have a ColliderComponent");
    }

    @Test
    void createStaticBox_isStatic() {
        Entity staticBox = BoxFactory.createStaticBox();

        PhysicsComponent physics = staticBox.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Static Box PhysicsComponent should have a static body type");
    }

    @Test
    void createMoveableBox_hasAllComponents() {
        Entity moveableBox = BoxFactory.createMoveableBox();
        assertNotNull(moveableBox.getComponent(TextureRenderComponent.class),
                "Moveable Box should have a TextureRendererComponent");
        assertNotNull(moveableBox.getComponent(PhysicsComponent.class),
                "Moveable Box should have a PhysicsComponent");
        assertNotNull(moveableBox.getComponent(ColliderComponent.class),
                "Moveable Box should have a ColliderComponent");
    }

    @Test
    void createMoveableBox_isMoveable() {
        Entity moveableBox = BoxFactory.createMoveableBox();
        PhysicsComponent physics = moveableBox.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.DynamicBody, physics.getBody().getType(),
                "Moveable Box PhysicsComponent should have a dynamic body type");
    }

    @Test
    void autonomousBoxBuilder_hasAllComponents() {
        Entity autonomousBox = new BoxFactory.AutonomousBoxBuilder()
                .moveX(3f, 10f)
                .moveY(3f, 3f)
                .speed(2f)
                .damage(5)
                .knockback(2)
                .build();

        assertNotNull(autonomousBox.getComponent(TextureRenderComponent.class),
                "Autonomous Box should have a TextureRendererComponent");
        assertNotNull(autonomousBox.getComponent(PhysicsComponent.class),
                "Autonomous Box should have a PhysicsComponent");
        assertNotNull(autonomousBox.getComponent(ColliderComponent.class),
                "Autonomous Box should have a ColliderComponent");
        assertNotNull(autonomousBox.getComponent(HitboxComponent.class),
                "Autonomous Box should have a HitboxComponent");
        assertNotNull(autonomousBox.getComponent(CombatStatsComponent.class),
                "Autonomous Box should have a CombatStatsComponent");
        assertNotNull(autonomousBox.getComponent(TouchAttackComponent.class),
                "Autonomous Box should have a TouchAttackComponent");
        assertNotNull(autonomousBox.getComponent(AutonomousBoxComponent.class),
                "Autonomous Box should have an AutonomousBoxComponent");

        CombatStatsComponent combat = autonomousBox.getComponent(CombatStatsComponent.class);
        assertEquals(5, combat.getBaseAttack(),
                "CombatStatsComponent should have correct damage");

        TouchAttackComponent touch = autonomousBox.getComponent(TouchAttackComponent.class);
        assertEquals(
                2f, touch.getKnockbackForce(), 0.001f,
                "TouchAttackComponent should have correct knockback force applied");
    }

    @Test
    void autonomousBoxBuilder_isKinematic() {
        Entity autonomousBox = new BoxFactory.AutonomousBoxBuilder()
                .moveX(3f, 10f)
                .moveY(3f, 3f)
                .speed(2f)
                .build();
        PhysicsComponent physics = autonomousBox.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.KinematicBody, physics.getBody().getType(),
                "Autonomous Box PhysicsComponent should have a kinematic body type");
    }

    @Test
    void autonomousBoxBuilder_setsBoundsAndSpeed() {
        float minX = 3f;
        float maxX = 10f;
        float minY = 3f;
        float maxY = 3f;
        float speed = 2f;
        Entity autonomousBox = new BoxFactory.AutonomousBoxBuilder()
                .moveX(minX, maxX)
                .moveY(minY, maxY)
                .speed(speed)
                .build();
        AutonomousBoxComponent component = autonomousBox.getComponent(AutonomousBoxComponent.class);
        assertEquals(
                minX,
                component.getLeftX(),
                "Left bound should match value set in builder");
        assertEquals(
                maxX,
                component.getRightX(),
                "Right bound should match value set in builder");
        assertEquals(
                speed,
                component.getSpeed(),
                "Speed should match value set in builder");
    }

    @Test
    void autonomousBoxBuilder_setsTooltip() {
        String tooltipText = "Custom tooltip";
        TooltipSystem.TooltipStyle tooltipStyle = TooltipSystem.TooltipStyle.DEFAULT;

        Entity autonomousBox = new BoxFactory.AutonomousBoxBuilder()
                .tooltip(tooltipText, tooltipStyle)
                .build();

        TooltipSystem.TooltipComponent tooltip = autonomousBox.getComponent(TooltipSystem.TooltipComponent.class);
        assertNotNull(
                tooltip,
                "Autonomous Box should have a TooltipComponent");
        assertEquals(
                tooltipText,
                tooltip.getText(),
                "Tooltip text should match value set in builder");
        assertEquals(
                tooltipStyle,
                tooltip.getStyle(),
                "Tooltip style should match value set in builder");
    }
}

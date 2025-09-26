package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
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
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;

import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Provider;

import static org.junit.jupiter.api.Assertions.*;
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

        RenderService renderService = new RenderService();
        ServiceLocator.registerRenderService(renderService);
    }

    @Test
    void privateConstructor_throwsException() throws Exception {
        Constructor<BoxFactory> constructor = BoxFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException e = assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(UnsupportedOperationException.class, e.getCause(), "Constructor should throw UnsupportedOperationException");
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

        TooltipSystem.TooltipComponent tooltip
                = autonomousBox.getComponent(TooltipSystem.TooltipComponent.class);
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

    @Test
    void autonomousBoxBuilder_scaleSetsCorrectValues() {
        Entity autonomousBox = new BoxFactory.AutonomousBoxBuilder()
                .scale(2f, 3f)
                .build();

        float scaleX = autonomousBox.getScale().x;
        float scaleY = autonomousBox.getScale().y;

        assertEquals(2f, scaleX, 0.001f,
                "Scale X should match value set in builder");
        assertEquals(3f, scaleY, 0.001f,
                "Scale Y should match value set in builder");
    }

    @Test
    void autonomousBoxBuilder_getSpawnCoordinates() {
        float minX = 4f;
        float maxX = 7f;
        float minY = 7f;
        float maxY = 10f;
        BoxFactory.AutonomousBoxBuilder builder = new BoxFactory.AutonomousBoxBuilder().moveX(minX, maxX).moveY(minY, maxY);

        assertEquals((minX + maxX) / 2f, builder.getSpawnX(), 0.001f,
                "Spawn X should be midpoint of moveX bounds");
        assertEquals((minY + maxY) / 2f, builder.getSpawnY(), 0.001f,
                "Spawn Y should be midpoint of moveY bounds");
    }

    @Test
    void autonomousBoxBuilder_setsCustomTexture() {
        Texture dummyTexture = mock(Texture.class);
        ResourceService mockResourceService = ServiceLocator.getResourceService();
        when(mockResourceService.getAsset(
                eq("images/box_green.png"),
                eq(Texture.class))).thenReturn(dummyTexture);

        Entity autonomousBox = new BoxFactory.AutonomousBoxBuilder()
                .texture("images/box_green.png").build();

        TextureRenderComponent textureComponent
                = autonomousBox.getComponent(TextureRenderComponent.class);
        assertNotNull(textureComponent,
                "Autonomous Box should have a TextureRenderComponent");
        assertSame(dummyTexture, textureComponent.getTexture(),
                "TextureRenderComponent should have a loaded Texture");
    }

    @Test
    void autonomousBoxBuilder_addsAnimationRenderComponentForAtlas() {
        TextureAtlas mockAtlas = mock(TextureAtlas.class);
        // Add mock region to avoid null exception
        TextureAtlas.AtlasRegion mockRegion = mock(TextureAtlas.AtlasRegion.class);
        Array<TextureAtlas.AtlasRegion> regions = new Array<>();
        regions.add(mockRegion);
        when(mockAtlas.findRegions("flying_bat")).thenReturn(regions);

        ResourceService mockResourceService = ServiceLocator.getResourceService();
        when(mockResourceService.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(mockAtlas);

        Entity autonomousBox = new BoxFactory.AutonomousBoxBuilder().texture("flying_bat.atlas").build();

        AnimationRenderComponent animator = autonomousBox.getComponent(AnimationRenderComponent.class);
        assertNotNull(animator, "Autonomous Box should have an AnimationRenderComponent");

        assertEquals("flying_bat", animator.getCurrentAnimation(), "AnimationRenderComponent should start 'flying_bat', animation");
    }
}

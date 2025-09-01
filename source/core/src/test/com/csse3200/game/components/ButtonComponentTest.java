package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
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
import static org.mockito.Mockito.*;

public class ButtonComponentTest {
    private Entity button;
    private ButtonComponent buttonComponent;
    private Entity player;
    private ColliderComponent playerCollider;

    @BeforeEach
    void setup() {
        // Register PhysicsService to initialise a box's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so Texture assets and Renderer won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);
        RenderService mockRenderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(mockRenderService);

        //create button entity
        button = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ButtonComponent())
                .addComponent(new TextureRenderComponent("images/button.png"));

        buttonComponent = button.getComponent(ButtonComponent.class);
        buttonComponent.setType("standard");
        button.create();

        //create player with collider
        player = new Entity().addComponent(new PhysicsComponent());
        playerCollider = new ColliderComponent();
        player.addComponent(playerCollider);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void setPlayerInRange() {
        buttonComponent.setPlayerInRange(playerCollider);
        assertTrue(buttonComponent.isPlayerInRange());
        assertEquals(playerCollider, buttonComponent.getPlayerCollider());
    }

}
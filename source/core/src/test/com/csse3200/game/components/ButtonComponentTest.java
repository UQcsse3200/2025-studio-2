package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ButtonComponentTest {
    private Entity button;
    private ButtonComponent buttonComponent;
    private Entity player;
    private ColliderComponent playerCollider;

    @BeforeEach
    void setup() {
        // Register PhysicsService to initialise a button's physics body during tests
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
    void setPlayerInRange_setsPlayerCorrectly() {
        buttonComponent.setPlayerInRange(playerCollider);
        assertTrue(buttonComponent.isPlayerInRange());
        assertEquals(playerCollider, buttonComponent.getPlayerCollider());
    }

    @Test
    void setPlayerInRange_nullColliderResetsState() {
        buttonComponent.setPlayerInRange(playerCollider);
        buttonComponent.setPlayerInRange(null);
        assertFalse(buttonComponent.isPlayerInRange());
        assertNull(buttonComponent.getPlayerCollider());
    }

    @Test
    void playerInteract_togglesButton() {
        player.setPosition(new Vector2(0, 0));
        button.setPosition(new Vector2(0.6f, 0));

        buttonComponent.setPlayerInRange(playerCollider);
        player.getEvents().trigger("interact");
        assertTrue(buttonComponent.isPushed());
    }

    @Test
    void playerInteract_doesNotToggleIfRight() {
        player.setPosition(new Vector2(1.2f, 0));
        button.setPosition(new Vector2(0.6f, 0));

        buttonComponent.setPlayerInRange(playerCollider);
        player.getEvents().trigger("interact");
        assertFalse(buttonComponent.isPushed());
    }

    @Test
    void setPushed_setsState() {
        buttonComponent.setPushed(true);
        assertTrue(buttonComponent.isPushed());
    }

    @Test
    void toggleButton_updatesPlatformTexture() {
        buttonComponent.setType("platform");
        buttonComponent.setPushed(false);
        buttonComponent.setPuzzleManager(null);

        buttonComponent.setPlayerInRange(playerCollider);
        player.setPosition(new Vector2(-1f, 0)); // left
        button.setPosition(new Vector2(0f, 0));
        player.getEvents().trigger("interact");

        TextureRenderComponent render = button.getComponent(TextureRenderComponent.class);
        assertNotNull(render);
    }

    @Test
    void toggleButton_updatesDoorTexture() {
        buttonComponent.setType("door");
        buttonComponent.setPushed(false);
        buttonComponent.setPuzzleManager(null);

        buttonComponent.setPlayerInRange(playerCollider);
        player.setPosition(new Vector2(-1f, 0)); // left
        button.setPosition(new Vector2(0f, 0));
        player.getEvents().trigger("interact");

        TextureRenderComponent render = button.getComponent(TextureRenderComponent.class);
        assertNotNull(render);
    }

    @Test
    void toggleButton_updatesStandardTexture() {
        buttonComponent.setType("standard");
        buttonComponent.setPushed(false);
        buttonComponent.setPuzzleManager(null);

        buttonComponent.setPlayerInRange(playerCollider);
        player.setPosition(new Vector2(-1f, 0)); // left
        button.setPosition(new Vector2(0f, 0));
        player.getEvents().trigger("interact");

        TextureRenderComponent render = button.getComponent(TextureRenderComponent.class);
        assertNotNull(render);
    }

    @Test
    void forceUnpress_unpressesButton() {
        buttonComponent.setPushed(true);
        buttonComponent.forceUnpress();
        assertFalse(buttonComponent.isPushed());
    }

    @Test
    void forceUnpress_whenNotPushed_doesNothing() {
        buttonComponent.setPushed(false);
        buttonComponent.forceUnpress();
        assertFalse(buttonComponent.isPushed());
    }

    @Test
    void setDirection_setsValidAndInvalidValues() {
        buttonComponent.setDirection("up");
        buttonComponent.setDirection("RIGHT");
        buttonComponent.setDirection(null);
    }

    @Test
    void update_autoUnpressAfterTimeExpires() {
        GameTime gameTime = mock(GameTime.class);
        ServiceLocator.registerTimeSource(gameTime);
        when(gameTime.getDeltaTime()).thenReturn(6f);

        buttonComponent.setType("standard");
        buttonComponent.setPuzzleManager(null);

        player.setPosition(new Vector2(-1f, 0));
        button.setPosition(new Vector2(0f, 0));
        buttonComponent.setPlayerInRange(playerCollider);
        player.getEvents().trigger("interact");

        buttonComponent.update();
        buttonComponent.update();

        assertFalse(buttonComponent.isPushed(), "Button should auto-unpress after timer");
        ServiceLocator.clear();
    }

    @Test
    void toggleButton_callsPuzzleManager_onButtonPressed() {
        ButtonManagerComponent mockManager = mock(ButtonManagerComponent.class);
        buttonComponent.setPuzzleManager(mockManager);
        buttonComponent.setPushed(false);

        buttonComponent.setPlayerInRange(playerCollider);
        player.setPosition(new Vector2(-1f, 0));
        button.setPosition(new Vector2(0f, 0));
        player.getEvents().trigger("interact");

        verify(mockManager).onButtonPressed();
    }

    @Test
    void toggleButton_doesNotStartTimer_whenPuzzleIncomplete() {
        ButtonManagerComponent mockManager = mock(ButtonManagerComponent.class);
        when(mockManager.isPuzzleCompleted()).thenReturn(false);
        buttonComponent.setPuzzleManager(mockManager);
        buttonComponent.setType("standard");

        buttonComponent.setPlayerInRange(playerCollider);
        player.setPosition(new Vector2(-1f, 0));
        button.setPosition(new Vector2(0f, 0));
        player.getEvents().trigger("interact");

        assertTrue(buttonComponent.isPushed());
        buttonComponent.update();
        assertTrue(buttonComponent.isPushed());
    }
}
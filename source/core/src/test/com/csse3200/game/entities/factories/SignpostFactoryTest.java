package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class SignpostFactoryTest {
    private ResourceService mockResourceService;

    @BeforeEach
    void setup() {
        mockResourceService = mock(ResourceService.class);
        Texture mockTexture = mock(Texture.class);
        when(mockResourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mockTexture);
        ServiceLocator.registerResourceService(mockResourceService);
    }

    @AfterEach
    void cleanup() {
        ServiceLocator.clear();
    }

    @Test
    void createSignpost_upHasTextureRenderComponent() {
        Entity signpost = SignpostFactory.createSignpost("up");
        assertNotNull(signpost.getComponent(TextureRenderComponent.class),
                "Up signpost should have a TextureRenderComponent");
    }

    @Test
    void createSignpost_downHasTextureRenderComponent() {
        Entity signpost = SignpostFactory.createSignpost("down");
        assertNotNull(signpost.getComponent(TextureRenderComponent.class),
                "Down signpost should have a TextureRenderComponent");
    }

    @Test
    void createSignpost_leftHasTextureRenderComponent() {
        Entity signpost = SignpostFactory.createSignpost("left");
        assertNotNull(signpost.getComponent(TextureRenderComponent.class),
                "Left signpost should have a TextureRenderComponent");
    }

    @Test
    void createSignpost_rightHasTextureRenderComponent() {
        Entity signpost = SignpostFactory.createSignpost("right");
        assertNotNull(signpost.getComponent(TextureRenderComponent.class),
                "Right signpost should have a TextureRenderComponent");
    }

    @Test
    void createSignpost_invalidDirectionDefaultsToStandard() {
        Entity signpost = SignpostFactory.createSignpost("none");
        assertNotNull(signpost.getComponent(TextureRenderComponent.class),
                "Invalid direction signpost should still have a TextureRenderComponent");
    }

    @Test
    void createSignpost_textureRenderComponent_isLayerZero() {
        Entity signpost = SignpostFactory.createSignpost("up");
        TextureRenderComponent texture = signpost.getComponent(TextureRenderComponent.class);

        assertEquals(0, texture.getLayer(), "Signpost texture should be on layer 0");
    }
}

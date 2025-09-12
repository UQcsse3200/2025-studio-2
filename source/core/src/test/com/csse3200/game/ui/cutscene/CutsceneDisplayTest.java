package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.ui.cutscene.CutsceneReaderComponent.TextBox;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class CutsceneDisplayTest {
    // Mocked classes used inside of component
    @Mock
    private GameArea gameArea;
    @Mock
    private ResourceService resourceService;
    @Mock
    private RenderService renderService;
    @Mock
    private Stage stage;

    // Component being tested
    private CutsceneDisplay cutsceneDisplay;
    // Text boxes to be used in component
    private List<TextBox> textBoxes;
    // String for next area to send player to
    private final String nextArea = "next_level";


    /**
     * Runs before each test.
     * Mocks the UI skin to prevent exceptions when creating UI elements
     * Resource service is mocked to make a dummy texture for background
     * Also prepares the list of text boxes and sets up the UI component
     */
    @BeforeEach
    void setUp() {
        // Prevent null pointer exceptions
        MockitoAnnotations.openMocks(this);

        // Mock render service
        when(renderService.getStage()).thenReturn(stage);

        // Mock resources service
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));

        // Register mocked services
        ServiceLocator.registerResourceService(resourceService);
        ServiceLocator.registerRenderService(renderService);

        // Prepare test data
        textBoxes = List.of(
                new TextBox("First line.", "images/test1.png"),
                new TextBox("Second line.", "images/test2.png"),
                new TextBox("Third line.", null),
                new TextBox("Fourth line.", "images/test3.png")
        );

        // Set up component
        cutsceneDisplay = new CutsceneDisplay(textBoxes, gameArea, nextArea);
    }

    @Test
    void createBuildsUI() {
        cutsceneDisplay.create();

        // Verify first background got loaded
        verify(resourceService).getAsset(eq("images/test1.png"), eq(Texture.class));

        // Verify UI is added to stage as a stack actor
        ArgumentCaptor<Actor> captor = ArgumentCaptor.forClass(Actor.class);
        verify(stage).addActor(captor.capture());
        assertInstanceOf(Stack.class, captor.getValue());
    }
}

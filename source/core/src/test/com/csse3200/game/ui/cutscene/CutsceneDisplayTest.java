package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.ui.cutscene.CutsceneReaderComponent.TextBox;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
    @DisplayName("Stack object with UI actors is added to stage when component created")
    void createBuildsUI() {
        cutsceneDisplay.create();

        // Verify first background got loaded
        verify(resourceService).getAsset(eq("images/test1.png"), eq(Texture.class));

        // Verify UI is added to stage as a stack actor
        ArgumentCaptor<Actor> captor = ArgumentCaptor.forClass(Actor.class);
        verify(stage).addActor(captor.capture());
        assertInstanceOf(Stack.class, captor.getValue());
    }

    @Test
    @DisplayName("Clicking button progresses cutscene")
    void buttonClickProgressesCutscene() {
        cutsceneDisplay.create();

        // Capture the actor (stack) added to stage
        ArgumentCaptor<Actor> captor = ArgumentCaptor.forClass(Actor.class);
        verify(stage).addActor(captor.capture());
        Actor ui = captor.getValue();

        // Mimic frame update for UI components
        ui.act(0f);

        // Find button
        TextButton button = findButton(ui);
        // Check button exists
        assertNotNull(button);

        // Find ChangeListener on button and fire
        boolean listenerFired = false;
        for (EventListener listener : button.getListeners()) {
            if (listener instanceof ChangeListener) {
                listener.handle(new ChangeListener.ChangeEvent());
                listenerFired = true;
                break;
            }
        }
        assertTrue(listenerFired);


        // Verify that the background element changed after the button click
        InOrder inOrder = inOrder(resourceService);
        inOrder.verify(resourceService).getAsset(eq("images/test1.png"), eq(Texture.class));
        inOrder.verify(resourceService).getAsset(eq("images/test2.png"), eq(Texture.class));
    }

    @Test
    @DisplayName("Clicking button on last text box should finish cutscene")
    void buttonClickFinishesCutscene() {
        cutsceneDisplay.create();

        // Capture the actor (stack) added to stage
        ArgumentCaptor<Actor> captor = ArgumentCaptor.forClass(Actor.class);
        verify(stage).addActor(captor.capture());
        Actor ui = captor.getValue();

        // Mimic frame update
        ui.act(0f);

        // Find button
        TextButton button = findButton(ui);
        // Check button exists
        assertNotNull(button);

        // Click button enough times to get to the end of the cutscene
        for (int i = 0; i < textBoxes.size(); i++) {
            boolean listenerFired = false;
            for (EventListener listener : button.getListeners()) {
                if (listener instanceof ChangeListener) {
                    listener.handle(new ChangeListener.ChangeEvent());
                    listenerFired = true;
                    break;
                }
            }
            assertTrue(listenerFired);
        }

        // Verify the event for finishing the cutscene was triggered
        verify(gameArea).trigger("cutsceneFinished", nextArea, null);
    }

    /**
     * Helper method that recursively searches an actor hierarchy until a TextButton is found
     * @param actor The root actor to search from
     * @return The TextButton, or null if it could not be found
     */
    private TextButton findButton(Actor actor) {
        // Base case: Actor is TextButton
        if (actor instanceof TextButton) {
            return (TextButton) actor;
        }
        // Recursive case: Actor is not TextButton, search each child
        if (actor instanceof Group) {
            for (Actor child : ((Group) actor).getChildren()) {
                TextButton found = findButton(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}

package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.audio.Sound;
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
import org.junit.jupiter.api.Disabled;
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


    /**
     * Runs before each test.
     * Mocks the UI skin to prevent exceptions when creating UI elements
     * Resource service is mocked to make a dummy texture for background
     * Also prepares the list of text boxes and sets up the UI component
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(renderService.getStage()).thenReturn(stage);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
        when(resourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(mock(Sound.class));

        // Register mocked services
        ServiceLocator.registerResourceService(resourceService);
        ServiceLocator.registerRenderService(renderService);

        // 🔑 Register a mock EntityService so entity.dispose() won't NPE
        ServiceLocator.registerEntityService(mock(com.csse3200.game.entities.EntityService.class));

        textBoxes = List.of(
                new TextBox("First line.", "images/test1.png"),
                new TextBox("Second line.", "images/test2.png"),
                new TextBox("Third line.", null),
                new TextBox("Fourth line.", "images/test3.png")
        );

        cutsceneDisplay = new CutsceneDisplay(textBoxes, gameArea);

        com.csse3200.game.entities.Entity dummy = new com.csse3200.game.entities.Entity();
        dummy.addComponent(cutsceneDisplay);
        dummy.create();
    }

    @Test
    @DisplayName("Stack object with UI actors is added to stage when component created")
    void createBuildsUI() {
        cutsceneDisplay.create();

        // Capture all actors added to the stage
        ArgumentCaptor<Actor> captor = ArgumentCaptor.forClass(Actor.class);
        verify(stage, atLeastOnce()).addActor(captor.capture());

        // Ensure at least one of them is a Stack
        boolean hasStack = captor.getAllValues().stream().anyMatch(a -> a instanceof Stack);
        assertTrue(hasStack, "Expected a Stack to be added to the stage");
    }

    @Test
    @DisplayName("Clicking button progresses cutscene")
    void buttonClickProgressesCutscene() {
        cutsceneDisplay.create();

        // Capture all actors added to the stage
        ArgumentCaptor<Actor> captor = ArgumentCaptor.forClass(Actor.class);
        verify(stage, atLeastOnce()).addActor(captor.capture());

        // Find the Stack in the captured actors
        Actor ui = captor.getAllValues().stream()
                .filter(a -> a instanceof Stack)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No Stack found in stage actors"));

        // Mimic frame update
        ui.act(0f);

        // Find button
        TextButton button = findButton(ui);
        assertNotNull(button);

        // Fire ChangeListener
        boolean listenerFired = false;
        for (EventListener listener : button.getListeners()) {
            if (listener instanceof ChangeListener) {
                listener.handle(new ChangeListener.ChangeEvent());
                listenerFired = true;
                break;
            }
        }
        assertTrue(listenerFired);

        // Verify that both backgrounds were requested at least once
        verify(resourceService, atLeastOnce()).getAsset(eq("images/test1.png"), eq(Texture.class));
        verify(resourceService, atLeastOnce()).getAsset(eq("images/test2.png"), eq(Texture.class));
    }

    @Test
    @DisplayName("Clicking button on last text box should finish cutscene")
    void buttonClickFinishesCutscene() {
        cutsceneDisplay.create();

        // Capture all actors added to the stage
        ArgumentCaptor<Actor> captor = ArgumentCaptor.forClass(Actor.class);
        verify(stage, atLeastOnce()).addActor(captor.capture());

        // Find the Stack in the captured actors
        Actor ui = captor.getAllValues().stream()
                .filter(a -> a instanceof Stack)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No Stack found in stage actors"));

        // Mimic frame update
        ui.act(0f);

        // Find button
        TextButton button = findButton(ui);
        assertNotNull(button);

        // Click through all text boxes
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

        // Verify cutscene finished event triggered
        verify(gameArea).trigger("cutsceneFinished");
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

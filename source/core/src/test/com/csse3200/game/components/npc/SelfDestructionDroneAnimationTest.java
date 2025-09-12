package com.csse3200.game.components.npc;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class SelfDestructionDroneAnimationTest {
    private Entity makeEntityWithSSDDA(){
        Entity e = new Entity();
        e.addComponent(new SelfDestructionDroneAnimation());
        return e;
    }

    @Test
    void flyingStrat_PlaysFlyingAnimation(){
        Entity e = makeEntityWithSSDDA();
        AnimationRenderComponent renderComponent = mock(AnimationRenderComponent.class);
        e.addComponent(renderComponent);
        e.create();

        e.getEvents().trigger("flyingStart");
        verify(renderComponent,times(1)).startAnimation("flying");

    }

    @Test
    void selfDestructionDrone_PlaysSelfDestructionAnimation(){
        Entity e = makeEntityWithSSDDA();
        AnimationRenderComponent renderComponent = mock(AnimationRenderComponent.class);
        e.addComponent(renderComponent);
        e.create();
        e.getEvents().trigger("selfDestructStart");
        verify(renderComponent,times(1)).startAnimation("self_destruct");
    }
    @Test
    void sameEventDoesNotRestartAnimation(){
        Entity e = makeEntityWithSSDDA();
        AnimationRenderComponent renderComponent = mock(AnimationRenderComponent.class);
        e.addComponent(renderComponent);
        e.create();

        e.getEvents().trigger("flyingStart");
        e.getEvents().trigger("flyingStart");

        verify(renderComponent,times(1)).startAnimation("flying");
    }
    @Test
    void correctAnimationsCalledForSequence(){
        Entity e = makeEntityWithSSDDA();
        AnimationRenderComponent renderComponent = mock(AnimationRenderComponent.class);
        e.addComponent(renderComponent);
        e.create();
        e.getEvents().trigger("flyingStart");
        e.getEvents().trigger("selfDestructStart");
        verify(renderComponent,times(2)).startAnimation("flying");
        verify(renderComponent,times(2)).startAnimation("self_destruct");
    }
}

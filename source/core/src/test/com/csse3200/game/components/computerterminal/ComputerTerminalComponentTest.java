package com.csse3200.game.components.computerterminal;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.*;
import com.csse3200.game.services.ComputerTerminalService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ComputerTerminalComponentTest {

    private static class ProbeTerminalService extends ComputerTerminalService {
        public Entity lastOpen;
        @Override public void open(Entity terminalEntity) { this.lastOpen = terminalEntity; }
    }

    private ProbeTerminalService probe;

    @BeforeEach
    void setup() {
        probe = new ProbeTerminalService();
        ServiceLocator.registerComputerTerminalService(probe);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    private Entity makePlayerAt(float x, float y) {
        Entity player = new Entity();
        player.addComponent(new PhysicsComponent());
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.PLAYER);
        player.addComponent(collider);
        player.create();
        player.setPosition(new Vector2(x, y));
        return player;
    }

    private Entity makeTerminalAt(float x, float y) {
        Entity terminal = new Entity();
        terminal.addComponent(new PhysicsComponent());
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(true);
        terminal.addComponent(collider);
        terminal.addComponent(new ComputerTerminalComponent());
        terminal.create();
        terminal.setPosition(new Vector2(x, y));
        return terminal;
    }

    @Test
    void interactWithinRangeOpens() {
        Entity player   = makePlayerAt(0, 0);
        Entity terminal = makeTerminalAt(0.5f, 0.5f);

        // simulate contact established (listener normally sets this)
        ColliderComponent playerCol = player.getComponent(ColliderComponent.class);
        ComputerTerminalComponent comp = terminal.getComponent(ComputerTerminalComponent.class);
        comp.setPlayerInRange(playerCol);

        // trigger the player's "interact" event
        player.getEvents().trigger("interact");

        assertEquals(terminal, probe.lastOpen, "Service should open the same terminal entity");
    }

    @Test
    void interactOutOfRangeDoesNotOpen() {
        Entity player   = makePlayerAt(0, 0);
        Entity terminal = makeTerminalAt(3f, 3f);

        ColliderComponent playerCol = player.getComponent(ColliderComponent.class);
        ComputerTerminalComponent comp = terminal.getComponent(ComputerTerminalComponent.class);
        comp.setPlayerInRange(playerCol);

        player.getEvents().trigger("interact");

        assertNull(probe.lastOpen, "Too far away; should not open");
    }
}

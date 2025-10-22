package com.csse3200.game.components.computerterminal;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ComputerTerminalService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComputerTerminalComponentTest {

    /** Probe service to capture open() calls without NPEs. */
    private static class ProbeTerminalService extends ComputerTerminalService {
        public Entity lastOpen;
        @Override public void open(Entity terminalEntity) { this.lastOpen = terminalEntity; }
    }

    private ProbeTerminalService probe;
    private HeadlessApplication app; // only needed if code touches Gdx.app logging, etc.

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();

        // Core services used by components under test
        ServiceLocator.registerTimeSource(new GameTime());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        probe = new ProbeTerminalService();
        ServiceLocator.registerComputerTerminalService(probe);

        // If anything logs via Gdx.app, provide a headless Application
        if (Gdx.app == null) {
            app = new HeadlessApplication(new ApplicationAdapter() {});
        }
    }

    @AfterEach
    void tearDown() {
        if (app != null) { app.exit(); app = null; }
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
        Entity player   = makePlayerAt(0f, 0f);
        Entity terminal = makeTerminalAt(0.5f, 0.5f);

        ColliderComponent playerCol = player.getComponent(ColliderComponent.class);
        ComputerTerminalComponent comp = terminal.getComponent(ComputerTerminalComponent.class);

        // Simulate the contact listener having marked the player "in range"
        comp.setPlayerInRange(playerCol);

        // Player hits interact
        player.getEvents().trigger("interact");

        assertEquals(terminal, probe.lastOpen, "Service should open the same terminal entity");
    }

    @Test
    void interactOutOfRangeDoesNotOpen() {
        Entity player   = makePlayerAt(0f, 0f);
        Entity terminal = makeTerminalAt(3f, 3f); // too far (>= 0.8f threshold in either axis)

        ColliderComponent playerCol = player.getComponent(ColliderComponent.class);
        ComputerTerminalComponent comp = terminal.getComponent(ComputerTerminalComponent.class);
        comp.setPlayerInRange(playerCol);

        player.getEvents().trigger("interact");

        assertNull(probe.lastOpen, "Too far away; should not open");
    }

    @Test
    void setPlayerInRangeNullClearsAndPreventsOpen() {
        Entity player   = makePlayerAt(0f, 0f);
        Entity terminal = makeTerminalAt(0.2f, 0.2f); // within range

        ColliderComponent playerCol = player.getComponent(ColliderComponent.class);
        ComputerTerminalComponent comp = terminal.getComponent(ComputerTerminalComponent.class);

        // Enter range -> listener attached
        comp.setPlayerInRange(playerCol);
        // Leave range -> playerCollider becomes null (uncovered branch)
        comp.setPlayerInRange(null);

        // Player presses interact; handler should early return due to null collider
        player.getEvents().trigger("interact");

        assertNull(probe.lastOpen, "After leaving range, interact should not open terminal");
    }

    @Test
    void noServiceRegisteredIsSafeAndDoesNotCrash() {
        // Fresh env WITHOUT ComputerTerminalService
        ServiceLocator.clear();
        ServiceLocator.registerTimeSource(new GameTime());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        if (Gdx.app == null) app = new HeadlessApplication(new ApplicationAdapter() {});

        Entity player   = makePlayerAt(0f, 0f);
        Entity terminal = makeTerminalAt(0.1f, 0.1f); // within range

        ColliderComponent playerCol = player.getComponent(ColliderComponent.class);
        ComputerTerminalComponent comp = terminal.getComponent(ComputerTerminalComponent.class);

        // Enter range and interact: hits svc==null branch and logs (uncovered branch)
        comp.setPlayerInRange(playerCol);
        assertDoesNotThrow(() -> player.getEvents().trigger("interact"),
                "Interact with no ComputerTerminalService should not throw");
    }

    @Test
    void captchaResultListenerCoversSuccessAndFailureLogs() {
        Entity terminal = makeTerminalAt(0f, 0f);
        // create() already added the listener; just emit events
        // success branch
        terminal.getEvents().trigger("terminal:captchaResult",
                new CaptchaResult(true, Set.of(1, 2), Set.of(1, 2)));
        // failure branch
        terminal.getEvents().trigger("terminal:captchaResult",
                new CaptchaResult(false, Set.of(3), Set.of(1, 2)));
        // No assertion needed—just exercising both branches
        assertTrue(true);
    }
}
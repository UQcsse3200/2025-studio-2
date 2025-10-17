package com.csse3200.game.components.computerterminal;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ComputerTerminalService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComputerTerminalServiceTest {
    @Test
    void openCloseToggleWithoutUiEntityIsSafe() {
        ComputerTerminalService svc = new ComputerTerminalService();
        // Should not throw
        svc.open(new Entity());
        svc.close();
        svc.toggle(new Entity());
        svc.dispose();
    }

    @Test
    void registersAndFiresEvents() {
        ComputerTerminalService svc = new ComputerTerminalService();
        Entity ui = new Entity();
        final boolean[] openCalled = {false};
        final boolean[] closeCalled = {false};
        final boolean[] toggleCalled = {false};

        ui.getEvents().addListener("terminal:open", (Entity e) -> openCalled[0] = true);
        ui.getEvents().addListener("terminal:close", () -> closeCalled[0] = true);
        ui.getEvents().addListener("terminal:toggle", (Entity e) -> toggleCalled[0] = true);

        svc.registerUiEntity(ui);
        svc.open(new Entity());
        svc.close();
        svc.toggle(new Entity());

        assertTrue(openCalled[0]);
        assertTrue(closeCalled[0]);
        assertTrue(toggleCalled[0]);
    }
}
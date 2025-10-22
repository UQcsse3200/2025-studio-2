package com.csse3200.game.services;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputerTerminalServiceTest {
    @Test
    void openCloseToggleWithoutUiEntityIsSafe() {
        ComputerTerminalService svc = new ComputerTerminalService();
        try {
          svc.open(new Entity());
          svc.close();
          svc.toggle(new Entity());
          svc.dispose();
        } catch (Exception e) {
          assertNull(e); // unreachable
        }

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
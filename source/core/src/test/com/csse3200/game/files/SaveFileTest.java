package com.csse3200.game.files;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.entities.configs.SaveConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.CollectableService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(GameExtension.class)
public class SaveFileTest {
    private static final Logger logger = LoggerFactory.getLogger(FileLoaderTest.class);
    private GdxGame game;

    private InventoryComponent inv;
    private MockedStatic<CollectableService> svcMock;


    /** Thank you Jasmine for the config mockup and code */
    private static CollectablesConfig cfg(String id) {
        var c = new CollectablesConfig();
        c.id = id;
        c.sprite = "";
        c.effects = List.of();
        return c;
    }

    @BeforeEach
    void setup() {
        game = new GdxGame();
        inv = new InventoryComponent();
        svcMock = Mockito.mockStatic(CollectableService.class);
        svcMock.when(() -> CollectableService.get(anyString()))
                .thenAnswer(invocation -> cfg(invocation.getArgument(0)));
    }
    @AfterEach
    void tearDown() {
        if (svcMock != null) svcMock.close();
    }

    @Test
    void saveToFile() {
        MainGameScreen.Areas area = MainGameScreen.Areas.LEVEL_TWO;

        Entity player = new Entity();
        player.addComponent(inv);

        inv.addItems(InventoryComponent.Bag.INVENTORY, "test1", 3);
        inv.addItems(InventoryComponent.Bag.UPGRADES, "test2", 4);

        game.saveLevel(area, player, "test/files/testsave.json");

        SaveConfig result = FileLoader.readClass(SaveConfig.class, "test/files/testsave.json");

        assertNotNull(result);

        assertTrue(result.area == area);

        assertTrue(result.inventory.containsKey("test1"));
        assertEquals(result.inventory.get("test1"), inv.getInventory().get("test1"));

        assertTrue(result.upgrades.containsKey("test2"));
        assertEquals(result.upgrades.get("test2"), inv.getUpgrades().get("test2"));
    }

    @Test
    void loadFromFile() {
        SaveConfig result = game.loadSave("test/files/loadtest.json");

        assertNotNull(result);

        assertTrue(result.area == MainGameScreen.Areas.LEVEL_TWO);

        assertTrue(result.inventory.size() == 1);
        assertTrue(result.inventory.get("test") == 5);

        assertTrue(result.upgrades.size() == 2);
        assertTrue(result.upgrades.get("test2") == 6);
        assertTrue(result.upgrades.get("test3") == 7);
    }
}

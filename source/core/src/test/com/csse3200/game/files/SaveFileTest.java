package com.csse3200.game.files;

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
    void loadValidFromFile() {
        SaveConfig result = game.loadSave("test/files/validloadtest.json");

        assertNotNull(result);

        assertTrue(result.area == MainGameScreen.Areas.LEVEL_TWO);

        assertTrue(result.inventory.size() == 1);
        assertTrue(result.inventory.get("test") == 5);

        assertTrue(result.upgrades.size() == 2);
        assertTrue(result.upgrades.get("test2") == 6);
        assertTrue(result.upgrades.get("test3") == 7);
    }

    @Test
    void loadInvalidArea() {
        SaveConfig result = game.loadSave("test/files/invalidarealoadtest.json");

        assertNotNull(result);

        // Invalid names get set to LEVEL_ONE
        assertTrue(result.area == MainGameScreen.Areas.LEVEL_ONE);
    }

    @Test
    void loadInvalidInventory() {
        SaveConfig result = game.loadSave("test/files/noinventoryloadtest.json");

        assertNotNull(result);

        // Creates empty inventories while loading
        assertNotNull(result.inventory);
        assertNotNull(result.upgrades);

        assertEquals(0, result.inventory.size());
        assertEquals(0, result.upgrades.size());
    }

    @Test
    void loadEmptyFile() {
        SaveConfig result = game.loadSave("test/files/emptyfile.json");

        assertNotNull(result);

        assertTrue(result.area == MainGameScreen.Areas.LEVEL_ONE);
        assertNotNull(result.inventory);
        assertNotNull(result.upgrades);
    }

    @Test
    void loadNonExistentFile() {
        SaveConfig result = game.loadSave("test/files/hdsaljslhgkjhsagi.json");

        assertNotNull(result);

        assertTrue(result.area == MainGameScreen.Areas.LEVEL_ONE);
        assertNotNull(result.inventory);
        assertNotNull(result.upgrades);
    }
}

package com.csse3200.game.components.statspage;

import com.csse3200.game.achievements.AchievementId;
import com.csse3200.game.achievements.AchievementService;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.statisticspage.StatsTracker;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.screens.MainGameScreen;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class StatisticsTest {

    private MockedStatic<FileLoader> fileLoaderMock;
    private MainGameScreen mainGameScreen;

    private AchievementService service;
    private AchievementService.Listener mockListener;

    /**
     * setUp() was authored with assistance from ChatGPT-5
     * Date: 4/10/25
     */
    @BeforeEach
    void setUp() {
        fileLoaderMock = mockStatic(FileLoader.class);

        fileLoaderMock.when(() ->
                FileLoader.readClass(any(), anyString(), any())
        ).thenReturn(null);

        fileLoaderMock.when(() ->
                FileLoader.writeClass(any(), anyString(), any())
        ).thenAnswer(inv -> null);

        StatsTracker.resetSession();

        mainGameScreen = mock(MainGameScreen.class);
        GameArea levelOneArea = mock(GameArea.class);
        GameArea levelTwoArea = mock(GameArea.class);
        GameArea cutsceneOneArea = mock(GameArea.class);
        GameArea cutsceneTwoArea = mock(GameArea.class);

        when(mainGameScreen.getGameArea(MainGameScreen.Areas.LEVEL_ONE)).then(invocation -> {
            StatsTracker.completeLevel();
            return levelOneArea;
        });
        when(mainGameScreen.getGameArea(MainGameScreen.Areas.LEVEL_TWO)).then(invocation -> {
            StatsTracker.completeLevel();
            return levelTwoArea;
        });

        when(mainGameScreen.getGameArea(MainGameScreen.Areas.LEVEL_ONE_CUTSCENE)).thenReturn(cutsceneOneArea);
        when(mainGameScreen.getGameArea(MainGameScreen.Areas.LEVEL_TWO_CUTSCENE)).thenReturn(cutsceneTwoArea);

        service = AchievementService.get(); // singleton
        service.devReset(); // reset all unlocked achievements

        mockListener = mock(AchievementService.Listener.class);
        service.addListener(mockListener);
    }

    @AfterEach
    void tearDown() {
        fileLoaderMock.close();
    }

    @Test
    void testInitialValues() {
        StatsTracker.loadStats();
        assertEquals(0, StatsTracker.getLevelsCompleted());
        assertEquals(0, StatsTracker.getDeathCount());
        assertEquals(0, StatsTracker.getUpgradesCollected());
        assertEquals(0, StatsTracker.getAchievementsUnlocked());
    }

    @Test
    void testAddDeathIncrementsCorrectly() {
        Entity mockPlayer = mock(Entity.class);
        EventHandler mockEvents = mock(EventHandler.class);

        when(mockPlayer.getEvents()).thenReturn(mockEvents);

        CombatStatsComponent combat = new CombatStatsComponent(100, 0);
        mockPlayer.addComponent(combat);
        combat.setEntity(mockPlayer);

        combat.setHealth(0);
        assertEquals(1, StatsTracker.getDeathCount());

        combat.setHealth(50);
        combat.setHealth(0);
        assertEquals(2, StatsTracker.getDeathCount());
    }

    @Test
    void testCompleteLevel() {
        assertEquals(0, StatsTracker.getLevelsCompleted());

        mainGameScreen.getGameArea(MainGameScreen.Areas.LEVEL_ONE);
        assertEquals(1, StatsTracker.getLevelsCompleted());

        mainGameScreen.getGameArea(MainGameScreen.Areas.LEVEL_ONE_CUTSCENE);
        assertEquals(1, StatsTracker.getLevelsCompleted());

        mainGameScreen.getGameArea(MainGameScreen.Areas.LEVEL_TWO);
        assertEquals(2, StatsTracker.getLevelsCompleted());

        mainGameScreen.getGameArea(MainGameScreen.Areas.LEVEL_TWO_CUTSCENE);
        assertEquals(2, StatsTracker.getLevelsCompleted());
    }

//    @Test
//    void testOnCollectIncrementsUpgrades() {
//        Entity mockPlayer = mock(Entity.class);
//        InventoryComponent mockInventory = mock(InventoryComponent.class);
//
//        when(mockPlayer.getComponent(InventoryComponent.class)).thenReturn(mockInventory);
//        when(mockInventory.hasItem(InventoryComponent.Bag.UPGRADES, "test_upgrade"))
//                .thenReturn(false);
//        UpgradesComponent upgrades = new UpgradesComponent("test_upgrade");
//        boolean collected = upgrades.onCollect(mockPlayer);
//
//        assertTrue(collected);
//        assertEquals(1, StatsTracker.getUpgradesCollected());
//        verify(mockInventory).addItem(InventoryComponent.Bag.UPGRADES, "test_upgrade");
//    }

    /**
     * testUnlockedAchievements() was authored with assistance from ChatGPT-5
     * Date: 16/10/25
     */
    @Test
    void testUnlockedAchievements() {
        // Testing completed level 1 with no stamina depletion
        service.onLevelStarted();
        service.onLevelCompleted(1);

        assert(service.isUnlocked(AchievementId.LEVEL_1_COMPLETE));
        assert(service.isUnlocked(AchievementId.STAMINA_MASTER));

        verify(mockListener, times(1))
                .onUnlocked(eq(AchievementId.LEVEL_1_COMPLETE), anyString(), anyString());
        verify(mockListener, times(1))
                .onUnlocked(eq(AchievementId.STAMINA_MASTER), anyString(), anyString());

        assertEquals(2, StatsTracker.getAchievementsUnlocked());
    }

    @Test
    void testJumpCounter() {
        assertEquals(0, StatsTracker.getJumpCount());
        for (int i = 0; i < 5; i++) {
            StatsTracker.addJump();
        }
        assertEquals(5, StatsTracker.getJumpCount());
    }

    @Test
    void testCodexReads() {
        assertEquals(0, StatsTracker.getCodexReads());
        for (int i = 0; i < 5; i++) {
            StatsTracker.addCodex();
        }
        assertEquals(5, StatsTracker.getCodexReads());
    }

    @Test
    void testResetSessionClearsStats() {
        StatsTracker.addDeath();
        StatsTracker.completeLevel();
        StatsTracker.addUpgrade();
        StatsTracker.unlockAchievement();
        StatsTracker.resetSession();
        assertEquals(0, StatsTracker.getLevelsCompleted());
        assertEquals(0, StatsTracker.getDeathCount());
        assertEquals(0, StatsTracker.getUpgradesCollected());
        assertEquals(0, StatsTracker.getAchievementsUnlocked());
    }
}

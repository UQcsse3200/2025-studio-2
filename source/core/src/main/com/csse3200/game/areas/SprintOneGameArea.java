package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.ButtonManagerComponent;
import com.csse3200.game.components.enemy.ActivationComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.entities.factories.LadderFactory;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csse3200.game.achievements.AchievementProgression;

import java.util.ArrayList;
import java.util.List;

public class SprintOneGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(SprintOneGameArea.class);
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
    private static final float WALL_WIDTH = 0.1f;
    private static boolean keySpawned;

    private static final String[] gameTextures = {
            "images/button.png",
            "images/key.png",
            "images/button_pushed.png",
            "images/blue_button.png",
            "images/blue_button_pushed.png",
            "images/red_button.png",
            "images/red_button_pushed.png",
            "images/box_orange.png",
            "images/minimap_player_marker.png",
            "images/minimap_forest_area.png",
            "images/box_blue.png",
            "images/box_white.png",
            "images/blue_button.png",
            "images/spikes_sprite.png",
            "images/TechWallBase.png",
            "images/TechWallVariant1.png",
            "images/TechWallVariant2.png",
            "images/TechWallVariant3.png",
            "images/platform.png",
            "images/gate.png",
            "images/door_open.png",
            "images/door_closed.png",
            "images/Gate_open.png",
            "images/box_boy_leaf.png",
            "images/tree.png",
            "images/ghost_king.png",
            "images/ghost_1.png",
            "images/grass_1.png",
            "images/grass_2.png",
            "images/grass_3.png",
            "images/key_tester.png",
            "images/hex_grass_1.png",
            "images/hex_grass_2.png",
            "images/hex_grass_3.png",
            "images/iso_grass_1.png",
            "images/iso_grass_2.png",
            "images/iso_grass_3.png",
            "images/button.png",
            "images/button_pushed.png",
            "images/blue_button_pushed.png",
            "images/blue_button.png",
            "images/drone.png",
            "images/bomb.png",
            "images/camera-body.png",
            "images/camera-lens.png",
            "images/tile.png",
            "images/wall.png",
            "images/PLAYER.png",
            "images/ladder.png",
            "images/cube.png"
    };
    private static final String[] forestTextureAtlases = {
            "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images" +
            "/ghostKing.atlas", "images/drone.atlas", "images/PLAYER.atlas",
            "images/terrain_iso_grass.atlas",
            "images/ghost.atlas",
            "images/ghostKing.atlas",
            "images/drone.atlas",
            "images/doors.atlas",
            "images/flying_bat.atlas" // Bat sprites from https://todemann.itch.io/bat (see Wiki)
    };
    private static final String[] forestSounds = {"sounds/Impact4.ogg", "sounds" +
            "/chimesound.mp3", "sounds/hurt.mp3"};
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] forestMusic = {backgroundMusic};

    private final TerrainFactory terrainFactory;

    /**
     * Initialise this ForestGameArea to use the provided TerrainFactory.
     * @param terrainFactory TerrainFactory used to create the terrain for the GameArea.
     * @requires terrainFactory != null
     */
    public SprintOneGameArea(TerrainFactory terrainFactory) {
        super();
        this.terrainFactory = terrainFactory;
    }

    /**
     * Load terrain, UI, music. Must be done before spawning entities.
     * Assets are loaded separately.
     * Entities spawned separately.
     */
    protected void loadPrerequisites() {
        displayUI();

        spawnTerrain();
        //spawnTrees();

        createMinimap(
            ServiceLocator.getResourceService().getAsset("images/minimap_forest_area.png", Texture.class)
        );
        AchievementProgression.onLevelStart();

        playMusic();

        keySpawned = false;
    }

    /**
     * Load entities. Terrain must be loaded beforehand.
     * Player must be spawned beforehand if spawning enemies.
     */
    protected void loadEntities() {
        spawnPlatform();
        spawnElevatorPlatform();
        spawnWalls();
        spawnBoxes();
        playMusic();
        spawnLights();
        spawnButtons();
        spawnTraps();
        spawnPlatformBat();
        spawnLevelOneBatRoom();
//        spawnDrone();
        spawnPatrollingDrone();
//        spawnBomberDrone();
        spawnDoor();
        spawnLadder();
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Sprint one demo level"));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
    }

    private void spawnTraps() {
        GridPoint2 spawnPos =  new GridPoint2(2,5);
        Vector2 safeSpotPos = new Vector2(((spawnPos.x)/2)+2, ((spawnPos.y)/2)+2);
        Entity spikes = TrapFactory.createSpikes(safeSpotPos, 90f);
        spawnEntityAt(spikes, spawnPos, true,  true);
    }

    private void spawnDeathZone() {
        GridPoint2 spawnPos =  new GridPoint2(15,5);
        Entity deathZone = DeathZoneFactory.createDeathZone();
        spawnEntityAt(deathZone, spawnPos, true,  true);
    }

    private void spawnButtons() {
        Entity puzzleEntity = new Entity();
        ButtonManagerComponent manager = new ButtonManagerComponent();
        puzzleEntity.addComponent(manager);
        // Prevent leak
        this.spawnEntityAt(puzzleEntity, new GridPoint2(0, 0), true, true);

        Entity button2 = ButtonFactory.createButton(false, "door", "left");
        button2.addComponent(new TooltipSystem.TooltipComponent("Door Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(button2, new GridPoint2(39/2 ,9/2), true,  true);

        Entity button3 = ButtonFactory.createButton(false, "nothing", "left");
        spawnEntityAt(button3, new GridPoint2(29,8), true,  true);

        Entity button = ButtonFactory.createPuzzleButton(false, "nothing", "down", manager);
        spawnEntityAt(button, new GridPoint2(15,7), true,  true);

        Entity button4 = ButtonFactory.createPuzzleButton(false, "nothing", "up", manager);
        spawnEntityAt(button4, new GridPoint2(20,4), true,  true);

        Entity button5 = ButtonFactory.createPuzzleButton(false, "nothing", "up", manager);
        spawnEntityAt(button5, new GridPoint2(23,4), true,  true);

        //listener to spawn key when door button pushed
        button2.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            if (isPushed && !keySpawned) {
                spawnKey();
                keySpawned = true;
            }
        });

        puzzleEntity.getEvents().addListener("puzzleCompleted", () -> {
            //what to do when puzzle completed, probably player upgrade but depends
        });

    }

    public void spawnKey() {
        Entity key = CollectableFactory.createKey("door");
        spawnEntityAt(key, new GridPoint2(13,17), true, true);
    }

    private void spawnLights() {
        // see the LightFactory class for more details on spawning these
        Entity securityLight = SecurityCameraFactory.createSecurityCamera(player, LightingDefaults.ANGULAR_VEL, "1");
        spawnEntityAt(securityLight, new GridPoint2(20, 10), true, true);
    }
    private void spawnTerrain() {
        // Background terrain
        terrain = terrainFactory.createTerrain(TerrainType.SPRINT_ONE_ORTHO);
        spawnEntity(new Entity().addComponent(terrain));

        // Terrain walls
        float tileSize = terrain.getTileSize();
        GridPoint2 tileBounds = terrain.getMapBounds(0);
        Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

        // Left
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y), GridPoint2Utils.ZERO, false, false);
        // Right
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y),
                new GridPoint2(tileBounds.x, 0),
                false,
                false);
        // Top
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
                new GridPoint2(0, tileBounds.y),
                false,
                false);
        // Bottom
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), GridPoint2Utils.ZERO, false, false);
    }

    protected Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer(new ArrayList<>());
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        newPlayer.getEvents().addListener("reset", this::reset);
        return newPlayer;
    }

    protected Entity spawnPlayer(List<Component> componentList) {
        Entity newPlayer = PlayerFactory.createPlayer(componentList);
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        newPlayer.getEvents().addListener("reset", this::reset);
        return newPlayer;
    }

    //Platform spawn in testing
    private void spawnPlatform() {
    /*
    Creates floor and several steps to test jumping
    */
        GridPoint2 groundPos = new GridPoint2(0, 2);
        Entity ground = PlatformFactory.createStaticPlatform();
        ground.setScale(15,1);
        spawnEntityAt(ground, groundPos, false, false);

        GridPoint2 step1Pos = new GridPoint2(7,4);
        Entity step1 = PlatformFactory.createStaticPlatform();
        step1.setScale(2,1);
        spawnEntityAt(step1, step1Pos, false, false);

        float ts = terrain.getTileSize();
        GridPoint2 movingPos = new GridPoint2(21,2);
        Vector2 offsetWorld  = new Vector2(2.5f * ts, 8f);
        float speed = 2f;
        Entity movingPlatform = PlatformFactory.createMovingPlatform(offsetWorld, speed);
        movingPlatform.setScale(2,1f);
        spawnEntityAt(movingPlatform, movingPos, false, false);

        // Platform for patrolling drone
        GridPoint2 longPlatPos = new GridPoint2(3, 22);
        Entity longPlatform = PlatformFactory.createStaticPlatform();
        longPlatform.setScale(5, 0.25f);
        spawnEntityAt(longPlatform, longPlatPos, false, false);

    }

    private void spawnWalls() {
        float ts = terrain.getTileSize();

        // Tall wall on the left
        GridPoint2 wall1Pos = new GridPoint2(8, 22);
        Entity wall1 = WallFactory.createWall(
                0f, 0f,
                1f * ts, 5f * ts,
                "images/walls.png"
        );
        spawnEntityAt(wall1, wall1Pos, false, false);

        // Shorter wall in the middle
        GridPoint2 wall2Pos = new GridPoint2(8, 6);
        Entity wall2 = WallFactory.createWall(
                0f, 0f,
                1f * ts, 3f * ts,
                "images/tile.png"
        );
        spawnEntityAt(wall2, wall2Pos, false, false);

        // Another tall wall further right
        GridPoint2 wall3Pos = new GridPoint2(18, 4);
        Entity wall3 = WallFactory.createWall(
                0f, 0f,
                1f * ts, 6f * ts,
                "images/walls.png"
        );
        spawnEntityAt(wall3, wall3Pos, false, false);
    }

    private void spawnBoxes() {

        // Static box
        Entity staticBox = BoxFactory.createStaticBox();

        staticBox.addComponent(new TooltipSystem.TooltipComponent("Static Box\nThis box is fixed," +
                " you cannot push it!", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(staticBox, new GridPoint2(12,4), true,  true);

        // Moveable box
        Entity moveableBox = BoxFactory.createMoveableBox();
        moveableBox.addComponent(new TooltipSystem.TooltipComponent("Moveable Box\nYou can push this box around!",
                TooltipSystem.TooltipStyle.SUCCESS));
        spawnEntityAt(moveableBox, new GridPoint2(5,30), true,  true);
    }

    private void spawnPlatformBat() {
        BoxFactory.AutonomousBoxBuilder horizontalPlatformBuilder = new BoxFactory.AutonomousBoxBuilder();
        Entity horizontalPlatformBat = horizontalPlatformBuilder
                .moveX(1.5f, 6f).moveY(23f, 23f)
                .texture("images/flying_bat.atlas")
                .tooltip("Beware! Bats bite and knock you back. Stay clear!",
                        TooltipSystem.TooltipStyle.WARNING)
                .build();
        spawnEntityAt(horizontalPlatformBat, new GridPoint2(
                (int) horizontalPlatformBuilder.getSpawnX(), (int) horizontalPlatformBuilder.getSpawnY()), true, true);
    }

    private void spawnLevelOneBatRoom() {

        int offsetX = 0;
        int offsetY = 3;

        BoxFactory.AutonomousBoxBuilder batBuilder1 = new BoxFactory.AutonomousBoxBuilder();
        Entity lowHorizontalBat = batBuilder1
                .moveX(1f + offsetX, 5f + offsetX).moveY(4f + offsetY, 4f + offsetY)
                .texture("images/flying_bat.atlas")
                .speed(4f).build();
        spawnEntityAt(lowHorizontalBat, new GridPoint2(
                (int) batBuilder1.getSpawnX() + offsetX,
                (int) batBuilder1.getSpawnY() + offsetY),
                true, true);

        BoxFactory.AutonomousBoxBuilder batBuilder2 = new BoxFactory.AutonomousBoxBuilder();
        Entity highHorizontalBat2 = batBuilder2
                .moveX(1f + offsetX, 5f + offsetX).moveY(14f + offsetY, 14f + offsetY)
                .texture("images/flying_bat.atlas")
                .build();
        spawnEntityAt(highHorizontalBat2, new GridPoint2(
                (int) batBuilder2.getSpawnX() + offsetX,
                (int) batBuilder2.getSpawnY() + offsetY),
                true, true);

        BoxFactory.AutonomousBoxBuilder batBuilder3 = new BoxFactory.AutonomousBoxBuilder();
        Entity rightZigzagBat1 = batBuilder3
                .moveX(3f + offsetX, 5f + offsetX).moveY(4f + offsetY, 7f + offsetY)
                .texture("images/flying_bat.atlas")
                .speed(2f).build();
        spawnEntityAt(rightZigzagBat1, new GridPoint2(
                (int) batBuilder3.getSpawnX() + offsetX,
                (int) batBuilder3.getSpawnY() + offsetY),
                true, true);

        BoxFactory.AutonomousBoxBuilder batBuilder4 = new BoxFactory.AutonomousBoxBuilder();
        Entity leftZigzagBat2 = batBuilder4
                .moveX(1f + offsetX, 3f + offsetX).moveY(2f + offsetY, 7f + offsetY)
                .texture("images/flying_bat.atlas")
                .build();
        spawnEntityAt(leftZigzagBat2, new GridPoint2(
                        (int) batBuilder4.getSpawnX() + offsetX,
                        (int) batBuilder4.getSpawnY() + offsetY),
                true, true);
    }
    public void spawnDoor() {
        Entity door = ObstacleFactory.createDoor("door", this);
        door.setScale(1, 2);
        door.addComponent(new TooltipSystem.TooltipComponent("Unlock the door with the key", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(door, new GridPoint2(28,5), true, true);
    }

    private void spawnDrone() {
        GridPoint2 spawnTile = new GridPoint2(27, 25);
        Vector2 spawnWorldPos = terrain.tileToWorldPosition(spawnTile);

        Entity drone = EnemyFactory.createDrone(player, spawnWorldPos)
                        .addComponent(new ActivationComponent("1"));
        spawnEntityAt(drone, spawnTile, true, true);

    }

    private void spawnPatrollingDrone() {
        GridPoint2 spawnTile = new GridPoint2(3, 13);

        Vector2[] patrolRoute = {
                terrain.tileToWorldPosition(spawnTile),
                terrain.tileToWorldPosition(new GridPoint2(11, 13))
        };
        Entity patrolDrone = EnemyFactory.createPatrollingDrone(player, patrolRoute)
                        .addComponent(new ActivationComponent("1"));
        spawnEntityAt(patrolDrone, spawnTile, true, true);
    }

    private void spawnBomberDrone() {
        GridPoint2 spawnTile = new GridPoint2(3, 15);
        Vector2 spawnWorldPos = terrain.tileToWorldPosition(spawnTile);

        Entity bomberDrone = EnemyFactory.createBomberDrone(player, spawnWorldPos);
        spawnEntityAt(bomberDrone, spawnTile, true, true);
    }

    private void spawnElevatorPlatform() {
        float ts = terrain.getTileSize();

        // Elevator: moves up 3 tiles when triggered
        Entity elevator = PlatformFactory.createButtonTriggeredPlatform(
                new Vector2(0, 18.5f * ts),
                2f
        );
        GridPoint2 elevatorPos = new GridPoint2(13, 3);
        elevator.setScale(2,0.5f);
        spawnEntityAt(elevator, elevatorPos, false, false);
        logger.info("Elevator spawned at {}", elevatorPos);

        // Button with tooltip
        Entity button = ButtonFactory.createButton(false, "platform", "down");
        button.addComponent(new TooltipSystem.TooltipComponent(
                "Platform Button\nPress E to interact",
                TooltipSystem.TooltipStyle.DEFAULT
        ));
        GridPoint2 buttonPos = new GridPoint2(25/2,11/2);
        spawnEntityAt(button, buttonPos, true, true);
        logger.info("Elevator button spawned at {}", buttonPos);

        // Listen for toggle event from button
        button.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            if (isPushed) {
                logger.info("Button toggled ON — activating elevator");
                elevator.getEvents().trigger("activatePlatform");
            } else {
                logger.info("Button toggled OFF — stopping elevator");
                elevator.getEvents().trigger("deactivatePlatform");
            }
        });

        /*
        Entity button2 = ButtonFactory.createButton(false, "platform", "left");
        button2.addComponent(new TooltipSystem.TooltipComponent(
                "Return Button\nPress E to go down",
                TooltipSystem.TooltipStyle.DEFAULT
        ));

        GridPoint2 topButton = new GridPoint2(17, 21);
        spawnEntityAt(button2, topButton, true, true);
        logger.info("Top elevator button spawned at {}", topButton);

        button2.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            if (isPushed) {
                logger.info("Top button toggled ON — returning elevator down");
                elevator.getEvents().trigger("deactivatePlatform");
            }
        });
         */
    }

    private void spawnLadder() {
        int x = 8;
        int y = 8;
        int height = 13;
        for (int i = 0; i < height; i++) {
            GridPoint2 ladderPos = new GridPoint2(x, (y + i));
            Entity ladder = LadderFactory.createStaticLadder();
            ladder.setScale(1f, 1);
            spawnEntityAt(ladder, ladderPos, false, false);
        }
    }


    private void playMusic() {
        Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
        music.setLooping(true);
        music.setVolume(UserSettings.getMusicVolumeNormalized());
        music.play();
    }

    protected void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(gameTextures);
        resourceService.loadTextureAtlases(forestTextureAtlases);
        resourceService.loadSounds(forestSounds);
        resourceService.loadMusic(forestMusic);

        while (!resourceService.loadForMillis(10)) {
            // This could be upgraded to a loading screen
            logger.info("Loading... {}%", resourceService.getProgress());
        }
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(gameTextures);
        resourceService.unloadAssets(forestTextureAtlases);
        resourceService.unloadAssets(forestSounds);
        resourceService.unloadAssets(forestMusic);
    }

    @Override
    public void dispose() {
        super.dispose();
        ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
        this.unloadAssets();
    }
}

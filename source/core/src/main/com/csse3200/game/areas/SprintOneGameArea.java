package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.obstacles.DoorComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.physics.ObjectContactListener;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            "images/camera-lens.png"
    };
    private static final String[] forestTextureAtlases = {
            "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas", "images/drone.atlas"
    };
    private static final String[] forestSounds = {"sounds/Impact4.ogg", "sounds" +
            "/chimesound.mp3"};
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] forestMusic = {backgroundMusic};

    private final TerrainFactory terrainFactory;

    private Entity player;

    /**
     * Initialise this ForestGameArea to use the provided TerrainFactory.
     * @param terrainFactory TerrainFactory used to create the terrain for the GameArea.
     * @requires terrainFactory != null
     */
    public SprintOneGameArea(TerrainFactory terrainFactory) {
        super();
        this.terrainFactory = terrainFactory;
    }

    /** Create the game area, including terrain, static entities (trees), dynamic entities (player) */
    @Override
    public void create() {

        PhysicsEngine engine = ServiceLocator.getPhysicsService().getPhysics();
        engine.getWorld().setContactListener(new ObjectContactListener());
        keySpawned = false;
        loadAssets();

        spawnTerrain();
        createMinimap();
        player = spawnPlayer();
        player.getComponent(KeyboardPlayerInputComponent.class).setWalkDirection(Vector2.Zero.cpy());
        player.getEvents().addListener("reset", this::reset);



        spawnPlatform();
        spawnElevatorPlatform();

        spawnBoxes();
        playMusic();
        spawnLights();
        spawnButtons();
        spawnTraps();
        spawnDrone();
        spawnPatrollingDrone();
        spawnBomberDrone();
        spawnDoor();
        displayUI();

    }
    @Override
    public Entity getPlayer() {
        return player;
    }

    @Override
    protected void reset() {

    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Sprint one demo level"));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
    }
    private MinimapDisplay createMinimap() {
        Texture minimapTexture =
                ServiceLocator.getResourceService().getAsset("images/minimap_forest_area.png", Texture.class);

        MinimapDisplay.MinimapOptions options = new MinimapDisplay.MinimapOptions();
        options.position = MinimapDisplay.MinimapPosition.BOTTOM_RIGHT;

        float tileSize = terrain.getTileSize();
        Vector2 worldSize =
                new Vector2(terrain.getMapBounds(0).x * tileSize, terrain.getMapBounds(0).y * tileSize);
        ServiceLocator.registerMinimapService(new MinimapService(minimapTexture, worldSize, new Vector2()));

        MinimapDisplay minimapDisplay =
                new MinimapDisplay(150f, options);

        Entity minimapEntity = new Entity();
        minimapEntity.addComponent(minimapDisplay);
        spawnEntity(minimapEntity);

        return minimapDisplay;
    }
    private void spawnTraps() {
        GridPoint2 spawnPos =  new GridPoint2(2,4);
        Vector2 safeSpotPos = new Vector2(((spawnPos.x)/2)+2, ((spawnPos.y)/2)+2);
        Entity spikes = TrapFactory.createSpikes(spawnPos, safeSpotPos);
        spawnEntityAt(spikes, spawnPos, true,  true);
    }
    private void spawnButtons() {
        Entity button2 = ButtonFactory.createButton(false, "door", "left");
        button2.addComponent(new TooltipSystem.TooltipComponent("Door Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(button2, new GridPoint2(6,5), true,  true);

        Entity button3 = ButtonFactory.createButton(false, "nothing", "left");
        spawnEntityAt(button3, new GridPoint2(29,8), true,  true);

        //listener to spawn key when door button pushed
        button2.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            if (isPushed && !keySpawned) {
                spawnKey();
                keySpawned = true;
            }
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

    private Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer();
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
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

        // Autonomous box
        float startX = 3f;
        float endX = 10f;
        float y = 25f;
        float speed = 2f;

        Entity autonomousBox = BoxFactory.createAutonomousBox(startX, endX, speed);
        autonomousBox.addComponent(new TooltipSystem.TooltipComponent("Autonomous Box\nThis box has a fixed path" +
                " and you cannot push it!", TooltipSystem.TooltipStyle.SUCCESS));

        spawnEntityAt(autonomousBox, new GridPoint2((int)startX, (int)y), true, true);
    }
    public void spawnDoor() {
        Entity door = ObstacleFactory.createDoor("door", this, "cave");
        door.setScale(1, 2);
        door.addComponent(new TooltipSystem.TooltipComponent("Unlock the door with the key", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(door, new GridPoint2(28,5), true, true);
    }

    private void spawnDrone() {
        GridPoint2 spawnTile = new GridPoint2(27, 25);
        Vector2 spawnWorldPos = terrain.tileToWorldPosition(spawnTile);

        Entity drone = EnemyFactory.createDrone(player, spawnWorldPos); // pass world pos here
        spawnEntityAt(drone, spawnTile, true, true);

    }

    private void spawnPatrollingDrone() {
        GridPoint2 spawnTile = new GridPoint2(3, 22);

        Vector2[] patrolRoute = {
                terrain.tileToWorldPosition(spawnTile),
                terrain.tileToWorldPosition(new GridPoint2(7, 22)),
                terrain.tileToWorldPosition(new GridPoint2(11, 22))
        };
        Entity patrolDrone = EnemyFactory.createPatrollingDrone(player, patrolRoute);
        spawnEntityAt(patrolDrone, spawnTile, false, false); // Changed to false so patrol doesn't look weird
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
        Entity button = ButtonFactory.createButton(false, "platform", "left");
        button.addComponent(new TooltipSystem.TooltipComponent(
                "Platform Button\nPress E to interact",
                TooltipSystem.TooltipStyle.DEFAULT
        ));
        GridPoint2 buttonPos = new GridPoint2(17, 4);
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
    }


    private void playMusic() {
        Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
        music.setLooping(true);
        music.setVolume(UserSettings.getMusicVolumeNormalized());
        music.play();
    }

    private void loadAssets() {
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

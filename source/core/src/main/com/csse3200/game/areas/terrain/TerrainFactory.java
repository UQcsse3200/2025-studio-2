package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.HexagonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent.TerrainOrientation;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/** Factory for creating game terrains. */
public class TerrainFactory {
  private static final GridPoint2 MAP_SIZE = new GridPoint2(30, 30);
  private static final int TUFT_TILE_COUNT = 30;
  //Set to 0 as Wall variant image doesnt look good, so keeping as all base for now
  private static final int VARIANT_TILE_COUNT = 0;
  private static final int ROCK_TILE_COUNT = 30;

  private final OrthographicCamera camera;
  private final TerrainOrientation orientation;

  /**
   * Create a terrain factory with Orthogonal orientation
   *
   * @param cameraComponent Camera to render terrains to. Must be ortographic.
   */
  public TerrainFactory(CameraComponent cameraComponent) {
    this(cameraComponent, TerrainOrientation.ORTHOGONAL);
  }

  /**
   * Create a terrain factory
   *
   * @param cameraComponent Camera to render terrains to. Must be orthographic.
   * @param orientation orientation to render terrain at
   */
  public TerrainFactory(CameraComponent cameraComponent, TerrainOrientation orientation) {
    this.camera = (OrthographicCamera) cameraComponent.getCamera();
    this.orientation = orientation;
  }

  /**
   * Create a terrain of the given type, using the orientation of the factory. This can be extended
   * to add additional game terrains.
   *
   * @param terrainType Terrain to create
   * @return Terrain component which renders the terrain
   */
  public TerrainComponent createTerrain(TerrainType terrainType) {
    // Call the overloaded method with a default mapSize
    return createTerrain(terrainType, MAP_SIZE);
  }

  /**
   * Used to generate TerrainComponent within game areas
   * @param tileWorldSize
   * @param tiledMap
   * @param tilePixelSize
   * @return
   */
  public TerrainComponent createFromTileMap(float tileWorldSize, TiledMap tiledMap, GridPoint2 tilePixelSize) {
    TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / tilePixelSize.x);
    return new TerrainComponent(camera, tiledMap, renderer, orientation, tileWorldSize);
  }

  /**
   * Used to generate terrainComponent within a game area. This method is now not apart of the control flow for
   * generating terrains. Should you createFromTileMap() within the game areas
   * @param terrainType
   * @param mapSize
   * @return
   */
  public TerrainComponent createTerrain(TerrainType terrainType, GridPoint2 mapSize) {
    ResourceService resourceService = ServiceLocator.getResourceService();
    TextureRegion variant1, variant2, variant3, base;
    switch (terrainType) {
      case SPRINT_ONE_ORTHO:
        base =
                new TextureRegion(resourceService.getAsset("images/TechWallBase.png", Texture.class));
        variant1 =
                new TextureRegion(resourceService.getAsset("images/TechWallVariant1.png", Texture.class));
        variant2 =
                new TextureRegion(resourceService.getAsset("images/TechWallVariant2.png", Texture.class));
        variant3 =
                new TextureRegion(resourceService.getAsset("images/TechWallVariant3.png", Texture.class));
        return createSprintOneTerrain(0.5f, base, variant1, variant2, variant3);
      case CAVE_ORTHO:
        TextureRegion orthoCave =
                new TextureRegion(resourceService.getAsset("images/cave_1.png", Texture.class));
        TextureRegion orthoCaveRocks =
                new TextureRegion(resourceService.getAsset("images/cave_2.png", Texture.class));
        return createCaveTerrain(0.5f, orthoCave, orthoCaveRocks);
      case FOREST_DEMO:
        TextureRegion orthoGrass =
            new TextureRegion(resourceService.getAsset("images/grass_1.png", Texture.class));
        TextureRegion orthoTuft =
            new TextureRegion(resourceService.getAsset("images/grass_2.png", Texture.class));
        TextureRegion orthoRocks =
            new TextureRegion(resourceService.getAsset("images/grass_3.png", Texture.class));
        return createForestDemoTerrain(0.5f, orthoGrass, orthoTuft, orthoRocks);
      case FOREST_DEMO_ISO:
        TextureRegion isoGrass =
            new TextureRegion(resourceService.getAsset("images/iso_grass_1.png", Texture.class));
        TextureRegion isoTuft =
            new TextureRegion(resourceService.getAsset("images/iso_grass_2.png", Texture.class));
        TextureRegion isoRocks =
            new TextureRegion(resourceService.getAsset("images/iso_grass_3.png", Texture.class));
        return createForestDemoTerrain(1f, isoGrass, isoTuft, isoRocks);
      case FOREST_DEMO_HEX:
        TextureRegion hexGrass =
            new TextureRegion(resourceService.getAsset("images/hex_grass_1.png", Texture.class));
        TextureRegion hexTuft =
            new TextureRegion(resourceService.getAsset("images/hex_grass_2.png", Texture.class));
        TextureRegion hexRocks =
            new TextureRegion(resourceService.getAsset("images/hex_grass_3.png", Texture.class));
        return createForestDemoTerrain(1f, hexGrass, hexTuft, hexRocks);
      default:
        return null;
    }
  }

  private TerrainComponent createForestDemoTerrain(
      float tileWorldSize, TextureRegion grass, TextureRegion grassTuft, TextureRegion rocks) {
    GridPoint2 tilePixelSize = new GridPoint2(grass.getRegionWidth(), grass.getRegionHeight());
    TiledMap tiledMap = createForestDemoTiles(tilePixelSize, grass, grassTuft, rocks);
    TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / tilePixelSize.x);
    return new TerrainComponent(camera, tiledMap, renderer, orientation, tileWorldSize);
  }

  private TerrainComponent createCaveTerrain(
          float tileWorldSize, TextureRegion cave, TextureRegion rocks) {
    GridPoint2 tilePixelSize = new GridPoint2(cave.getRegionWidth(), cave.getRegionHeight());
    TiledMap tiledMap = createCaveTiles(tilePixelSize, cave, rocks);
    TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / tilePixelSize.x);
    return new TerrainComponent(camera, tiledMap, renderer, orientation, tileWorldSize);
  }
  private TerrainComponent createSprintOneTerrain(
          float tileWorldSize, TextureRegion baseTile,
          TextureRegion variant1,
          TextureRegion variant2,
          TextureRegion variant3) {
    GridPoint2 tilePixelSize = new GridPoint2(baseTile.getRegionWidth(), baseTile.getRegionHeight());
    TiledMap tiledMap = createSprintOneTiles(tilePixelSize, baseTile, variant1, variant2, variant3);
    TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / tilePixelSize.x);
    return new TerrainComponent(camera, tiledMap, renderer, orientation, tileWorldSize);
  }


  private TiledMapRenderer createRenderer(TiledMap tiledMap, float tileScale) {
    switch (orientation) {
      case ORTHOGONAL:
        return new OrthogonalTiledMapRenderer(tiledMap, tileScale);
      case ISOMETRIC:
        return new IsometricTiledMapRenderer(tiledMap, tileScale);
      case HEXAGONAL:
        return new HexagonalTiledMapRenderer(tiledMap, tileScale);
      default:
        return null;
    }
  }

  /**
   * Used to generate invisible TerrainComponent within game areas
   * @param tileWorldSize
   * @param tiledMap
   * @param tilePixelSize
   * @return
   */
  public TerrainComponent createInvisibleFromTileMap(float tileWorldSize, TiledMap tiledMap, GridPoint2 tilePixelSize) {
    TiledMapRenderer renderer = createRenderer(tiledMap, tileWorldSize / tilePixelSize.x);
    return new InvisibleTerrainComponent(camera, tiledMap, renderer, orientation, tileWorldSize);
  }

  /**
   * Create invisible terrain of the given type, using the orientation of the factory.
   * @param terrainType Terrain to create
   * @return Invisible terrain component which provides grid without rendering
   */
  public TerrainComponent createInvisibleTerrain(TerrainType terrainType) {
    return createInvisibleTerrain(terrainType, MAP_SIZE);
  }

  /**
   * Create invisible terrain with custom map size
   * @param terrainType
   * @param mapSize
   * @return
   */
  public TerrainComponent createInvisibleTerrain(TerrainType terrainType, GridPoint2 mapSize) {
    ResourceService resourceService = ServiceLocator.getResourceService();
    // Use empty/transparent texture for invisible terrain
    TextureRegion emptyTile = new TextureRegion(resourceService.getAsset("images/Empty.png", Texture.class));

    switch (terrainType) {
      case SPRINT_ONE_ORTHO:
      case CAVE_ORTHO:
      case FOREST_DEMO:
      case FOREST_DEMO_ISO:
      case FOREST_DEMO_HEX:
        GridPoint2 tilePixelSize = new GridPoint2(emptyTile.getRegionWidth(), emptyTile.getRegionHeight());
        TiledMap tiledMap = createDefaultTiles(tilePixelSize, emptyTile, emptyTile, emptyTile, emptyTile, mapSize);
        return createInvisibleFromTileMap(0.5f, tiledMap, tilePixelSize);
      default:
        return null;
    }
  }

  /**
   * Used to create/set ordering of background tiles with variants
   * @param tileSize
   * @param base
   * @param variant1
   * @param variant2
   * @param variant3
   * @param mapSize
   * @return
   */
  public TiledMap createDefaultTiles(
      GridPoint2 tileSize,
      TextureRegion base,
      TextureRegion variant1,
      TextureRegion variant2,
      TextureRegion variant3,
      GridPoint2 mapSize) {
    TiledMap tiledMap = new TiledMap();
    TerrainTile baseTile = new TerrainTile(base);
    TerrainTile variant1Tile = new TerrainTile(variant1);
    TerrainTile variant2Tile = new TerrainTile(variant2);
    TerrainTile variant3Tile = new TerrainTile(variant3);
    TiledMapTileLayer layer = new TiledMapTileLayer(mapSize.x, mapSize.y, tileSize.x, tileSize.y);

    // Create base grass
    fillTiles(layer, mapSize, baseTile);

    // Add some grass and rocks
    fillTilesAtRandom(layer, mapSize, variant1Tile, VARIANT_TILE_COUNT);
    fillTilesAtRandom(layer, mapSize, variant2Tile, VARIANT_TILE_COUNT);
    fillTilesAtRandom(layer, mapSize, variant3Tile, VARIANT_TILE_COUNT);

    tiledMap.getLayers().add(layer);
    return tiledMap;
  }

  private TiledMap createForestDemoTiles(
      GridPoint2 tileSize, TextureRegion grass, TextureRegion grassTuft, TextureRegion rocks) {
    TiledMap tiledMap = new TiledMap();
    TerrainTile grassTile = new TerrainTile(grass);
    TerrainTile grassTuftTile = new TerrainTile(grassTuft);
    TerrainTile rockTile = new TerrainTile(rocks);
    TiledMapTileLayer layer = new TiledMapTileLayer(MAP_SIZE.x, MAP_SIZE.y, tileSize.x, tileSize.y);

    // Create base grass
    fillTiles(layer, MAP_SIZE, grassTile);

    // Add some grass and rocks
    fillTilesAtRandom(layer, MAP_SIZE, grassTuftTile, TUFT_TILE_COUNT);
    fillTilesAtRandom(layer, MAP_SIZE, rockTile, ROCK_TILE_COUNT);

    tiledMap.getLayers().add(layer);
    return tiledMap;
  }

  private TiledMap createCaveTiles(
          GridPoint2 tileSize, TextureRegion cave, TextureRegion rocks) {
    TiledMap tiledMap = new TiledMap();
    TerrainTile grassTile = new TerrainTile(cave);
    TerrainTile rockTile = new TerrainTile(rocks);
    TiledMapTileLayer layer = new TiledMapTileLayer(MAP_SIZE.x, MAP_SIZE.y, tileSize.x, tileSize.y);

    // Create base grass
    fillTiles(layer, MAP_SIZE, grassTile);

    // Add some grass and rocks
    fillTilesAtRandom(layer, MAP_SIZE, rockTile, ROCK_TILE_COUNT);

    tiledMap.getLayers().add(layer);
    return tiledMap;
  }

  private TiledMap createSprintOneTiles(
          GridPoint2 tileSize,
          TextureRegion base,
          TextureRegion variant1,
          TextureRegion variant2,
          TextureRegion variant3) {
    TiledMap tiledMap = new TiledMap();
    TerrainTile baseTile = new TerrainTile(base);
    TerrainTile variant1Tile = new TerrainTile(variant1);
    TerrainTile variant2Tile = new TerrainTile(variant2);
    TerrainTile variant3Tile = new TerrainTile(variant3);
    TiledMapTileLayer layer = new TiledMapTileLayer(MAP_SIZE.x, MAP_SIZE.y, tileSize.x, tileSize.y);

    // Create base grass
    fillTiles(layer, MAP_SIZE, baseTile);

    // Add some grass and rocks
    fillTilesAtRandom(layer, MAP_SIZE, variant1Tile, VARIANT_TILE_COUNT);
    fillTilesAtRandom(layer, MAP_SIZE, variant2Tile, VARIANT_TILE_COUNT);
    fillTilesAtRandom(layer, MAP_SIZE, variant3Tile, VARIANT_TILE_COUNT);

    tiledMap.getLayers().add(layer);
    return tiledMap;
  }

  private static void fillTilesAtRandom(
      TiledMapTileLayer layer, GridPoint2 mapSize, TerrainTile tile, int amount) {
    GridPoint2 min = new GridPoint2(0, 0);
    GridPoint2 max = new GridPoint2(mapSize.x - 1, mapSize.y - 1);

    for (int i = 0; i < amount; i++) {
      GridPoint2 tilePos = RandomUtils.random(min, max);
      Cell cell = layer.getCell(tilePos.x, tilePos.y);
      cell.setTile(tile);
    }
  }

  private static void fillTiles(TiledMapTileLayer layer, GridPoint2 mapSize, TerrainTile tile) {
    for (int x = 0; x < mapSize.x; x++) {
      for (int y = 0; y < mapSize.y; y++) {
        Cell cell = new Cell();
        cell.setTile(tile);
        layer.setCell(x, y, cell);
      }
    }
  }

  /**
   * This enum should contain the different terrains in your game, e.g. forest, cave, home, all with
   * the same oerientation. But for demonstration purposes, the base code has the same level in 3
   * different orientations.
   */
  public enum TerrainType {
    FOREST_DEMO,
    FOREST_DEMO_ISO,
    FOREST_DEMO_HEX,
    CAVE_ORTHO,
    SPRINT_ONE_ORTHO
  }
}

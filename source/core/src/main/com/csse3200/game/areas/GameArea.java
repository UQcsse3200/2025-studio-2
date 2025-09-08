package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.events.EventHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an area in the game, such as a level, indoor area, etc. An area has a terrain and
 * other entities to spawn on that terrain.
 *
 * <p>Support for enabling/disabling game areas could be added by making this a Component instead.
 */
public abstract class GameArea implements Disposable {
  protected TerrainComponent terrain;
  protected List<Entity> areaEntities;

  private final EventHandler events = new EventHandler();

  public EventHandler getEvents() {
    return events;
  }

  public void trigger(String eventName, String keyId, Entity player) {
    events.trigger(eventName, keyId, player);
  }

  protected Entity player;


  protected GameArea() {
    areaEntities = new ArrayList<>();
  }

  /** Create the game area in the world. */
  public abstract void create();

  /** Reset current game area. */
  protected abstract void reset();


  /** Dispose of all internal entities in the area */
  public void dispose() {
    for (Entity entity : areaEntities) {
      // entity.dispose() does not delete the entity object itself.
      entity.dispose();
    }

    // Clear list of entities.
    areaEntities.clear();
  }

  public Entity getPlayer() {
    return player;
  }

  /**
   * Spawn entity at its current position
   *
   * @param entity Entity (not yet registered)
   */
  protected void spawnEntity(Entity entity) {
    areaEntities.add(entity);
    ServiceLocator.getEntityService().register(entity);
  }

  /**
   * Spawn entity on a given tile. Requires the terrain to be set first.
   *
   * @param entity Entity (not yet registered)
   * @param tilePos tile position to spawn at
   * @param centerX true to center entity X on the tile, false to align the bottom left corner
   * @param centerY true to center entity Y on the tile, false to align the bottom left corner
   */
  protected void spawnEntityAt(
      Entity entity, GridPoint2 tilePos, boolean centerX, boolean centerY) {
    Vector2 worldPos = terrain.tileToWorldPosition(tilePos);
    float tileSize = terrain.getTileSize();

    if (centerX) {
      worldPos.x += (tileSize / 2) - entity.getCenterPosition().x;
    }
    if (centerY) {
      worldPos.y += (tileSize / 2) - entity.getCenterPosition().y;
    }

    entity.setPosition(worldPos);
    spawnEntity(entity);
  }

  /**
   * Creates and adds the minimap display to the given game area.
   *
   * @param minimapTexture the texture to use as minimap background
   */
  protected void createMinimap(Texture minimapTexture) {
    float tileSize = terrain.getTileSize();
    GridPoint2 bounds = terrain.getMapBounds(0);
    Vector2 worldSize = new Vector2(bounds.x * tileSize, bounds.y * tileSize);

    MinimapService minimapService = new MinimapService(minimapTexture, worldSize, new Vector2());
    ServiceLocator.registerMinimapService(minimapService);

    MinimapDisplay.MinimapOptions options = new MinimapDisplay.MinimapOptions();
    options.position = MinimapDisplay.MinimapPosition.BOTTOM_RIGHT;
    MinimapDisplay minimapDisplay = new MinimapDisplay(150f, options);
    minimapService.setDisplay(minimapDisplay);

    Entity minimapEntity = new Entity();
    minimapEntity.addComponent(minimapDisplay);
    spawnEntity(minimapEntity);
  }
}

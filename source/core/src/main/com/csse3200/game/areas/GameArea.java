package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.collectables.effects.ItemEffectRegistry;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.physics.ObjectContactListener;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.CollectableService;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an area in the game, such as a level, indoor area, etc. An area has a terrain and
 * other entities to spawn on that terrain.
 *
 * <p>Support for enabling/disabling game areas could be added by making this a Component instead.
 */
public abstract class GameArea implements Disposable {
  private static final Logger logger = LoggerFactory.getLogger(GameArea.class);
  protected TerrainComponent terrain;
  protected GridPoint2 tileBounds;
  protected List<Entity> areaEntities;
  protected ArrayList<Vector2> deathLocations = new ArrayList<>();
  private Texture deathMarkerTexture = null;

  private final EventHandler events = new EventHandler();

  public EventHandler getEvents() {
    return events;
  }

  public GridPoint2 getMapBounds() {
    return terrain.getMapBounds(0);
  }

  public void trigger(String eventName) {
    events.trigger(eventName, player);
  }

  protected Entity player;

  protected boolean isResetting = false;

  // Components we want to keep in between levels, new list for every GameArea
  protected CombatStatsComponent combatStats; // health
  protected InventoryComponent inventory; // keys, upgrades, etc.

  protected GameArea() {
    areaEntities = new ArrayList<>();
    createDeathMarkerTexture();
  }

  private void createDeathMarkerTexture() {
    Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.RED);
    pixmap.drawLine(0, 0, 15, 15);
    pixmap.drawLine(15, 0, 0, 15);
    deathMarkerTexture = new Texture(pixmap);
    pixmap.dispose();
  }

  /**
   * Sets the texture to use for the death marker.
   *
   * @param texture the texture to use for the death marker.
   */
  protected void setDeathMarkerTexture(Texture texture) {
    deathMarkerTexture = texture;
  }

  /**
   * Records the death location of the player (does NOT copy the location).
   *
   * @param location the location of the player's death.
   */
  public void recordDeathLocation(Vector2 location) {
    deathLocations.add(location);
  }

  /**
   * Returns the list of death locations.
   *
   * @return the list of death locations.
   */
  public ArrayList<Vector2> getDeathLocations() {
    return deathLocations;
  }

  /**
   * Sets the list of death locations.
   * This must be called only once because it spawns entities that are not cleaned up if this is set again.
   *
   */
  public void setDeathLocations(ArrayList<Vector2> deathLocations) {
    assert(this.deathLocations.size() == 0);
    this.deathLocations = deathLocations;
  }

  /**
   * Spawns a death marker at the given location.
   *
   * @param location the location to spawn the death marker at.
   */
  protected void spawnDeathMarker(Vector2 location) {
    Entity marker = new Entity();
    marker.addComponent(new TextureRenderComponent(deathMarkerTexture).setLayer(0));
    marker.setPosition(location);
    marker.setScale(0.5f, 0.5f);
    spawnEntity(marker);
  }

  /**
   * Spawns a death marker at the death location.
   */
  public void spawnDeathMarkers() {
    for (Vector2 location : deathLocations) spawnDeathMarker(location);
  }

  /** Create the game area, including terrain, static entities (trees), dynamic entities (player) */
  public void create() {
    PhysicsEngine engine = ServiceLocator.getPhysicsService().getPhysics();
    engine.getWorld().setContactListener(new ObjectContactListener());
    CollectableService.load("configs/items.json");
    ItemEffectRegistry.registerDefaults();
    loadAssets();

    // Terrain must be loaded first in order to spawn entities
    loadPrerequisites();

    // player must be spawned before enemies as they require a player to target
    player = spawnPlayer();
    // Save this new player's components
    saveComponents(player.getComponent(CombatStatsComponent.class),
            player.getComponent(InventoryComponent.class));

    // load remaining entities
    loadEntities();
  }

  /**
   * Create the game area using components from a different player entity.
   * @param oldPlayer the older player entity
   */
  public void createWithPlayer(Entity oldPlayer) {
    PhysicsEngine engine = ServiceLocator.getPhysicsService().getPhysics();
    engine.getWorld().setContactListener(new ObjectContactListener());
    loadAssets();

    // Terrain must be loaded first in order to spawn entities
    loadPrerequisites();

    // Save the old player's combat stats and inventory
    saveComponents(oldPlayer.getComponent(CombatStatsComponent.class),
            oldPlayer.getComponent(InventoryComponent.class));

    // player must be spawned before enemies as they require a player to target
    player = spawnPlayer(getComponents());

    // load remaining entities
    loadEntities();
  }

  /**
   * Resets the game area
   */
  public void reset() {
    isResetting = true;
    final int oldEntityCount = ServiceLocator.getEntityService().getEntities().size;
    // Delete all entities within the room
    // Note: Using GameArea's dispose() instead of the specific area's as this does not unload assets (in theory).
    dispose();

    loadAssets(); // As we also dispose of animation components we have to reload assets
    loadPrerequisites();

    // Components such as health, upgrades and items we want to revert to how they were at
    // the start of the level. Copies are used in order to not lose the original components when
    // the original player is disposed.
    player = spawnPlayer(getComponents());
    createDeathMarkerTexture();
    spawnDeathMarkers();

    loadEntities();

    final int newEntityCount = ServiceLocator.getEntityService().getEntities().size;
    if (oldEntityCount != newEntityCount) {
      logger.error(
          "only {} entities should exist but {} entities exist now, {} Entities Leaked!!!",
          oldEntityCount, newEntityCount, newEntityCount - oldEntityCount
      );
    }

    // This is listened to by the MainGameScreen to show the death screen.
    this.trigger("reset");
    isResetting = false;
  }

  /**
   * Loads prerequisites for each area. Music, sounds, terrain etc
   */
  protected abstract void loadPrerequisites();

  /**
   * Loads all entities in a given area
   */
  protected abstract void loadEntities();

  /**
   * Spawns player
   * @return player entity
   */
  protected abstract Entity spawnPlayer();

  /**
   * Spawns player with previous components
   * @param componentList the list of components with witch to create the player
   * @return Player entity with old components
   */
  protected abstract Entity spawnPlayer(List<Component> componentList);

  /**
   * Loads assets
   */
  protected abstract void loadAssets();


  /**
   * Get copies all the player components we want to transfer in between resets/levels.
   * @return The list of all player components.
   */
  public List<Component> getComponents() {
    List<Component> resetComponents = new ArrayList<>();
    resetComponents.add(new InventoryComponent(inventory));
    return resetComponents;
  }

  /**
   * Save a copy of all the components we want to store for resets/level switches.
   * @param combatStats - CombatStatsComponent.
   * @param inventory - InventoryComponent.
   */
  public void saveComponents(CombatStatsComponent combatStats,
                                     InventoryComponent inventory) {
    this.inventory = new InventoryComponent(inventory);
  }


  /** Dispose of all internal entities in the area */
  public void dispose() {
    for (Entity entity : areaEntities) {
      // entity.dispose() does not remove the entity from the list of entities this area contains.
      entity.dispose();
    }

    // Clear list of entities.
    areaEntities.clear();

    if (deathMarkerTexture != null) {
      deathMarkerTexture.dispose();
      deathMarkerTexture = null;
    }
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
    MinimapDisplay minimapDisplay = new MinimapDisplay(200f, options);
    minimapService.setDisplay(minimapDisplay);

    Entity minimapEntity = new Entity();
    minimapEntity.addComponent(minimapDisplay);
    spawnEntity(minimapEntity);
  }
}

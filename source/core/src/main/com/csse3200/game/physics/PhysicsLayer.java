package com.csse3200.game.physics;

public class PhysicsLayer {
  public static final short NONE = 0;
  public static final short DEFAULT = (1 << 0);
  public static final short PLAYER = (1 << 1);
  // Terrain obstacle, e.g. trees
  public static final short OBSTACLE = (1 << 2);
  // NPC (Non-Playable Character) colliders
  public static final short NPC = (1 << 3);
  public static final short COLLECTABLE = (1 << 4);
  // Tooltip trigger zones and Tooltip colliders
  public static final short TOOLTIP = (1 << 5);
  public static final short LASER_REFLECTOR = (1 << 6);
  public static final short LASER_DETECTOR = (1 << 7);
  public static final short ALL = ~0;
  public static final short PROJECTILE = (1 << 7);
    public static final short WALL = (1 << 2);
    public static final short ENEMY = (1 << 3);
    public static final short TILE = (1 << 6);


  public static boolean contains(short filterBits, short layer) {
    return (filterBits & layer) != 0;
  }
    public static final short WALL_BITS = WALL | DEFAULT | PLAYER | ENEMY | NPC | OBSTACLE;
    public static final short ENEMY_BITS = ENEMY | DEFAULT | PLAYER | WALL | OBSTACLE | TILE | NPC;
    public static final short TILE_BITS = TILE | DEFAULT | PLAYER | ENEMY;
    // Allow projectiles (like lasers) to hit players and obstacles
    public static final short PROJECTILE_BITS = PROJECTILE | PLAYER | OBSTACLE;
    public static final short PLAYER_BITS = PLAYER | OBSTACLE | NPC | PROJECTILE | ENEMY;
  private PhysicsLayer() {
    throw new IllegalStateException("Instantiating static util class");
  }
}

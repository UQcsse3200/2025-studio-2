package com.csse3200.game.entities.configs;

import java.util.List;

/**
 * Configuration container for defining level properties and entities.
 * <p>
 * This class is populated from a JSON or configuration file
 * to describe the structure of a game level, including its name,
 * map size, player spawn point, minimap, walls, and entities.
 * </p>
 */
public class LevelConfig {
    public String name;
    public int[] mapSize;
    public int[] playerSpawn;
    public String miniMap;
    public Walls walls = new Walls();
    public List<E> entities = java.util.List.of();

    /**
     * Wall configuration data for a level.
     */
    public static class Walls {
        public float thickness = 0.1f;
    }

    /**
     * Configuration for an individual entity in a level.
     * <p>
     * Contains type, subtype, identifiers, relationships, positioning,
     * sizing, movement attributes, and flags for centering.
     * </p>
     */
    public static class E {
        /** Type of the entity (e.g., floor, platform, collectable) (required). */
        public String type;

        /** Subtype of the entity (e.g., ground, moving, potion) (if required). */
        public String subtype;

        /** Optional identifier for this entity (if required). */
        public String id;

        /** Linked entity id, used for relationships like triggers (if applicable).. */
        public String linked;

        /** Target property (e.g., health, door) (if required).. */
        public String target;

        /** Tooltip or description text for this entity (optional). */
        public String tooltip;

        /** Extra metadata for custom behavior (if applicable).. */
        public String extra;

        /** Direction of directional entities (e.g., up, left, etc.) (if applicable). */
        public String direction;

        /** X-coordinate position of the entity. */
        public int x = 0;

        /** Y-coordinate position of the entity. */
        public int y = 0;

        /** Scale factor along the x-axis (optional). */
        public Float sx = 1f;

        /** Scale factor along the y-axis (optional). */
        public Float sy = 1f;

        /** Movement speed of the entity (if applicable). */
        public Float speed = 0f;

        /** Movement range of the entity (if applicable). */
        public Float range = 0f;

        /** Rotation angle of the entity in degrees (if applicable). */
        public Float rotation = 0f;

        /** Delta movement along the x-axis (for moving entities). */
        public Float dx = 0f;

        /** Delta movement along the y-axis (for moving entities). */
        public Float dy = 0f;

        /** Safe x-coordinate for fallback or respawn. */
        public float safeX = 0;

        /** Safe y-coordinate for fallback or respawn. */
        public float safeY = 0;

        /** Whether the entity should be centered along the x-axis. (optional) */
        public Boolean centerX = true;

        /** Whether the entity should be centered along the y-axis. (optional) */
        public Boolean centerY = true;

        /** Initial collectable visibility */
        public Boolean isVisible = true;
    }
}

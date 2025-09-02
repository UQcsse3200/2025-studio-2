package com.csse3200.game.entities.configs;

/**
 * Defines properties for enemy types to be loaded in EnemyFactory.
 * Each field corresponds to a specific enemy (i.e. drone) and contains its base stats. Can be
 * extended to add properties unique to specific enemies.
 */
public class EnemyConfigs {
    /** Configuration for drone enemy type */
    public BaseEntityConfig drone = new BaseEntityConfig();
}
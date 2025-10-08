package com.csse3200.game.entities.configs;

/**
 * Defines properties for enemy types to be loaded in EnemyFactory.
 * Each field corresponds to a specific enemy (i.e. drone, bomber, patrollingDrone)
 * and contains its base stats. Can be extended to add properties unique to specific enemies.
 */
public class EnemyConfigs {
    /** Configuration for basic drone enemy type */
    public BaseEntityConfig drone = new BaseEntityConfig();

    /** Configuration for bomber drone enemy type */
    public BaseEntityConfig bomber = new BaseEntityConfig();

    /** Configuration for patrolling drone enemy type */
    public BaseEntityConfig patrollingDrone = new BaseEntityConfig();
}

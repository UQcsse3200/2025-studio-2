package com.csse3200.game.entities.configs;

/**
 * Defines the properties stored in player config files to be loaded by the Player Factory.
 */
public class PlayerConfig extends BaseEntityConfig  {
  public int gold = 1;
  public String favouriteColour = "none";
  public int maxStamina;
  public float staminaRegenPerSecond;
  public float sprintDrainPerSecond;
  public float staminaRegenDelaySeconds;
}

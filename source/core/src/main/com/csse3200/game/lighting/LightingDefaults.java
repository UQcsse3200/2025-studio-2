package com.csse3200.game.lighting;

import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.physics.PhysicsLayer;

public final class LightingDefaults {
    private LightingDefaults() {} // do not initialise

    // Lighting Engine Defaults
    public static final float AMBIENT_LIGHT = 1f;
    public static final int   BLUR_NUM      = 3;

    // Light Defaults
    public static final int   RAYS     = 128;
    public static final float DIST     = 5f;
    public static final float CONE_DEG = 35f;

    // Directions
    public static final float UP    =  90f;
    public static final float DOWN  = -90f;
    public static final float LEFT  =  180f;
    public static final float RIGHT =  0f;

    // Colours
    public static final Color NORMAL_COLOR   = new Color(230f/255f, 210f/255f, 140f/255f, 70f/100f);
    public static final Color DETECTED_COLOR = Color.RED;

    // Security Camera
    public static final short OCCLUDER = PhysicsLayer.OBSTACLE;

    // Panning Task
    public static final float START_DEG   = -135f;
    public static final float END_DEG     = -45f;
    public static final float ANGULAR_ACC =  40f;
    public static final float ANGULAR_VEL =  30f;

}

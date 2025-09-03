# Camera Movement Implementation

## Overview
This document describes the implementation of camera movement functionality that follows the player entity as it moves around the world. The camera only moves when the player is near the edge of the screen.

## Features Implemented

### 1. Camera Follows Player
- The camera automatically tracks the player entity's position
- Camera movement is smooth and responsive to player movement

### 2. Edge-Based Movement
- Camera only moves when the player approaches the screen edges
- Uses a "deadzone" system where the camera remains stationary when the player is in the center area
- Horizontal deadzone: 40% of screen width (DEADZONE_H_FRAC = 0.40f)
- Vertical deadzone: 35% of screen height (DEADZONE_V_FRAC = 0.35f)

### 3. Smooth Camera Movement
- Camera movement uses linear interpolation (LERP) for smooth transitions
- LERP factor: 0.15 (CAMERA_LERP = 0.15f) - provides smooth, not jerky movement

## Technical Implementation

### MainGameScreen.java Changes
1. **Added camera follow parameters:**
   ```java
   private static final float DEADZONE_H_FRAC = 0.40f; // Horizontal deadzone
   private static final float DEADZONE_V_FRAC = 0.35f; // Vertical deadzone
   private static final float CAMERA_LERP = 0.15f;      // Smoothing factor
   ```

2. **Added updateCameraFollow() method:**
   - Finds the player entity by searching for entities with PlayerActions component
   - Calculates deadzone boundaries based on current camera position
   - Only moves camera when player is outside the deadzone
   - Uses smooth interpolation for camera movement

3. **Integrated with render loop:**
   - Camera follow is updated every frame in the render method
   - Only updates when game is not paused

### ForestGameArea.java Changes
1. **Added getPlayer() method:**
   ```java
   public Entity getPlayer() {
       return player;
   }
   ```
   - Provides access to the player entity for the camera system

## How It Works

### Deadzone System
```
Screen Layout:
┌─────────────────────────────────────┐
│                                     │
│  ┌─────────────┐                   │
│  │             │                   │
│  │  Deadzone  │  ← Camera stays   │
│  │             │    stationary     │
│  │             │    here           │
│  └─────────────┘                   │
│                                     │
└─────────────────────────────────────┘
```

### Movement Logic
1. **Player in center (deadzone):** Camera doesn't move
2. **Player near left edge:** Camera moves left to keep player visible
3. **Player near right edge:** Camera moves right to keep player visible
4. **Player near top edge:** Camera moves up to keep player visible
5. **Player near bottom edge:** Camera moves down to keep player visible

### Smooth Movement
- Camera position is interpolated using LERP: `newPos = currentPos + (targetPos - currentPos) * LERP_FACTOR`
- LERP factor of 0.15 means camera moves 15% of the way to target each frame
- Results in smooth, natural camera movement

## Dependencies
- **PlayerActions component:** Must exist on the player entity
- **EntityService:** Used to find the player entity
- **Renderer:** Provides camera access and viewport dimensions

## Performance Considerations
- Camera follow logic runs every frame
- Entity search is O(n) where n is number of entities
- Could be optimized by caching player reference if needed

## Future Enhancements
1. **Camera bounds:** Limit camera movement to world boundaries
2. **Dynamic deadzone:** Adjust deadzone size based on player speed
3. **Camera shake:** Add effects for impacts or special events
4. **Multiple targets:** Support following multiple entities or switching targets

## Testing
To test the camera functionality:
1. Run the game
2. Move the player character around the world
3. Observe that camera only moves when player approaches screen edges
4. Verify smooth camera movement without jerky behavior
5. Check that camera follows player in all directions (up, down, left, right)

package com.csse3200.game.components.tooltip;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.ui.UIComponent;

/**
 * Complete tooltip system for displaying contextual information when players approach entities.
 * 
 * This system provides:
 * - TooltipStyle: Visual styles (DEFAULT, SUCCESS, WARNING)
 * - TooltipComponent: Detection component for entities that need tooltips
 * - TooltipDisplay: UI rendering component for showing tooltips
 * - TooltipManager: Global coordination between components
 * 
 * Usage:
 * 1. Add TooltipDisplay to your game area's UI entity
 * 2. Add TooltipComponent to any entity that needs a tooltip
 * 
 * Example:
 * // In game area UI setup:
 * ui.addComponent(new TooltipSystem.TooltipDisplay());
 * 
 * // On any entity:
 * entity.addComponent(new TooltipSystem.TooltipComponent("Press E to interact"));
 */
public class TooltipSystem {
    
    /**
     * Visual styles for tooltips using existing UI skin colors
     */
    public enum TooltipStyle {
        DEFAULT("button", "white"),    // Grey background, white text
        SUCCESS("button", "green"),    // Grey background, green text  
        WARNING("button", "red");      // Grey background, red text
        
        private final String backgroundDrawable;
        private final String textColor;
        
        TooltipStyle(String backgroundDrawable, String textColor) {
            this.backgroundDrawable = backgroundDrawable;
            this.textColor = textColor;
        }
        
        public String getBackgroundDrawable() { return backgroundDrawable; }
        public String getTextColor() { return textColor; }
    }
    
    /**
     * Global manager for coordinating between TooltipComponent and TooltipDisplay.
     * Uses static methods for simple global communication.
     */
    public static class TooltipManager {
        private static TooltipDisplay activeDisplay;
        
        /**
         * Register the active tooltip display component
         * @param display The TooltipDisplay component to register
         */
        public static void setActiveDisplay(TooltipDisplay display) {
            activeDisplay = display;
        }
        
        /**
         * Show a tooltip with the specified text and style
         * @param text The text to display
         * @param style The visual style to use
         */
        public static void showTooltip(String text, TooltipStyle style) {
            if (activeDisplay != null) {
                activeDisplay.showTooltip(text, style);
            }
        }
        
        /**
         * Hide the currently displayed tooltip
         */
        public static void hideTooltip() {
            if (activeDisplay != null) {
                activeDisplay.hideTooltip();
            }
        }
    }
    
    /**
     * Component that handles tooltip detection and triggering.
     * Add this component to any entity that should display a tooltip when the player approaches.
     * 
     * The component automatically:
     * - Creates a HitboxComponent if the entity doesn't have one
     * - Detects when the player enters/exits the trigger area
     * - Triggers global events that TooltipDisplay listens for
     * 
     * Usage examples:
     * - Basic: new TooltipComponent("Press E to interact")
     * - With style: new TooltipComponent("Dangerous!", TooltipStyle.WARNING)  
     * - With custom area: new TooltipComponent("Boss", TooltipStyle.WARNING, 4.0f, 3.0f)
     */
    public static class TooltipComponent extends Component {
        
        private final String text;
        private final TooltipStyle style;
        private final float widthMultiplier;
        private final float heightMultiplier;
        
        /**
         * Creates a tooltip with default style and auto-sized trigger area
         * @param text The text to display in the tooltip
         */
        public TooltipComponent(String text) {
            this(text, TooltipStyle.DEFAULT);
        }
        
        /**
         * Creates a tooltip with custom style and auto-sized trigger area
         * @param text The text to display in the tooltip
         * @param style The visual style for the tooltip
         */
        public TooltipComponent(String text, TooltipStyle style) {
            this(text, style, 1.5f, 1.5f);
        }
        
        /**
         * Creates a tooltip with custom style and trigger area size
         * @param text The text to display in the tooltip
         * @param style The visual style for the tooltip
         * @param widthMultiplier Multiplier for trigger area width (relative to entity)
         * @param heightMultiplier Multiplier for trigger area height (relative to entity)
         */
        public TooltipComponent(String text, TooltipStyle style, float widthMultiplier, float heightMultiplier) {
            this.text = text;
            this.style = style;
            this.widthMultiplier = widthMultiplier;
            this.heightMultiplier = heightMultiplier;
        }
        
        private Entity triggerZoneEntity; // Store reference to trigger zone
        
        @Override
        public void create() {
            super.create();
            createTriggerZoneEntity();
        }
        
        /**
         * Creates a separate invisible entity for tooltip detection with custom radius
         */
        private void createTriggerZoneEntity() {
            // Calculate trigger zone size
            Vector2 entityScale = entity.getScale();
            Vector2 triggerSize = new Vector2(
                entityScale.x * widthMultiplier,
                entityScale.y * heightMultiplier
            );
            
            // Create a new invisible entity for tooltip detection
            triggerZoneEntity = new Entity();
            // Don't set position yet - it will be updated in update() method
            triggerZoneEntity.setScale(triggerSize);
            
            // Add physics components for collision detection
            triggerZoneEntity.addComponent(new PhysicsComponent().setBodyType(com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody));
            HitboxComponent hitbox = new HitboxComponent();
            hitbox.setLayer(PhysicsLayer.OBSTACLE);
            triggerZoneEntity.addComponent(hitbox);
            
            // Add a component to handle the tooltip logic
            triggerZoneEntity.addComponent(new TriggerZoneComponent(text, style));
            
            // Try to spawn via entity service
            try {
                // Get the entity service and spawn the trigger zone
                var entityService = com.csse3200.game.services.ServiceLocator.getEntityService();
                if (entityService != null) {
                    entityService.register(triggerZoneEntity);
                }
            } catch (Exception e) {
                // Silent fail
            }
        }
        
        @Override
        public void update() {
            super.update();
            // Keep trigger zone positioned at the main entity's location
            if (triggerZoneEntity != null) {
                triggerZoneEntity.setPosition(entity.getPosition());
            }
        }
        
        /**
         * Gets the trigger zone entity for manual spawning if needed
         * @return the trigger zone entity, or null if not created
         */
        public Entity getTriggerZoneEntity() {
            return triggerZoneEntity;
        }
        
        @Override
        public void dispose() {
            super.dispose();
        }
    }
    
    /**
     * UI component for rendering tooltips in the bottom-left corner of the screen.
     * Add this component to your game area's UI entity to enable tooltip display.
     * 
     * Features:
     * - Fixed bottom-left positioning with consistent spacing
     * - Automatic text wrapping for long content
     * - Multiple visual styles (DEFAULT, SUCCESS, WARNING)
     * - Uses existing UI skin for consistent styling
     * 
     * The component automatically registers itself with TooltipManager and handles
     * all rendering and positioning logic.
     */
    public static class TooltipDisplay extends UIComponent {
        
        private Table tooltipTable;
        private Label tooltipLabel;
        private boolean isVisible = false;
        
        @Override
        public void create() {
            super.create();
            TooltipManager.setActiveDisplay(this);
        }
        
        /**
         * Shows a tooltip with the specified text and style
         * @param text The text to display (supports \n for line breaks)
         * @param style The visual style to use
         */
        public void showTooltip(String text, TooltipStyle style) {
            if (isVisible) {
                hideTooltip(); // Hide existing tooltip first
            }
            
            createTooltipUI(text, style);
            isVisible = true;
        }
        
        /**
         * Hides the currently displayed tooltip
         */
        public void hideTooltip() {
            if (tooltipTable != null) {
                tooltipTable.remove();
                tooltipTable = null;
                tooltipLabel = null;
            }
            isVisible = false;
        }
        
        /**
         * Creates the tooltip UI elements with proper styling and positioning
         * @param text The text to display
         * @param style The visual style to apply
         */
        private void createTooltipUI(String text, TooltipStyle style) {
            // Create table for layout
            tooltipTable = new Table(skin);
            tooltipTable.setBackground(skin.getDrawable(style.getBackgroundDrawable()));
            
            // Create label with text wrapping using the "default" label style
            tooltipLabel = new Label(text, skin, "default");
            tooltipLabel.setColor(skin.getColor(style.getTextColor()));
            tooltipLabel.setWrap(true);
            
            // Add label to table with padding
            tooltipTable.add(tooltipLabel).pad(10f).prefWidth(300f);
            
            // Add table to stage
            stage.addActor(tooltipTable);
            
            // Position will be set in draw() method
        }
        
        @Override
        public void draw(SpriteBatch batch) {
            if (isVisible && tooltipTable != null) {
                // Position tooltip in bottom-left corner with spacing
                float spacing = 20f;
                
                tooltipTable.setPosition(spacing, spacing);
                
                // Pack table to fit content
                tooltipTable.pack();
            }
        }
        
        @Override
        public void dispose() {
            hideTooltip();
            super.dispose();
        }
    }
    
    /**
     * Component for invisible trigger zone entities that handle tooltip detection
     */
    public static class TriggerZoneComponent extends Component {
        private final String text;
        private final TooltipStyle style;
        private boolean playerInRange = false;
        
        public TriggerZoneComponent(String text, TooltipStyle style) {
            this.text = text;
            this.style = style;
        }
        
        @Override
        public void create() {
            super.create();
            // Listen for collision events
            entity.getEvents().addListener("collisionStart", this::onCollisionStart);
            entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
        }
        
        @SuppressWarnings("unused") // Parameter required by event system
        private void onCollisionStart(Fixture me, Fixture other) {
            if (other == null || other.getBody() == null || other.getBody().getUserData() == null) {
                return;
            }
            
            BodyUserData userData = (BodyUserData) other.getBody().getUserData();
            Entity otherEntity = userData.entity;
            
            if (isPlayer(otherEntity) && !playerInRange) {
                playerInRange = true;
                TooltipManager.showTooltip(text, style);
            }
        }
        
        @SuppressWarnings("unused") // Parameter required by event system
        private void onCollisionEnd(Fixture me, Fixture other) {
            if (other == null || other.getBody() == null || other.getBody().getUserData() == null) {
                return;
            }
            
            BodyUserData userData = (BodyUserData) other.getBody().getUserData();
            Entity otherEntity = userData.entity;
            
            if (isPlayer(otherEntity) && playerInRange) {
                playerInRange = false;
                TooltipManager.hideTooltip();
            }
        }
        
        private boolean isPlayer(Entity entity) {
            return entity.getComponent(PlayerActions.class) != null;
        }
    }
}

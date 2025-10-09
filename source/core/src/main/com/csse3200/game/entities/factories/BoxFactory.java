 package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.AutonomousBoxComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.obstacles.MoveableBoxComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory class for creating box entities in the game.
 * <p>
 * Types of boxes that can be created include: <br>
 * -  white static (immovable) <br>
 * -  blue movable
 */
public class BoxFactory {

    /**
     * Private constructor to prevent instantiation of non-static box instances and remove gradle
     * warnings about missing constructor in this class.
     * @throws UnsupportedOperationException
     */
    private BoxFactory() {
        throw new UnsupportedOperationException("Cannot instantiate BoxFactory");
    }

    /**
     * Creates a static (immovable) box entity.
     * <p>
     * The box currently displays as a white square, scaled to half a game unit.
     * <p>
     * Its static body type makes it immovable.  Other physical game objects can collide with it,
     * but it does not move, react to collisions and cannot be destroyed.
     * @return A new static box Entity
     */
    public static Entity createStaticBox() {

        Entity staticBox = new Entity()
                .addComponent(new TextureRenderComponent("images/box_white.png"))
                .addComponent(new PhysicsComponent()
                        .setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        // Scaled to half a world unit
        staticBox.setScale(0.5f, 0.5f);
        return staticBox;
    }

    /**
     * Creates a dynamic (moveable) box entity.
     * <p>
     * The box currently displays as a blue square, scaled to half a game unit.
     * <p>
     * Its dynamic body type makes it moveable.  The player can automatically push the box with
     * its body, or interact with the box to lift it or drop it.  The player can move with the
     * box whilst it is lifted.
     * @return A new moveable box Entity
     */
    public static Entity createMoveableBox() {
        RenderService rs = ServiceLocator.getRenderService();
        Camera camera = rs.getRenderer() == null ? null : rs.getRenderer().getCamera().getCamera();
        Entity moveableBox = new Entity()
                .addComponent(new PhysicsComponent()
                        .setBodyType(BodyDef.BodyType.DynamicBody))
                .addComponent(new ColliderComponent()
                        .setLayer(PhysicsLayer.OBSTACLE)
                        .setDensity(1f)
                        .setRestitution(0.1f)
                        .setFriction(0.8f))
                .addComponent(new MoveableBoxComponent().setCamera(camera))
                .addComponent(new TextureRenderComponent("images/cube.png"));

        moveableBox.setScale(0.5f, 0.5f);
        return moveableBox;
    }

    /**
     * Creates a dynamic box which has a stronger gravity and can press down pressure plates
     *
     * @return a new moveable weighted box entity
     */
    public static Entity createWeightedBox() {
        Entity weightedBox = createMoveableBox();
        weightedBox.getComponent(TextureRenderComponent.class).setTexture("images/heavy-cube.png");
        weightedBox.getComponent(MoveableBoxComponent.class).setBaseGravityScale(0.85f);

        return weightedBox;
    }

    /**
     * Creates a dynamic box which has the ability to reflect laser beams.
     *
     * @return a new moveable reflector box
     */
    public static Entity createReflectorBox() {
        Entity reflectorBox = createMoveableBox();

        reflectorBox.getComponent(TextureRenderComponent.class).setTexture("images/mirror-cube-off.png");
        ConeLightComponent light = new ConeLightComponent(
                ServiceLocator.getLightingService().getEngine().getRayHandler(),
                LightingDefaults.RAYS,
                Color.RED,
                1f,
                0f,
                180f
        ).setFollowEntity(false);
        reflectorBox.addComponent(light);

        reflectorBox.getComponent(MoveableBoxComponent.class).setPhysicsLayer(PhysicsLayer.LASER_REFLECTOR);

        return reflectorBox;
    }

    /**
     * Builder that creates autonomous (kinematic) box entities that can be used as moving game
     * obstacles.
     * <p>
     * By default, the autonomous box spawns as an orange square at the minimum X and Y movement
     * bounds, scaled to half a game unit. Default values also exist for speed, scale, damage,
     * knockback and the tooltip text where specific values are not set.
     * <p>
     * The autonomous box's kinematic nature means it will not be affected by gravity or move
     * as a result of a collision force applied to it. It can be set to continuously travel along
     * a horizontal or vertical path at a set speed and distance, reversing direction when reaching
     * each bound.
     */
    public static class AutonomousBoxBuilder {

        // Default box movement
        private float minMoveX = 0f;
        private float maxMoveX = 0f;
        private float minMoveY = 0f;
        private float maxMoveY = 0f;
        private float speed = 3f;

        // Appearance
        private String texturePath = "images/box_orange.png";
        private float scaleX = 0.5f;
        private float scaleY = 0.5f;

        // Other default properties
        private float spawnX = (minMoveX + maxMoveX) / 2f;
        private float spawnY = (minMoveY + maxMoveY) / 2f;
        private int damage = 5;
        private float knockback = 4f;

        // Tooltip
        private String tooltipText = "";
        private TooltipSystem.TooltipStyle tooltipStyle = TooltipSystem.TooltipStyle.WARNING;

        /**
         * Sets the horizontal movement bounds for the box.
         * The box spawns midway between the min and max bounds.
         *
         * @param minX The minimum X coordinate
         * @param maxX The maximum X coordinate
         * @return the builder for chaining the horizontal movement bounds
         */
        public AutonomousBoxBuilder moveX(float minX, float maxX) {
            this.minMoveX = minX;
            this.maxMoveX = maxX;
            this.spawnX = (minX + maxX) / 2f;
            return this;
        }

        /**
         * Sets the vertical movement bounds for the box.
         * The box spawns midway between the min and max bounds.
         *
         * @param minY The minimum Y coordinate
         * @param maxY The maximum Y coordinate
         * @return the builder for chaining the vertical movement bounds
         */
        public AutonomousBoxBuilder moveY(float minY, float maxY) {
            this.minMoveY = minY;
            this.maxMoveY = maxY;
            this.spawnY = (minY + maxY) / 2f;
            return this;
        }

        /**
         * Sets the speed at which the box moves.
         *
         * @param speed Movement speed in world units per second
         * @return the builder for chaining the movement speed
         */
        public AutonomousBoxBuilder speed(float speed) {
            this.speed = speed;
            return this;
        }

        /**
         * Sets the visual scale of the box, relative to a single world unit.
         *
         * @param scaleX Horizontal scale
         * @param scaleY Vertical scale
         * @return the builder for chaining the scale
         */
        public AutonomousBoxBuilder scale(float scaleX, float scaleY) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            return this;
        }

        /**
         * Sets the damage the box deals to the player on each contact.
         *
         * @param damage The amount of damage applied to the player
         * @return the builder for chaining the amount of damage
         */
        public AutonomousBoxBuilder damage(int damage) {
            this.damage = damage;
            return this;
        }

        /**
         * Sets the knockback applied to the player on contact.
         *
         * @param knockback The knockback force applied to the player
         * @return the builder for chaining the knockback force
         */
        public AutonomousBoxBuilder knockback(int knockback) {
            this.knockback = knockback;
            return this;
        }

        /**
         * Sets a custom texture for the box.
         *
         * @param texturePath path to the image file
         * @return the builder for chaining the texture image
         */
        public AutonomousBoxBuilder texture(String texturePath) {
            this.texturePath = texturePath;
            return this;
        }

        /**
         * Sets a tooltip for the box
         *
         * @param text The tooltip text
         * @param style The tooltip style
         * @return the builder for chaining the tooltip text
         */
        public AutonomousBoxBuilder tooltip(String text, TooltipSystem.TooltipStyle style) {
            this.tooltipText = text;
            this.tooltipStyle = style;
            return this;
        }

        /**
         * Returns the x coordinate where the box will spawn.
         *
         * @return spawn location's x coordinate.
         */
        public float getSpawnX() {
            return spawnX;
        }

        /**
         * Returns the y coordinate where the box will spawn.
         *
         * @return spawn location's y coordinate.
         */
        public float getSpawnY() {
            return spawnY;
        }

        /**
         * Builds and returns the autonomous box entity with all its properties configured.

         * @return The constructed autonomous box entity
         */
        public Entity build() {
            Entity autonomousBox = new Entity();
            if (texturePath.endsWith(".atlas")) {
                TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(texturePath, TextureAtlas.class);
                AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
                animator.addAnimation("flying_bat", 0.1f, Animation.PlayMode.LOOP);
                animator.startAnimation("flying_bat");
                autonomousBox.addComponent(animator);
                if (texturePath.contains("flying_bat")) {
                    boolean isBat = texturePath.endsWith(".atlas") && texturePath.contains("flying_bat");
                    float mul = isBat ? 1.3f : 1f;
                    autonomousBox.setScale(scaleX * mul, scaleY * mul);
                }
            } else {
                autonomousBox.addComponent(new TextureRenderComponent(texturePath));
            }



            autonomousBox
                    .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.KinematicBody))
                    .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
                    .addComponent(new HitboxComponent())
                    .addComponent(new CombatStatsComponent(1, damage))
                    .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, knockback))
                    .addComponent(new AutonomousBoxComponent());




            AutonomousBoxComponent autonomousBoxComponent = autonomousBox.getComponent(AutonomousBoxComponent.class);
            autonomousBox.setScale(scaleX, scaleY);
            autonomousBoxComponent.setBounds(minMoveX, maxMoveX, minMoveY, maxMoveY);
            autonomousBoxComponent.setSpeed(speed);
            autonomousBox.getComponent(PhysicsComponent.class).getBody().setTransform(spawnX, spawnY, 0);
            autonomousBox.addComponent(new TooltipSystem.TooltipComponent(tooltipText, tooltipStyle));
            //
            return autonomousBox;
        }
    }
}

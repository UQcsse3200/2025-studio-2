package com.csse3200.game.entities.spawn;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.ButtonManagerComponent;
import com.csse3200.game.components.IdentifierComponent;
import com.csse3200.game.components.PositionSyncComponent;
import com.csse3200.game.components.collectables.CollectableComponentV2;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.UUID;

public final class Spawners {
    private Spawners(){}

    public static void registerAll(Entity player, GameArea area) {
        Logger logger = LoggerFactory.getLogger(Spawners.class);

        // --- Box ---
        SpawnRegistry.register("box", a -> {
            EntitySubtype subtype = EntitySubtype.fromString(a.subtype);

            Entity box = switch (subtype) {
                case MOVEABLE -> BoxFactory.createMoveableBox();
                case WEIGHTED -> BoxFactory.createWeightedBox();
                case REFLECTABLE -> BoxFactory.createReflectorBox();
                case null, default -> BoxFactory.createStaticBox();
            };
            linkEntities(box, a.linked);
            addIdentifier(box, String.valueOf(a.id));

            return box;
        });

        // --- Collectable ---
        SpawnRegistry.register("collectable", a -> {
            if (a.subtype == null) throw new RuntimeException("Collectable needs a subtype");
            if (a.target  == null) throw new RuntimeException("Collectable needs a target");

            Entity collectable = CollectableFactory.createCollectable(a.subtype + ":" + a.target);

            // visibility listener logic
            if (a.extra != null) {
                Entity toggler = ServiceLocator.getEntityService().getEntityById(a.extra);
                collectable.getComponent(CollectableComponentV2.class).toggleVisibility(false);

                // plate visibility logic -> show
                toggler.getEvents().addListener("platePressed", () -> {
                    collectable.getComponent(CollectableComponentV2.class).toggleVisibility(true);
                });

                // plate visibility logic -> hide
                toggler.getEvents().addListener("plateReleased", () -> {
                    collectable.getComponent(CollectableComponentV2.class).toggleVisibility(false);
                });
            }

            linkEntities(collectable, a.linked);
            addIdentifier(collectable, a.id);
            addTooltip(collectable, a.tooltip);
            return collectable;
        });

        // --- Camera ---
        SpawnRegistry.register("camera", a -> {
            String id = (a.id == null) ? UUID.randomUUID().toString() : a.id;

            Entity camera = SecurityCameraFactory.createSecurityCamera(player, a.speed, a.rotation, id);
            linkEntities(camera, a.linked);
            addIdentifier(camera, id);

            return camera;
        });

        // --- Pressure Plate ---
        SpawnRegistry.register("pressure_plate", a -> {
            Entity plate = PressurePlateFactory.createBoxOnlyPlate();
            linkEntities(plate, a.linked);
            addIdentifier(plate, String.valueOf(a.id));
            addTooltip(plate, a.tooltip);

            return plate;
        });

        // --- Floor ---
        SpawnRegistry.register("floor", a -> {
            EntitySubtype subtype = EntitySubtype.fromString(a.subtype);

            Entity floor = switch (subtype) {
                case GROUND -> FloorFactory.createGroundFloor();
                case DECORATIVE -> FloorFactory.createDecorativeFloor();
                default -> FloorFactory.createStaticFloor();
            };

            floor.setScale(a.sx, a.sy);
            return floor;
        });

        // --- Platform ---
        SpawnRegistry.register("platform", a -> {
            EntitySubtype subtype = EntitySubtype.fromString(a.subtype);

            Entity platform = switch (subtype) {
                case MOVING -> PlatformFactory.createMovingPlatform(new Vector2(a.dx, a.dy), a.speed);
                case VOLATILE -> PlatformFactory.createVolatilePlatform(a.speed, 1f);
                default -> PlatformFactory.createStaticPlatform();
            };

            linkEntities(platform, a.linked);
            addIdentifier(platform, String.valueOf(a.id));
            platform.setScale(a.sx, a.sy);

            return platform;
        });

        // --- Door ---
        SpawnRegistry.register("door", a -> {
            Entity door = ObstacleFactory.createDoor(String.valueOf(a.id), area);
            door.setScale(a.sx, a.sy);
            addTooltip(door, a.tooltip);

            return door;
        });

        // --- Door ---
        SpawnRegistry.register("door", a -> {
            String id = String.valueOf(a.id);
            String target    = a.target != null ? a.target : "";
            boolean isStatic = a.subtype != null && a.subtype.equalsIgnoreCase("static");

            Entity door = ObstacleFactory.createDoor(id, area, target, isStatic);
            door.setScale(a.sx, a.sy);
            addTooltip(door, a.tooltip);

            return door;
        });


        // --- Trap ---
        SpawnRegistry.register("trap", a -> {
            Entity trap = TrapFactory.createSpikes(new Vector2(a.safeX, a.safeY), a.rotation);

            linkEntities(trap, a.linked);
            addTooltip(trap, a.tooltip);
            trap.setScale(a.sx, a.sy);

            return trap;
        });

        // --- lasers ---
        SpawnRegistry.register("laser", a -> LaserFactory.createLaserEmitter(a.rotation));

        // --- Buttons ---
        SpawnRegistry.register("button", a -> {
            if (a.direction == null) a.direction = "right";
            Entity button = ButtonFactory.createButton(false, a.subtype, a.direction);

            // buttonToggled listener

            linkEntities(button, a.linked);
            addTooltip(button, a.tooltip);
            addIdentifier(button, String.valueOf(a.id));

            return button;
        });

        // --- Laser Detector ---
        SpawnRegistry.register("laser_detector", a -> {
            if (a.rotation != null) {
                return LaserDetectorFactory.createLaserDetector(a.rotation);
            }
            return LaserDetectorFactory.createLaserDetector();
        });

        // --- Sign Posts ---
        SpawnRegistry.register("sign_post", a -> {
            if (a.direction == null) a.direction = "right";
            SignpostFactory.createSignpost(a.direction);
            return SignpostFactory.createSignpost(a.direction);
        });

        // --- Death Zone ---
        SpawnRegistry.register("death_zone", a -> DeathZoneFactory.createDeathZone());

        // --- Upgrade ---
        SpawnRegistry.register("upgrade", a -> {
            return CollectableFactory.createJetpackUpgrade();
        });

    }

    // --- Helpers ---

    private static void addTooltip(Entity entity, String text) {
        if (text == null || text.isBlank()) return;
        entity.addComponent(new TooltipSystem.TooltipComponent(text, TooltipSystem.TooltipStyle.DEFAULT));
    }

    private static void linkEntities(Entity entity, String host) {
        if (host == null) return;
        Entity hostEntity = ServiceLocator.getEntityService().getEntityById(host);
        if (hostEntity == null) throw new RuntimeException("Failed to link: host not found: " + host);
        entity.addComponent(new PositionSyncComponent(hostEntity));
    }

    private static void addIdentifier(Entity entity, String id) {
        if (id == null || id.isBlank()) return;
        entity.addComponent(new IdentifierComponent(id));
    }
}

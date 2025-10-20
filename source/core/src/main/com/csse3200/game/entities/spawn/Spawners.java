package com.csse3200.game.entities.spawn;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.ButtonManagerComponent;
import com.csse3200.game.components.IdentifierComponent;
import com.csse3200.game.components.PositionSyncComponent;
import com.csse3200.game.components.collectables.CollectableComponentV2;
import com.csse3200.game.components.collectables.UpgradesComponent;
import com.csse3200.game.components.enemy.ActivationComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.obstacles.MoveableBoxComponent;
import com.csse3200.game.components.platforms.VolatilePlatformComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.UUID;

public final class Spawners {
    private static final Logger log = LoggerFactory.getLogger(Spawners.class);

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

            if (!a.isVisible) {
                box.getComponent(MoveableBoxComponent.class).setVisible(false);
            }

            if (a.target != null) {
                Entity target = ServiceLocator.getEntityService().getEntityById(a.target);

                target.getEvents().addListener("puzzleCompleted", () -> {
                    box.getEvents().trigger("setVisible", true);
                });
            }

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

                // button visibility logic -> show/hide
                toggler.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
                    collectable.getComponent(CollectableComponentV2.class).toggleVisibility(isPushed);
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
            EntitySubtype subtype = EntitySubtype.fromString(a.subtype);
            Entity plate;

            if (subtype == EntitySubtype.LADDER) {
                String ladderId = String.valueOf(a.id);
                if (ladderId == null || ladderId.isBlank()) {
                    throw new IllegalArgumentException("ladder_plate needs an  id matching its ladder");
                }

                plate = PressurePlateFactory.createLadderPlate(ladderId, a.offset, 0.05f);
            } else {
                plate = PressurePlateFactory.createBoxOnlyPlate();
                linkEntities(plate, a.linked);
                addIdentifier(plate, String.valueOf(a.id));

                if (a.target != null && a.extra != null) {
                    if (a.extra.equals("platform")) {
                        Entity target = ServiceLocator.getEntityService().getEntityById(a.target);
                        target.getEvents().trigger("stop");

                        plate.getEvents().addListener("platePressed", () -> {
                            target.getEvents().trigger("start");
                        });

                        plate.getEvents().addListener("plateReleased", () -> {
                            target.getEvents().trigger("stop");
                        });
                    }
                }
            }
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

        // -- Wall ---
        SpawnRegistry.register("wall", a -> {
            Entity wall =  WallFactory.createWall(a.x, a.y, a.dx, a.dy, "images/wall.png");
            wall.setScale(a.sx, a.sy);
            return wall;
        });

        // --- Platform ---
        SpawnRegistry.register("platform", a -> {
            EntitySubtype subtype = EntitySubtype.fromString(a.subtype);

            Entity platform = switch (subtype) {
                case MOVING -> PlatformFactory.createMovingPlatform(new Vector2(a.dx, a.dy), a.speed);
                case VOLATILE -> PlatformFactory.createVolatilePlatform(a.speed, 1f);
                case PLATE -> PlatformFactory.createPressurePlatePlatform();
                case REFLECTIVE -> PlatformFactory.createReflectivePlatform();
                case BUTTON -> PlatformFactory.createButtonTriggeredPlatform(new Vector2(a.dx, a.dy), a.speed);
                default -> PlatformFactory.createStaticPlatform();
            };

            if (subtype == EntitySubtype.PLATE && a.target != null) {
                Entity  target = ServiceLocator.getEntityService().getEntityById(a.target);
                platform.getComponent(VolatilePlatformComponent.class).linkToPlate(target);
            }

            linkEntities(platform, a.linked);
            addIdentifier(platform, String.valueOf(a.id));
            platform.setScale(a.sx, a.sy);

            return platform;
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
        SpawnRegistry.register("laser", a -> {
            Entity laser = LaserFactory.createLaserEmitter(a.rotation);
            addIdentifier(laser, String.valueOf(a.id));
            laser.setScale(a.sx, a.sy);
            return  laser;
        });

        // --- Buttons ---
        SpawnRegistry.register("button", a -> {
            if (a.direction == null) a.direction = "right";
            if (a.subtype == null) a.subtype = "standard";
            Entity button = ButtonFactory.createButton(false, a.subtype, a.direction);

            if (a.extra != null) {
                if(!a.extra.equals("evil")) {
                    // link to button manager
                    Entity target = ServiceLocator.getEntityService().getEntityById(a.extra);
                    ButtonManagerComponent manager = target.getComponent(ButtonManagerComponent.class);
                    ButtonComponent buttonComp = button.getComponent(ButtonComponent.class);

                    buttonComp.setPuzzleManager(manager);
                    manager.addButton(buttonComp);
                } else {
                    addGlow(button);
                }
            }

            if (a.target != null) {
                Entity target = ServiceLocator.getEntityService().getEntityById(a.target);

                button.getEvents().addListener("buttonToggled", (Boolean isPressed) -> {
                    if (isPressed) {
                        target.getEvents().trigger("disable");
                        target.getEvents().trigger("activatePlatform");
                    } else {
                        target.getEvents().trigger("enable");
                        target.getEvents().trigger("deactivatePlatform");
                    }
                });
            }

            linkEntities(button, a.linked);
            addTooltip(button, a.tooltip);
            addIdentifier(button, String.valueOf(a.id));

            return button;
        });

        // --- Button Manager ---
        SpawnRegistry.register("button_manager", a -> {
            Entity manager = new Entity().addComponent(new ButtonManagerComponent());

            addIdentifier(manager, String.valueOf(a.id));
            return manager;
        });

        // --- Laser Detector ---
        SpawnRegistry.register("laser_detector", a -> {
            Entity laserDetector = LaserDetectorFactory.createLaserDetector(a.rotation);

            if (a.target != null) {
                if (a.target.equals("jetpack")) {
                    Entity target = ServiceLocator.getEntityService().getEntityById(a.target);
                    laserDetector.getEvents().addListener("detectingStart", () -> {
                        UpgradesComponent cc = target.getComponent(UpgradesComponent.class);
                        cc.toggleVisibility(true);
                    });
                }
            }
            return laserDetector;
        });

        // --- Sign Posts ---
        SpawnRegistry.register("sign_post", a -> {
            if (a.direction == null) a.direction = "right";
            SignpostFactory.createSignpost(a.direction);
            return SignpostFactory.createSignpost(a.direction);
        });

        // --- Death Zone ---
        SpawnRegistry.register("death_zone", a -> {
            Entity deathZone = DeathZoneFactory.createDeathZone();

            if(a.sx != 1 && a.sy != 1) {
                deathZone.setScale(a.sx,a.sy);
            }

            if(a.extra != null) {
                deathZone.getComponent(ColliderComponent.class).setAsBoxAligned(deathZone.getScale().scl(Float.parseFloat(a.extra)),
                        PhysicsComponent.AlignX.LEFT,
                        PhysicsComponent.AlignY.BOTTOM);
            }

            return deathZone;
        });

        // --- Upgrade ---
        SpawnRegistry.register("upgrade", a -> {
            Entity upgrade = CollectableFactory.createJetpackUpgrade();
            if (a.isVisible == false) upgrade.getComponent(UpgradesComponent.class).toggleVisibility(false);
            addIdentifier(upgrade, String.valueOf(a.id));
            return  upgrade;
        });

        // --- Enemies ---
        SpawnRegistry.register("enemy", a -> {
            // get patrol
            Vector2[] patrolRoute;
            if (a.extra == null || a.extra.isBlank()) {
                patrolRoute = new Vector2[] {new Vector2(a.x / 2f, a.y / 2f), new Vector2(a.dx / 2f, a.dy / 2f)};
            } else {
                String[] patrolPts = a.extra.split(";");

                patrolRoute = new Vector2[patrolPts.length];

                for (int i = 0; i < patrolRoute.length; i++) {
                    String[] parts = patrolPts[i].split(",");
                    float x = Float.parseFloat(parts[0]) / 2f;
                    float y = Float.parseFloat(parts[1]) / 2f;
                    patrolRoute[i] = new Vector2(x,y);
                }
            }

            // get subtype
            EntitySubtype subtype = EntitySubtype.fromString(a.subtype);

            Entity enemy = switch (subtype) {
                case AUTO_BOMBER -> EnemyFactory.createAutoBomberDrone(player, patrolRoute, a.id);
                case SELF_DESTRUCT -> EnemyFactory.createSelfDestructionDrone(
                        player,
                        new Vector2((float) a.x / 2, (float) a.y / 2)
                ).addComponent(new ActivationComponent(a.id));
                case null, default -> EnemyFactory.createPatrollingDrone(player, patrolRoute);
            };

            return enemy;
        });

        SpawnRegistry.register("ladder", a -> {
            String ladderId = String.valueOf(a.id);
            if (ladderId == null || ladderId.isBlank()) {
                throw new IllegalArgumentException("ladder needs an id to group rungs/plates");
            }

            // anchor entity (returned to game area to position)
            Entity anchor = LadderFactory.createLadderBase(ladderId, a.height, a.offset);

            return anchor;
        });

        // --- Codex Terminal ---
        SpawnRegistry.register("terminal", a ->
                CodexTerminalFactory.createTerminal(ServiceLocator.getCodexService().getEntry(a.id)));

        // --- Tutorials ---

        SpawnRegistry.register("tutorial", a -> {
            EntitySubtype subtype = EntitySubtype.fromString(a.subtype);

            Entity tutorial = switch (subtype) {
                case JUMP -> ActionIndicatorFactory.createJumpTutorial();
                case DOUBLE_JUMP -> ActionIndicatorFactory.createDoubleJumpTutorial();
                case DASH -> ActionIndicatorFactory.createDashTutorial();
                case null, default -> ActionIndicatorFactory.createJumpTutorial();
            };

            return tutorial;
        });

        // --- Objectives ---
        SpawnRegistry.register("objective", a ->
                CollectableFactory.createObjective(a.id, a.sx, a.sy));

        // --- Bats ---
        SpawnRegistry.register("bat", a -> {
            BoxFactory.AutonomousBoxBuilder builder = new BoxFactory.AutonomousBoxBuilder();
            Entity bat = builder
                    .moveX(a.x, a.dx).moveY(a.y, a.dy)
                    .texture("images/flying_bat.atlas")
                    .speed(a.speed)
                    .build();

            addTooltip(bat, a.tooltip);
            return bat;
        });

        /*
        // --- Prompts ---
        SpawnRegistry.register("prompt", a -> {
            return CollectableFactory.createPrompt(a.extra, a.speed, a.dx, a.dy);
        });

         */
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

    private static void addGlow(Entity entity) {
        ConeLightComponent evilGlow = new ConeLightComponent(
                ServiceLocator.getLightingService().getEngine().getRayHandler(),
                128,
                new Color().set(1f, 0f, 0f, 0.6f),
                2.5f,
                0f,
                180f);
        entity.addComponent(evilGlow);
    }
}

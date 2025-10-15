package com.csse3200.game.components.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.ladders.LadderComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.Vector2Utils;

import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Math.abs;

/**
 * Input handler for the player for keyboard and touch (mouse) input.
 * This input handler only uses keyboard input.
 */
public class KeyboardPlayerInputComponent extends InputComponent {
    private final Vector2 walkDirection = Vector2.Zero.cpy();

    private boolean isGliding;

    private int[] CHEAT_INPUT_HISTORY = new int[4];
    private int cheatPosition = 0;
    private Boolean cheatsOn = false;

    private boolean holdingBox = false;
    private Entity heldBox = null;

    private HashMap<Integer, Boolean> pressedKeys = new HashMap<>();

    private Array<Entity> ladders = null;

    private Boolean onLadder = false;

    public KeyboardPlayerInputComponent() {
        super(5);
    }

    /**
     * Triggers player events on specific keycodes.
     *
     * @return whether the input was processed
     * @see InputProcessor#keyDown(int)
     */
    @Override
    public boolean keyDown(int keycode) {
        if (!enabled) return false;

        // If the key has already been pressed then it's a legacy input from pausing the game
        if (pressedKeys.getOrDefault(keycode, false)) {
            return false;
        }

        //gets all the ladder in the level if not already done so.
        if (this.ladders == null) {
            this.ladders = findLadders();
        }

        if (keycode == Keymap.getActionKeyCode("PlayerJump")) {
            //takes player off ladder if they are on one.
            this.onLadder = false;
            entity.getEvents().trigger("gravityForPlayerOn");

            triggerJumpEvent();
        } else if (keycode == Keymap.getActionKeyCode("PlayerLeft")) {
            //takes player off ladder if they are on one.
            this.onLadder = false;

            walkDirection.add(Vector2Utils.LEFT);
            triggerWalkEvent();
            if (isGliding) {
                triggerGlideEvent(true);
            }
        } else if (keycode == Keymap.getActionKeyCode("PlayerRight")) {
            //takes player off ladder if they are on one.
            this.onLadder = false;

            walkDirection.add(Vector2Utils.RIGHT);
            triggerWalkEvent();
            if (isGliding) {
                triggerGlideEvent(true);
            }
        } else if (keycode == Keymap.getActionKeyCode("PlayerInteract")) {
            entity.getEvents().trigger("interact");
        } else if (keycode == Keymap.getActionKeyCode("PlayerDash")) {
            //takes player off ladder if they are on one.
            this.onLadder = false;
            triggerDashEvent();
        } else if (keycode == Keymap.getActionKeyCode("PlayerCrouch")) {
            triggerCrouchEvent();
        } else if (keycode == Keymap.getActionKeyCode("Reset")) {
            entity.getEvents().trigger("reset"); // This might cause a memory leak?
        } else if (keycode == Keymap.getActionKeyCode("Glide")) {
            triggerGlideEvent(true);
        }
        // Sprint: TAB (and optionally a Keymap binding named "PlayerSprint")
        else if (keycode == Keys.TAB || keycode == Keymap.getActionKeyCode("PlayerSprint")) {
            entity.getEvents().trigger("sprintStart");
        } else if (keycode == Keymap.getActionKeyCode("PlayerUp")) {
            CHEAT_INPUT_HISTORY = addToCheatHistory(CHEAT_INPUT_HISTORY, cheatPosition, Keymap.getActionKeyCode("PlayerUp"));
            cheatPosition++;

            //Only moves the player up if they are in front of a ladder.
            if (inFrontOfLadder(findLadders())) {
                this.onLadder = true;
                //walkDirection.sub(Vector2Utils.DOWN);
                walkDirection.add(Vector2Utils.UP);
                triggerWalkEvent();
            } else {
                entity.getEvents().trigger("gravityForPlayerOn");
                this.onLadder = false;

                triggerJetpackEvent();
                if (cheatsOn) {
                    walkDirection.add(Vector2Utils.UP);
                    triggerWalkEvent();
                }
            }
        } else if (keycode == Keymap.getActionKeyCode("PlayerDown")) {
            CHEAT_INPUT_HISTORY = addToCheatHistory(CHEAT_INPUT_HISTORY, cheatPosition, Keymap.getActionKeyCode("PlayerDown"));
            cheatPosition++;

            //Only moves the player down if they are in front of a ladder.
            if (inFrontOfLadder(this.ladders)) {
                this.onLadder = true;
                //walkDirection.sub(Vector2Utils.UP);
                walkDirection.add(Vector2Utils.DOWN);
                triggerWalkEvent();
            } else {
                entity.getEvents().trigger("gravityForPlayerOn");
                this.onLadder = false;
                if (cheatsOn) {
                    walkDirection.add(Vector2Utils.DOWN);
                    triggerWalkEvent();
                }
            }
        } else if (keycode == Keymap.getActionKeyCode("Enter")) {
            enableCheats();
        } else {
            return false;
        }

        // Mark key as pressed
        pressedKeys.put(keycode, true);
        return true;
    }

    /**
     * Triggers player events on specific keycodes.
     *
     * @return whether the input was processed
     * @see InputProcessor#keyUp(int)
     */
    @Override
    public boolean keyUp(int keycode) {
        if (!enabled) return false;

        // If the key hasn't been pressed but has somehow been released then it's a legacy input
        // from an earlier KeyboardPlayerInputComponent
        if (!pressedKeys.getOrDefault(keycode, false)) {
            return false;
        }

        //gets all the ladder in the level if not already done so.
        if (this.ladders == null) {
            this.ladders = findLadders();
        }

        if (this.onLadder) {
            this.onLadder = inFrontOfLadder(this.ladders);
        }

        if (keycode == Keymap.getActionKeyCode("PlayerLeft")) {
            walkDirection.sub(Vector2Utils.LEFT);
            triggerWalkEvent();
        } else if (keycode == Keymap.getActionKeyCode("PlayerRight")) {
            walkDirection.sub(Vector2Utils.RIGHT);
            triggerWalkEvent();
        } else if (keycode == Keymap.getActionKeyCode("PlayerUp")) {
            if (inFrontOfLadder(findLadders())) {
                //walkDirection.setZero();
                walkDirection.sub(Vector2Utils.UP);
                triggerWalkEvent();
                entity.getEvents().trigger("walkStop");

            } else {
                entity.getEvents().trigger("gravityForPlayerOn");
                this.onLadder = false;

                triggerJetpackOffEvent();
                if (cheatsOn) {
                    walkDirection.sub(Vector2Utils.UP);
                    triggerWalkEvent();
                }
            }
        } else if (keycode == Keymap.getActionKeyCode("PlayerDown")) {
            if (inFrontOfLadder(this.ladders)) {
                //walkDirection.setZero();
                walkDirection.sub(Vector2Utils.DOWN);
                triggerWalkEvent();
                entity.getEvents().trigger("walkStop");

            } else {
                entity.getEvents().trigger("gravityForPlayerOn");
                this.onLadder = false;

                if (cheatsOn) {
                    walkDirection.sub(Vector2Utils.DOWN);
                    triggerWalkEvent();
                }
            }
        } else if (keycode == Keys.TAB || keycode == Keymap.getActionKeyCode("PlayerSprint")) {
            entity.getEvents().trigger("sprintStop");
        } else if (keycode == Keymap.getActionKeyCode("Glide")) {
            this.onLadder = false;
            triggerGlideEvent(false);
            // Need to mark the following keys as released
        } else if (keycode == Keymap.getActionKeyCode("PlayerJump")) {
        } else if (keycode == Keymap.getActionKeyCode("PlayerDash")) {
        } else if (keycode == Keymap.getActionKeyCode("PlayerInteract")){
        } else if (keycode == Keymap.getActionKeyCode("PlayerCrouch")) {
        } else if (keycode == Keymap.getActionKeyCode("Enter")) {
        } else if (keycode == Keymap.getActionKeyCode("Reset")) {
        } else {
            return false;
        }

        // Mark key as released
        pressedKeys.put(keycode, false);
        return true;
    }

    private void triggerWalkEvent() {
        if (walkDirection.epsilonEquals(Vector2.Zero)) {
            entity.getEvents().trigger("walkStop");
        } else {
            if (!cheatsOn && !onLadder && Math.abs(walkDirection.y) > 0f) {
                walkDirection.y = 0f;
            }
            entity.getEvents().trigger("walk", walkDirection);
        }
    }

    /**
     * Use this to start a jump event
     */
    private void triggerJumpEvent() {
        entity.getEvents().trigger("jump"); //put jump here

    }

    private void triggerDashEvent() {
        if (entity.getComponent(InventoryComponent.class).hasItem(InventoryComponent.Bag.UPGRADES, "dash")) {
            entity.getEvents().trigger("dash");
        }
    }

    private void triggerCrouchEvent() {
        entity.getEvents().trigger("crouch");
    }

    private void triggerGlideEvent(boolean status) {
        if (entity.getComponent(InventoryComponent.class).hasItem(InventoryComponent.Bag.UPGRADES, "glider")) {
            entity.getEvents().trigger("glide", status);
        }
        isGliding = status;
    }

    private void triggerJetpackEvent() {

        if (entity.getComponent(InventoryComponent.class).hasItem(InventoryComponent.Bag.UPGRADES, "jetpack")) {
            entity.getEvents().trigger("jetpackOn");
        }

    }

    private void triggerJetpackOffEvent() {

        entity.getEvents().trigger("jetpackOff");
    }

    private int[] addToCheatHistory(int[] keyHistory, int position, int input) {
        if (position > 3) {
            for (int i = 1; i < 3; i++) {
                keyHistory[i] = keyHistory[i + 1];
            }
            keyHistory[3] = input;
        } else {
            keyHistory[position] = input;
        }


        return keyHistory;
    }

    public int[] getInputHistory() {
        return CHEAT_INPUT_HISTORY;
    }

    public Boolean getIsCheatsOn() {
        return cheatsOn;
    }

    public void setIsCheatsOn(boolean on) {
        cheatsOn = on;
    }

    private void enableCheats() {
        if (Arrays.equals(CHEAT_INPUT_HISTORY, new int[]{Keymap.getActionKeyCode("PlayerUp"), Keymap.getActionKeyCode("PlayerUp"), Keymap.getActionKeyCode("PlayerDown"), Keymap.getActionKeyCode("PlayerUp")})) {
            cheatsOn = !cheatsOn;
            entity.getEvents().trigger("gravityForPlayerOff");
        }
    }

    public void resetInputState() {
        walkDirection.setZero();
        triggerWalkEvent();
        pressedKeys.clear();
    }

    /**
     * Return current walk direction.
     * (Only current use is for transfers between resets.)
     *
     * @return walkDirection
     */
    @Deprecated
    public Vector2 getWalkDirection() {
        return walkDirection;
    }

    /**
     * Set current walk direction.
     * (Only current use is for transfers between resets.)
     *
     * @param walkDirection - walkDirection to set.
     */
    @Deprecated
    public void setWalkDirection(Vector2 walkDirection) {
        this.walkDirection.set(walkDirection);
    }

    /**
     * Checks every entity currently in the game and finds all the ones that are ladders.
     *
     * @return Array of Entities that are ladders.
     */
    private Array<Entity> findLadders() {
        Array<Entity> ladd = new Array<>();
        Array<Entity> bobs = ServiceLocator.getEntityService().get_entities();
        for (Entity bob : bobs) {
            if (bob.getComponent(LadderComponent.class) != null) {
                ladd.add(bob);
            }
        }
        return ladd;
    }

    /**
     * Checks if the player is in front of one of the ladders in the level.
     * <p>
     * A player is in front of a ladder when they are roughly aligned with the ladder's horizontal
     * centre, and their vertical position is within the ladder's height. <br>
     * When in front of a ladder, gravity is temporarily disabled for the player to allow
     * climbing.<br>
     * The in-front check allows the player to re-engage with a ladder after walking away,
     * enabling climbing even if they briefly step off.
     *
     * @param ladders  Array of ladder entities currently in the level
     * @return true if in front of a ladder, false if not.
     */
    private Boolean inFrontOfLadder(Array<Entity> ladders) {
        // Player's horizontal centre and vertical boundaries
        float playerCentreX = entity.getCenterPosition().x;
        float playerBottom = entity.getPosition().y;
        float playerTop = playerBottom + entity.getScale().y;

        // Horizontal margin for detecting ladder nearby
        float horizontalMargin = 0.1f;
        boolean inFront = false;

        for (Entity ladder : ladders) {
            // Ladder's horizontal center and vertical boundaries
            float ladderCentreX = ladder.getPosition().x + ladder.getScale().x / 2f;
            float ladderBottom = ladder.getPosition().y;
            float ladderTop = ladderBottom + ladder.getScale().y;

            // Check horizontal and vertical overlap
            float allowedDistance = ladder.getScale().x / 2f + horizontalMargin;
            boolean xOverlap = Math.abs(playerCentreX - ladderCentreX) <= allowedDistance;
            boolean yOverlap = playerTop >= ladderBottom && playerBottom <= ladderTop;

            if (xOverlap && yOverlap) {
                // Player is in front of this ladder, allows climbing after stepping away
                inFront = true;
                break;
            }
        }
        // Update player's state on/off ladder
        if (inFront) {
            entity.getEvents().trigger("gravityForPlayerOff");
            this.onLadder = true;
        } else {
            this.onLadder = false;
        }
        return inFront;
    }

    /**
     * Gets the current on ladder state of the player
     * @return boolean value of the state.
     */
    public Boolean getOnLadder() {
        return this.onLadder;
    }

    public boolean isHoldingBox() {
        return this.holdingBox;
    }

    public void setHoldingBox(boolean holdingBox) {
        this.holdingBox = holdingBox;
    }

    public void setHeldBox (Entity e) {
        this.heldBox = e;
    }

    public Entity getHeldBox() {
        return this.heldBox;
    }

    /**
     * Sets the on ladder state of the player to the given boolean value
     * @param set boolean value to set the on ladder state too.
     */
    public void setOnLadder (boolean set) {
        this.onLadder = set;
    }
}

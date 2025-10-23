package com.csse3200.game.components.ladders;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LadderSectionControllerComponent extends Component {
    private final String id;
    private final int extendCount;
    private final float stepInterval;

    private final List<LadderRungComponent> rungs = new ArrayList<>();

    private int visibleCount = 0;
    private int targetVisible = 0;
    private float stepTimer = 0f;

    private Sound ladderSfx;
    private int lastMoveDir = 0; // -1 is retracting, 0 is idle, +1 is extending
    private boolean playedOnExtend = false;
    private boolean playedOnRetract = false;

    public LadderSectionControllerComponent(String id, int extendCount, float stepInterval) {
        this.id = id;
        this.extendCount = extendCount;
        this.stepInterval = stepInterval;
    }

    @Override
    public void create() {
        findExtendableRungs();

        ladderSfx = ServiceLocator.getResourceService().getAsset(
                "sounds/laddersound.mp3", Sound.class);

        // setup listeners
        entity.getEvents().addListener("platePressed", this::onPressed);
        entity.getEvents().addListener("plateReleased", this::onReleased);
    }

    @Override
    public void update() {
        if (rungs.isEmpty()) return;

        int moveDir = Integer.signum(targetVisible - visibleCount);
        if (moveDir != 0 && moveDir != lastMoveDir) {
            // direction flipped or started moving
            playedOnExtend = false;
            playedOnRetract = false;
            lastMoveDir = moveDir;
        }

        // if nothing to do stop quickly
        if (visibleCount == targetVisible) return;

        // move one rung per interval towards the target
        stepTimer += ServiceLocator.getTimeSource().getDeltaTime();
        while (stepTimer >= stepInterval && visibleCount != targetVisible) {
            stepTimer -= stepInterval;

            if (visibleCount < targetVisible) {
                // reveal next rung in the sequence
                rungs.get(visibleCount).show();
                visibleCount++;
            } else {
                // hide the last revealed rung
                visibleCount--;
                rungs.get(visibleCount).hide();
            }
        }

        playSound();
    }

    private void playSound() {
        if (ladderSfx == null || rungs.isEmpty()) return;

        int half = rungs.size() / 2;

        if (lastMoveDir > 0) {
            if (!playedOnExtend && visibleCount >= half) {
                ladderSfx.play(UserSettings.get().masterVolume);
                playedOnExtend = true;
            }
        } else if (lastMoveDir < 0) {
            if (!playedOnRetract && visibleCount <= half) {
                ladderSfx.play(UserSettings.get().masterVolume);
                playedOnRetract = true;
            }
        }
    }

    private void onPressed() {
        // ensure rung list is present
        if (rungs.isEmpty()) {
            findExtendableRungs();
        }
        targetVisible = rungs.size();
    }

    private void onReleased() {
        if (rungs.isEmpty()) {
            findExtendableRungs();
        }
        targetVisible = 0;
    }

    private List<LadderRungComponent> findExtendableRungs() {
        rungs.clear();
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            LadderRungComponent rung = e.getComponent(LadderRungComponent.class);
            if (rung != null && id.equals(rung.getId()) && rung.getRungIdx() < extendCount) {
                rungs.add(rung);
            }
        }
        // sort base->up by rung idx
        rungs.sort(Comparator.comparingInt(LadderRungComponent::getRungIdx).reversed());
        return rungs;
    }


}

package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * BossLaserRenderComponent renders the boss's laser visually.
 * Laser positions are updated every frame by BossLaserAttack.
 */
public class BossLaserRenderComponent extends RenderComponent {

    private final Color color = new Color(1f, 0f, 0f, 1f);
    private final Color glowColor = new Color(1f, 0.32f, 0.32f, 1f);

    private static final float THICKNESS = 0.05f;  // core laser thickness
    private static final int GLOW_STEPS = 4;       // glow smoothness
    private static final float GLOW_MULT = 2.7f;   // glow thickness multiplier
    private static final float GLOW_ALPHA = 0.35f; // max glow alpha

    private TextureRegion laserTexture;
    private final List<Vector2> positions = new ArrayList<>();

    @Override
    public void create() {
        super.create();

        // Load laser texture from boss atlas
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/boss.atlas", TextureAtlas.class);

        if (atlas != null && atlas.findRegion("shoot-laser") != null) {
            laserTexture = atlas.findRegion("shoot-laser");
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {
        if (positions.size() < 2 || laserTexture == null) return;

        for (int i = 0; i < positions.size() - 1; i++) {
            Vector2 a = positions.get(i);
            Vector2 b = positions.get(i + 1);

            float dx = b.x - a.x;
            float dy = b.y - a.y;
            float len = (float) Math.hypot(dx, dy);
            if (len < 1e-4f) continue;

            float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

            // Draw glow underneath
            batch.flush();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            for (int s = GLOW_STEPS; s >= 1; s--) {
                float t = THICKNESS * (1f + (GLOW_MULT - 1f) * (s / (float) GLOW_STEPS));
                float aGlow = GLOW_ALPHA * (s / (float) GLOW_STEPS);
                batch.setColor(glowColor.r, glowColor.g, glowColor.b, aGlow);

                batch.draw(
                        laserTexture,
                        a.x, a.y - t / 2f,
                        0, t / 2f,
                        len, t,
                        1f, 1f,
                        angleDeg
                );
            }
            batch.flush();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            // Draw core laser
            batch.setColor(color);
            batch.draw(
                    laserTexture,
                    a.x, a.y - THICKNESS / 2f,
                    0, THICKNESS / 2f,
                    len, THICKNESS,
                    1f, 1f,
                    angleDeg
            );
        }

        batch.setColor(Color.WHITE); // reset batch color
    }

    /**
     * Update the laser positions from BossLaserAttack.
     *
     * @param positions current laser positions
     */
    public void setLaserPositions(List<Vector2> positions) {
        this.positions.clear();
        this.positions.addAll(positions);
    }
}

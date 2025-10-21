package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.lasers.LaserEmitterComponent;
import com.csse3200.game.components.lasers.LaserShowerComponent;
import com.csse3200.game.entities.Entity;


import java.util.List;

/**
 * Renders laser beams for both LaserEmitter and LaserShower entities.
 * Supports glow effects and core beam rendering.
 */
public class LaserRenderComponent extends RenderComponent {
    private Texture pixelTex;
    private TextureRegion pixel;
    private Color color;
    private Color glowColor;

    private static final float THICKNESS  = 0.05f; // core beam thickness
    private static final int   GLOW_STEPS = 4;     // glow smoothness
    private static final float GLOW_MULT  = 2.7f;  // outer glow thickness multiplier
    private static final float GLOW_ALPHA = 0.35f; // max glow alpha

    private LaserShowerComponent showerEmitter;
    private LaserEmitterComponent mainEmitter;

    @Override
    public void create() {
        super.create();
        // Get laser components from the entity
        showerEmitter = entity.getComponent(LaserShowerComponent.class);
        mainEmitter = entity.getComponent(LaserEmitterComponent.class);

        // Assign colors based on laser type
        if (showerEmitter != null) {
            // Blue laser shower
            color = new Color(0.05f, 0.35f, 0.6f, 1.0f);      // Core beam (A darker, deeper Cyan)
            glowColor = new Color(0.15f, 0.5f, 0.8f, 1.0f);   // Glow
        } else if (mainEmitter != null) {
            // Red laser emitter
            color = new Color(1f, 0f, 0f, 1f);
            glowColor = new Color(1f, 0.32f, 0.32f, 1f);
        } else {
            // Default fallback (white)
            color = new Color(1f, 1f, 1f, 1f);
            glowColor = new Color(1f, 1f, 1f, 0.5f);
        }
        // make 1x1 pixel
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        pixelTex = new Texture(pm);
        pixelTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixel = new TextureRegion(pixelTex);
        pm.dispose();
    }

    @Override
    protected void draw(SpriteBatch batch) {
        if (mainEmitter != null && !mainEmitter.getEnable()) return;
        List<Vector2> pts = null;

        // Get laser path points from the appropriate component
        if (mainEmitter != null) {
            pts = mainEmitter.getPositions();
        } else if (showerEmitter != null) {
            pts = showerEmitter.getPositions();
        }

        if (pts == null || pts.size() < 2) return;

        // Draw each segment of the laser
        for (int i = 0; i < pts.size() - 1; i++) {
            Vector2 a =  pts.get(i);
            Vector2 b =  pts.get(i + 1);

            float dx = b.x - a.x;
            float dy = b.y - a.y;
            float len = (float) Math.hypot(dx, dy);// Length of segment
            if (len < 1e-4f) continue; // Skip very short segments

            float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

            // draw glow pass first underneath
            batch.flush();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            for (int s = GLOW_STEPS; s >= 1; s--) {
                float t = THICKNESS * (1f + (GLOW_MULT - 1f) * (s / (float) GLOW_STEPS));
                float aGlow = GLOW_ALPHA * (s / (float) GLOW_STEPS);
                batch.setColor(glowColor.r, glowColor.g, glowColor.b, aGlow);

                float originX = 0f;
                float originY = t / 2f;
                batch.draw(
                        pixel,
                        a.x, a.y - originY,
                        originX, originY,
                        len, t,
                        1f, 1f,
                        angleDeg
                );
            }
            batch.flush();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            // draw the "pixel" stretched to len x thickness
            batch.setColor(color);
            float originX = 0f;
            float originY = THICKNESS / 2f;
            batch.draw(
                    pixel,
                    a.x, a.y - originY,
                    originX, originY,
                    len, THICKNESS,
                    1f, 1f,
                    angleDeg
            );
        }

        // reset batch color
        batch.setColor(1f, 1f, 1f, 1f);
    }
    // Accessors for color and glow
    public Color getColor() { return color; }
    public Color getGlowColor() { return glowColor; }
    public void setEntity(Entity entity) {
        this.entity = entity;
    }


    @Override
    public void dispose() {
        if (pixelTex != null) pixelTex.dispose();
        super.dispose();
    }
}

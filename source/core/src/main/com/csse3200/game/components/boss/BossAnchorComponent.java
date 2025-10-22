package com.csse3200.game.components.boss;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

public class BossAnchorComponent extends Component {
    private float marginX = 1.0f;  // from left side of camera viewport
    private float offsetY = 0f;
    private PhysicsComponent phys;

    public BossAnchorComponent(float marginX, float offsetY) {
        this.marginX = marginX; this.offsetY = offsetY;
    }

    @Override
    public void create() {
        phys = entity.getComponent(PhysicsComponent.class);

        if (phys != null && phys.getBody() != null) {
            phys.getBody().setGravityScale(0f);
            phys.getBody().setFixedRotation(true);
        }
        pinToCamera();
    }

    @Override
    public void update() {
        pinToCamera();   // keep it pinned every frame
    }

    private void pinToCamera() {
        OrthographicCamera cam = (OrthographicCamera)
                ServiceLocator.getRenderService().getRenderer().getCamera().getCamera();
        if (cam == null) return;

        float left = cam.position.x - cam.viewportWidth * 0.5f;
        float midY = cam.position.y + offsetY;

        float w = entity.getScale().x;
        float h = entity.getScale().y;
        float cx = left + marginX + w * 0.5f;  // left edge pinned at marginX

        if (phys != null && phys.getBody() != null) {
            phys.getBody().setTransform(cx, midY, phys.getBody().getAngle());
            phys.getBody().setLinearVelocity(0f, 0f);
        }
        entity.setPosition(cx - w * 0.5f, midY - h * 0.5f);
    }
}

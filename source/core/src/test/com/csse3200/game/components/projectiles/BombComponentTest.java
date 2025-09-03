package com.csse3200.game.components.projectiles;

import com.csse3200.game.components.DisposalComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class BombComponentTest {
    @BeforeEach
    void setup() {
        ServiceLocator.clear();
        ServiceLocator.registerTimeSource(new GameTime());
    }
    private Entity makeBombEntity(float explosionDelay, float explosionRadius, short targetLayer) {
        Entity e = new Entity();
        e.addComponent(new BombComponent(explosionDelay, explosionRadius, targetLayer));
        e.create();
        return e;
    }

    @Test
    void addDisposalComponentOnCreate() {
        Entity e = makeBombEntity(2.0f, 2.5f, (short) 0);
        DisposalComponent dc = e.getComponent(DisposalComponent.class);
        assertNotNull(dc, "Bomb should attach a DisposalComponent during create()");
    }

    @Test
    void doesNotExplodeImmediately() {
        Entity e = makeBombEntity(2.0f, 2.5f, (short) 0);
        BombComponent bomb = e.getComponent(BombComponent.class);
        assertNotNull(bomb);

        bomb.update();

        assertNotEquals(0f, e.getScale().x, 1e-6, "Bomb should remain visible right after create()");
        assertNotEquals(0f, e.getScale().y, 1e-6, "Bomb should remain visible right after create()");
        assertFalse(bomb.hasExploded());
    }
}

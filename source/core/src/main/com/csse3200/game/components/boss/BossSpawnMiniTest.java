package com.csse3200.game.components.boss;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.Gdx;
public class BossSpawnMiniTest extends Component{
	private float t = 0f;
	private boolean started = false;
	private boolean bumped = false;

	@Override
	public void update() {
		t += ServiceLocator.getTimeSource().getDeltaTime();

		// burst after 1s
		if (!started && t > 1f) {
			Gdx.app.log("BossSpawnMiniTest", "trigger startSpawning");
			entity.getEvents().trigger("boss:startSpawning");
			started = true;
		}

		//  start Phase 2 after 6s
		if (!bumped && t > 6f) {
			Gdx.app.log("BossSpawnMiniTest", "trigger setPhase(2)");
			entity.getEvents().trigger("boss:setPhase", 2);
			bumped = true;
		}

		// stop after 12s
		if (t > 12f) {
			Gdx.app.log("BossSpawnMiniTest", "trigger stopSpawning");
			entity.getEvents().trigger("boss:stopSpawning");
			// Delete automatically after testing to avoid repeated triggering
			entity.removeComponent(this);

		}
	}
}

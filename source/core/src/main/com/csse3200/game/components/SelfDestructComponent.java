package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * component for self destruction drones
 */
public class SelfDestructComponent extends Component {
    private final Entity target;
    private boolean exploded =false;

    private static final String EXPLOSION_SOUND = "sounds/explosion.mp3";
    public SelfDestructComponent(Entity target){
        this.target=target;
    }

    @Override
    public void update(){
        if(exploded) return;
        if(target==null) return;

        if(entity.getCenterPosition().dst(target.getCenterPosition()) <1.1f){
            explode();
        }
    }

    private void explode(){
        if(exploded) return;
        exploded =true;
        CombatStatsComponent targetStats= target.getComponent(CombatStatsComponent.class);
        if (targetStats!=null ){
            targetStats.setHealth(Math.max(0,targetStats.getHealth()-2));
        }
        AnimationRenderComponent animator= entity.getComponent(AnimationRenderComponent.class);
        if(animator!=null){
            animator.startAnimation("bomb_effect");
        }
        Sound explosionSound = ServiceLocator.getResourceService().getAsset(EXPLOSION_SOUND,Sound.class);
        if (explosionSound!=null){
            long soundId = explosionSound.play(1.0f); // full volume
            fadeOutSound(explosionSound,soundId,0.5f); // fade out over 0.5 secondes
        }

        Timer.schedule(new Timer.Task(){
            @Override
            public void run(){
                if(animator!=null){
                    animator.stopAnimation();
                    entity.removeComponent(animator);
                }
                PhysicsComponent physics= entity.getComponent(PhysicsComponent.class);
                if(physics!=null){
                    physics.getBody().setActive(false);
                    entity.removeComponent(physics);
                    // physics.dispose();  //to safely destroy box2D body
                }
                entity.getEvents().trigger("destroy"); // custom destroy event for cleanup
                entity.removeComponent(SelfDestructComponent.this);
            }
        }, 0.5f);
    }

    /**
     * Gradually fades out the given sound over specified duration
     * @param sound Sound object
     * @param soundId Sound instance ID
     * @param duration Duration to fade out in seconds
     */
    private void fadeOutSound(Sound sound, long soundId, float duration) {
        final int steps =10;
        final float interval = duration/steps;

        for(int i=0;i<steps;i++){
            final float volume = 1.0f - (i/(float)steps);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    sound.setVolume(soundId, volume);
                    if (volume <= 0f){
                        sound.stop(soundId);
                    }
                }
            },i*interval);
        }
    }
}
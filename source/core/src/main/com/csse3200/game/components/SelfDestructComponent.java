package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.lighting.ConeLightComponent;
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
    private static final float MAX_DISTANCE=11f;
    private static final float TELEPORT_OFFSET = 1.5f;
    public SelfDestructComponent(Entity target){
        this.target=target;
    }

    @Override
    public void update(){
        if(exploded) return;
        if(target==null) return;

        float distance = entity.getCenterPosition().dst(target.getCenterPosition());
        if(distance > MAX_DISTANCE){
            teleportNearPlayer();
            //return;
        }
        if( distance<1.1f){
            explode();
        }
    }
    private void teleportNearPlayer(){
        Vector2 position=target.getCenterPosition();

        float randomAngle = (float)(Math.random()*Math.PI*2);
        Vector2 offset = new Vector2(
                (float) Math.cos(randomAngle)*TELEPORT_OFFSET,
                (float) Math.sin(randomAngle)*TELEPORT_OFFSET
        );
        Vector2 newPos = position.cpy().add(offset);
        entity.setPosition(newPos);
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
                PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
                if(physics!=null){
                    if(physics.getBody()!=null){
                        physics.getBody().setActive(false);
                    }
                    entity.removeComponent(physics);
                }
                ConeLightComponent light= entity.getComponent(ConeLightComponent.class);
                if(light!=null){
                    light.dispose();
                    //physics.getBody().setActive(false);
                    entity.removeComponent(light);
                }
                entity.getEvents().trigger("destroy");
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
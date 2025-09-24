package com.csse3200.game.components;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;

/**
 * component for self destruction drones
 */
public class SelfDestructComponent extends Component {
    private final Entity target;
    private boolean exploded =false;

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
        CombatStatsComponent selfStats = entity.getComponent(CombatStatsComponent.class);
        if (targetStats!=null && selfStats!= null){
            targetStats.hit(selfStats);

        }
        AnimationRenderComponent animator= entity.getComponent(AnimationRenderComponent.class);
        if(animator!=null){
            animator.startAnimation("bomb_effect");
        }

        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task(){
            @Override
            public void run(){
                entity.getEvents().trigger("destroy"); // custom destroy event for cleanup
                entity.removeComponent(SelfDestructComponent.this);
            }


        }, 0.5f);
    }



}
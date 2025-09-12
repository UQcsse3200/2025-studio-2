package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;

/**
 * component for self destruction drones
 */
public class SelfDestructComponent extends Component {
    private final Entity target;
    private boolean exploded =false;
    private Vector2 spawnPosition;

    public SelfDestructComponent(Entity target){
        this.target=target;
    }

    @Override
    public void create() {
        super.create();
        this.spawnPosition = new Vector2(entity.getPosition());
    }

    @Override
    public void update(){
        if(exploded) return;

        if(entity.getCenterPosition().dst(target.getCenterPosition()) <1f){
            explode();
        }
    }

    private void explode(){
        exploded=true;

        AnimationRenderComponent animator=entity.getComponent(AnimationRenderComponent.class);
        if(animator!=null){
            animator.startAnimation("explode");
        }

        target.getEvents().trigger("takeDamage",20);

        //reset after explosion animation duration
        float animationDuration =1f; //Replace with actual duration if needed
        entity.getEvents().trigger("SelfDestructComplete");
        resetAfterExplosion();
    }

    private void resetAfterExplosion(){
        //reset drone Position
        if(spawnPosition!=null){
            entity.setPosition(spawnPosition.cpy());
        }
        exploded =false;

        //reset animation to flying
        AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator != null) {
            animator.startAnimation("flying");
        }
    }
}

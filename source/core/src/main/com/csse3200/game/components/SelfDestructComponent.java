package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;

import javax.xml.namespace.QName;

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
    }
}

package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.PhysicsComponent;

import java.util.HashSet;
import java.util.Set;
public class MovingPlatformComponent extends Component {
    private Vector2 start;
    private Vector2 end;
    private final Vector2 offset;
    private final float speed;
    private boolean forward = true;
    private PhysicsComponent physics;
    private final Set<Entity> passengers = new HashSet<>();
    private Vector2 lastPos;

    public MovingPlatformComponent(Vector2 offset, float speed){
        this.offset = offset;
        this.speed = speed;
    }

    @Override
    public void create(){
        physics = entity.getComponent(PhysicsComponent.class);
        Vector2 pos = physics.getBody().getPosition().cpy();
        start = pos;
        end = pos.cpy().add(offset);
        lastPos=pos.cpy();
    }

    @Override
    public void update(){
        Body body = physics.getBody();
        Vector2 pos = body.getPosition();
        Vector2 target = forward ? end : start;
        Vector2 dir = target.cpy().sub(pos);

         if (dir.len() < 0.05f) {
             forward = !forward;
             dir.setZero();
        }else {
             dir.nor().scl(speed);
        }
        Vector2 delta = pos.cpy().sub(lastPos);
        for (Entity passenger : passengers){
            passenger.getComponent(PhysicsComponent.class)
                    .getBody()
                    .setTransform(passenger.getComponent(PhysicsComponent.class)
                            .getBody()
                            .getPosition()
                            .add(delta),0);
        }
        lastPos.set(pos);
        System.out.println("Pos: " + pos + " Target: " + target + " Vel: " + dir);
    }

    private void onCollisionStart(Object fixture, Object otherFixture) {
        Entity other = ((BodyUserData)((Fixture) otherFixture).getBody().getUserData()).entity;
        if (other!=null && other.getComponent(PlayerActions.class)!=null){
            passengers.add(other);
        }
    }
    private void onCollisionEnd(Object fixture, Object otherFixture) {
        Entity other = ((BodyUserData)((Fixture) otherFixture).getBody().getUserData()).entity;
        passengers.remove(other);
    }
}

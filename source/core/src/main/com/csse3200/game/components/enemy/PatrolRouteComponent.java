package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

public class PatrolRouteComponent extends Component {
    private final Vector2 spawnPos;
    private final Vector2[] steps;
    private Vector2[] waypoints;

    public PatrolRouteComponent(Vector2 spawnPos, Vector2[] steps) {
        this.spawnPos = new Vector2(spawnPos);
        this.steps = new Vector2[steps.length];
        for (int k = 0; k < steps.length; k++) this.steps[k] = new Vector2(steps[k]);
    }

    @Override
    public void create() {
        // Build array of cumulative waypoints from patrol steps
        if (steps.length == 0) {
            this.waypoints = new Vector2[] {new Vector2(spawnPos)};
            return;
        }

        waypoints = new Vector2[steps.length];
        Vector2 acc = new Vector2();
        for (int k = 0; k < steps.length; k++) {
            acc.add(steps[k]);
            waypoints[k] = new Vector2(spawnPos).add(acc);
        }
    }

    /**
     * Get number of waypoints in patrol route
     * @return number of waypoints in the patrol
     * @exception IllegalStateException if waypoints not initialised
     */
    public int numWaypoints() {
        if (waypoints == null) {
            throw new IllegalStateException("PatrolRouteComponent not initialised. Waypoints not set with create()");
        } else {
            return waypoints.length;
        }
    }

    /**
     * Get the first waypoint of the patrol route
     * @return first waypoint of the patrol
     * @exception IllegalStateException if waypoints not initialised
     */
    public Vector2 patrolStart() {
        if (waypoints == null) {
            throw new IllegalStateException("PatrolRouteComponent not initialised. Waypoints not set with create()");
        } else {
            return new Vector2(waypoints[0]);
        }
    }

    /**
     * Get the waypoint at a given index
     * @param i Index of desired waypoint
     * @return Vector2 corresponding to waypoint at the index
     * @exception IllegalStateException if waypoints not initialised
     * @exception IndexOutOfBoundsException if index outside bounds of waypoints
     */
    public Vector2 getWaypointAt(int i) {
        if (waypoints == null) {
            throw new IllegalStateException("PatrolRouteComponent not initialised. Waypoints not set with create()");
        }
        if (i < 0 || i >= waypoints.length) {
            throw new IndexOutOfBoundsException("Index out of bounds for waypoints");
        }
        return new Vector2(waypoints[i]);
    }

    /**
     * Get an array of cumulative waypoints built from the patrol steps.
     * @return an array of waypoints or null
     * @exception IllegalStateException if waypoints not initialised
     */
    public Vector2[] getWaypoints() {
        if (waypoints == null) {
            throw new IllegalStateException("PatrolRouteComponent not initialised. Waypoints not set with create()");
        }
        else return waypoints;
    }

    /**
     * Get the spawnPos used to build the waypoints
     * @return spawnPos
     */
    public Vector2 getSpawnPos() {
        return new Vector2(spawnPos);
    }
}

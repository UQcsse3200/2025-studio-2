package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

public class PatrolRouteComponent extends Component {
    private final Vector2[] waypoints;

    public PatrolRouteComponent(Vector2[] waypoints) {
        this.waypoints = new Vector2[waypoints.length];
        for (int k = 0; k < waypoints.length; k++) this.waypoints[k] = new Vector2(waypoints[k]);
    }

    /**
     * Get number of waypoints in patrol route
     * @return number of waypoints in the patrol
     */
    public int numWaypoints() {
        return waypoints.length;
    }

    /**
     * Get the first waypoint of the patrol route
     * @return first waypoint of the patrol
     */
    public Vector2 patrolStart() {
        return new Vector2(waypoints[0]);
    }

    /**
     * Get the waypoint at a given index
     * @param i Index of desired waypoint
     * @return Vector2 corresponding to waypoint at the index
     * @exception IndexOutOfBoundsException if index outside bounds of waypoints
     */
    public Vector2 getWaypointAt(int i) {
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
        return waypoints;
    }
}

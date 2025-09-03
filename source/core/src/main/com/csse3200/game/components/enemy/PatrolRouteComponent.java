package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

/**
 * A component that stores the patrol route for an enemy entity, which is represented
 * as an ordered list of waypoints.
 * This component should be added to any entity that is expected to perform patrol behaviour
 * (via PatrolTask) so the route can be queried.
 */
public class PatrolRouteComponent extends Component {
    private final Vector2[] waypoints;

    /**
     * Create a new PatrolRouteComponent with the supplied waypoints.
     * @param waypoints an array of world positions defining the patrol route
     */
    public PatrolRouteComponent(Vector2[] waypoints) {
        if (waypoints == null || waypoints.length == 0) {
            throw new IllegalArgumentException("Patrol route must have at least one waypoint");
        }

        this.waypoints = new Vector2[waypoints.length];
        for (int k = 0; k < waypoints.length; k++) {
            if (waypoints[k] == null) {
                throw new IllegalArgumentException("Patrol route must not have null waypoints");
            }
            this.waypoints[k] = new Vector2(waypoints[k]);
        }
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
        Vector2[] waypointsCpy = new Vector2[waypoints.length];
        for (int k = 0; k < waypoints.length; k++) waypointsCpy[k] = new Vector2(waypoints[k]);
        return waypointsCpy;
    }
}

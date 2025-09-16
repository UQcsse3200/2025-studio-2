
package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class PatrolRouteComponentTest {

    @Test
    void constructorRejectsEmptyWaypoints() {
        assertThrows(IllegalArgumentException.class,
                () -> new PatrolRouteComponent(new Vector2[0]),
                "Empty patrol routes should be rejected");
    }

    @Test
    void constructorRejectsNullWaypoints() {
        assertThrows(IllegalArgumentException.class,
                () -> new PatrolRouteComponent(null),
                "Null patrol routes should be rejected");
    }

    @Test
    void constructorRejectsNullWaypoint() {
        assertThrows(IllegalArgumentException.class,
                () -> new PatrolRouteComponent(new Vector2[]{null}),
                "Patrol routes with null elements should be rejected");
    }

    @Test
    void constructorCopiesWaypointsNotRefs() {
        Vector2[] points = {new Vector2(0, 0), new Vector2(1, 1), new Vector2(2, 2)};
        PatrolRouteComponent route = new PatrolRouteComponent(points);

        points[0].set(3, 3);
        points[1].set(4, 4);
        points[2].set(5, 5);

        for (int i = 0; i < points.length; i++) {
            assertEquals(new Vector2(i, i), route.getWaypointAt(i),
                    "Mutating the original route array  must not affect internal state");
        }
    }

    @Test
    void patrolStart_returnsCopy() {
        Vector2[] points = {new Vector2(0, 0), new Vector2(1, 0)};
        PatrolRouteComponent route = new PatrolRouteComponent(points);

        Vector2 start = route.patrolStart();
        start.set(1, 1);

        assertEquals(new Vector2(0, 0), route.patrolStart(),
                "Mutating the returned patrol start must not affect internal state");
    }

    @Test
    void patrolStart_equalsFirstWaypoint() {
        Vector2[] points = {new Vector2(0, 0), new Vector2(1, 1)};
        PatrolRouteComponent route = new PatrolRouteComponent(points);

        assertEquals(route.getWaypointAt(0), route.patrolStart(),
                "patrolStart shoud equal first waypoint");
    }

    @Test
    void getWaypointAt_returnsCopy() {
        Vector2[] points = {new Vector2(0, 0)};
        PatrolRouteComponent route = new PatrolRouteComponent(points);

        Vector2 wp0 = route.getWaypointAt(0);
        wp0.set(1, 1);

        assertEquals(new Vector2(0, 0), route.getWaypointAt(0),
                "Mutating the returned waypoint must not affect internal state");
    }

    @Test
    void getWaypoints_returnsCopy() {
        Vector2[] points = {new Vector2(0, 0)};
        PatrolRouteComponent route = new PatrolRouteComponent(points);

        Vector2[] leaked = route.getWaypoints();

        leaked[0].set(1, 1);

        assertNotEquals(points, route.getWaypoints(),
                "Mutating the returned route must not affect internal state");
    }

    @Test
    void getWaypointAt_throwsOutOfBounds() {
        Vector2[] points = {new Vector2(0, 0)};
        PatrolRouteComponent route = new PatrolRouteComponent(points);

        assertThrows(IndexOutOfBoundsException.class, () -> route.getWaypointAt(-1),
                "Should throw exception for index < 0");
        assertThrows(IndexOutOfBoundsException.class, () -> route.getWaypointAt(1),
                "Should throw exception for index >= waypoints.length");
    }

    @Test
    void numWaypoints_returnsCorrectLength() {
        Vector2 [] points = {new Vector2(0, 0)};
        PatrolRouteComponent route = new PatrolRouteComponent(points);

        assertEquals(1, route.numWaypoints(),
                "numWaypoints() should return correct number of waypoints");
    }

    @Test
    void getWaypoints_returnsFreshArray() {
        Vector2[] points = {new Vector2(0, 0), new Vector2(1, 0)};
        PatrolRouteComponent patrol = new PatrolRouteComponent(points);

        Vector2[] a = patrol.getWaypoints();
        Vector2[] b = patrol.getWaypoints();

        assertNotSame(a, b, "getWaypoints() should return fresh copies of waypoints");
    }
}

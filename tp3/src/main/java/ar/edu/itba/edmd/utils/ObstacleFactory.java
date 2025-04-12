package ar.edu.itba.edmd.utils;

import ar.edu.itba.edmd.structures.Obstacle;
import ar.edu.itba.edmd.structures.ObstacleType;
import ar.edu.itba.edmd.structures.Vector;

public class ObstacleFactory {

    public static Obstacle createStatic(double radius, double containerRadius) {
        Vector position = new Vector(0, 0);
        Vector velocity = new Vector(0, 0);
        return new Obstacle(position, velocity, radius, /*mass=*/1.0, ObstacleType.STATIC, -1);
    }

    public static Obstacle createMovable(double radius, double mass, double containerRadius) {
        Vector position = new Vector(0, 0);
        Vector velocity = new Vector(0, 0);
        return new Obstacle(position, velocity, radius, mass, ObstacleType.MOVABLE, -1);
    }
}

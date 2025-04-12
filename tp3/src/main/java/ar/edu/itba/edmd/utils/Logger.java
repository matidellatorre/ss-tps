package ar.edu.itba.edmd.utils;

import ar.edu.itba.edmd.structures.Obstacle;
import ar.edu.itba.edmd.structures.Particle;
import ar.edu.itba.edmd.structures.Vector;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class Logger {
    private final PrintWriter writer;
    private int eventCounter = 0;

    public Logger(String filename) throws IOException {
        this.writer = new PrintWriter(new FileWriter(filename));
    }

    public void logSnapshot(double time, List<Particle> particles, Obstacle obstacle) {
        writer.printf("e%d %.5f%n", eventCounter++, time);

        if (obstacle != null) {
            Vector pos = obstacle.getPosition();
            Vector vel = obstacle.getVelocity();
            writer.printf("p0 %.6f %.6f %.6f %.6f%n",
                    pos.getx(), pos.gety(),
                    vel.getx(), vel.gety());
        }

        for (Particle p : particles) {
            Vector pos = p.getPosition();
            Vector vel = p.getVelocity();
            writer.printf("p%d %.6f %.6f %.6f %.6f%n", p.getId(),
                    pos.getx(), pos.gety(),
                    vel.getx(), vel.gety());
        }
    }

    public void close() {
        writer.close();
    }
}
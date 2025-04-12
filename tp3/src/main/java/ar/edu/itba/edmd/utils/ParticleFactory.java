package ar.edu.itba.edmd.utils;

import ar.edu.itba.edmd.structures.Particle;
import ar.edu.itba.edmd.structures.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleFactory {

    public static List<Particle> generateParticles(
            int N,
            double v0,
            double radius,
            double mass,
            double containerRadius,
            double obstacleRadius
    ) {
        List<Particle> particles = new ArrayList<>();
        Random rand = new Random();
        int idCounter = 1;

        int attempts = 0;
        while (particles.size() < N && attempts < N * 1000) {
            // Pick a position in polar coordinates (r, theta)
            double rMin = obstacleRadius + radius;
            double rMax = containerRadius - radius;
            double r = Math.sqrt(rand.nextDouble()) * (rMax - rMin) + rMin;  // uniform over area
            double theta = 2 * Math.PI * rand.nextDouble();

            double x = r * Math.cos(theta);
            double y = r * Math.sin(theta);
            Vector position = new Vector(x, y);

            // Check against other particles
            boolean overlaps = false;
            for (Particle p : particles) {
                if (p.getPosition().distanceTo(position) < 2 * radius) {
                    overlaps = true;
                    break;
                }
            }

            if (overlaps) {
                attempts++;
                continue;
            }

            // Random velocity direction
            double angle = 2 * Math.PI * rand.nextDouble();
            Vector velocity = new Vector(Math.cos(angle), Math.sin(angle)).scale(v0);

            particles.add(new Particle(position, velocity,idCounter++, radius, mass));
        }

        if (particles.size() < N) {
            throw new RuntimeException("Could not place all particles without overlap.");
        }

        return particles;
    }

}
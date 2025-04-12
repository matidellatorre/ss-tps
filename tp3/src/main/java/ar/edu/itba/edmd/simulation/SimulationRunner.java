package ar.edu.itba.edmd.simulation;

import ar.edu.itba.edmd.structures.Obstacle;
import ar.edu.itba.edmd.structures.Particle;
import ar.edu.itba.edmd.utils.Logger;
import ar.edu.itba.edmd.utils.ObstacleFactory;
import ar.edu.itba.edmd.utils.ParticleFactory;

import java.io.IOException;
import java.util.List;

public class SimulationRunner {
    public static void run(SimulationConfig config) {
        try {
            // === Particle creation ===
            List<Particle> particles = ParticleFactory.generateParticles(
                    config.numParticles,
                    config.initialVelocity,
                    config.particleRadius,
                    config.particleMass,
                    config.getContainerRadius(),
                    config.obstacleRadius
            );

            // === Obstacle creation ===
            Obstacle obstacle = switch (config.obstacleType) {
                case STATIC -> ObstacleFactory.createStatic(
                        config.obstacleRadius,
                        config.getContainerRadius()
                );
                case MOVABLE -> ObstacleFactory.createMovable(
                        config.obstacleRadius,
                        config.obstacleMass,
                        config.getContainerRadius()
                );
            };

            // === Logger ===
            Logger logger = new Logger(config.outputFile);

            // === Simulation ===
            Simulation sim = new Simulation(particles, obstacle, config.containerDiameter, logger);
            sim.simulate(config.totalTime);

        } catch (IOException e) {
            System.err.println("Error during simulation: " + e.getMessage());
        }
    }
}

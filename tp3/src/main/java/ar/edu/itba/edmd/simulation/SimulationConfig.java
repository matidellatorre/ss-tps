package ar.edu.itba.edmd.simulation;

import ar.edu.itba.edmd.structures.ObstacleType;

public class SimulationConfig {
    public final double containerDiameter;
    public final int numParticles;
    public final double particleRadius;
    public final double particleMass;
    public final double obstacleRadius;
    public final double obstacleMass;
    public final ObstacleType obstacleType;
    public final double initialVelocity;  // v0
    public final double totalTime;
    public final String outputFile;

    public SimulationConfig(
            double containerDiameter,
            int numParticles,
            double particleRadius,
            double particleMass,
            double obstacleRadius,
            double obstacleMass,
            ObstacleType obstacleType,
            double initialVelocity,
            double totalTime,
            String outputFile
    ) {
        this.containerDiameter = containerDiameter;
        this.numParticles = numParticles;
        this.particleRadius = particleRadius;
        this.particleMass = particleMass;
        this.obstacleRadius = obstacleRadius;
        this.obstacleMass = obstacleMass;
        this.obstacleType = obstacleType;
        this.initialVelocity = initialVelocity;
        this.totalTime = totalTime;
        this.outputFile = outputFile;
    }

    public double getContainerRadius() {
        return containerDiameter / 2.0;
    }
}

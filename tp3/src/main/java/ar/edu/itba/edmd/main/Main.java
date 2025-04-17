package ar.edu.itba.edmd.main;

import ar.edu.itba.edmd.simulation.SimulationConfig;
import ar.edu.itba.edmd.simulation.SimulationRunner;
import ar.edu.itba.edmd.structures.ObstacleType;

public class Main {
    public static void main(String[] args) {
        SimulationConfig config = new SimulationConfig(
                0.1,       // container diameter
                210,       // N particles
                0.0005,    // particle radius
                1.0,       // particle mass
                0.005,     // obstacle radius
                3.0,       // obstacle mass (only used if MOVABLE)
                ObstacleType.STATIC,  // or MOVABLE
                1.0,        // initial v0
                100.0,      // total time
                "src/results/output_1.1.txt"
        );

        SimulationRunner.run(config);
    }
}

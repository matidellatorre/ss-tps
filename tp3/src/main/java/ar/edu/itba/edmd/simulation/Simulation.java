package ar.edu.itba.edmd.simulation;

import ar.edu.itba.edmd.events.Event;
import ar.edu.itba.edmd.events.EventQueue;
import ar.edu.itba.edmd.events.EventType;
import ar.edu.itba.edmd.structures.Obstacle;
import ar.edu.itba.edmd.structures.Particle;
import ar.edu.itba.edmd.utils.Logger;

import java.util.List;

public class Simulation {

    private final List<Particle> particles;
    private final Obstacle obstacle;
    private final EventQueue eventQueue;
    private double time;

    private final double L;
    private final Logger logger;
    private final double updateInterval = 1.0 / 60.0; // PARA RERENDERIZAR el dibujo

    public Simulation(List<Particle> particles, final Obstacle obstacle, final double radius, final Logger logger) {
        this.particles = particles;
        this.obstacle = obstacle;
        this.eventQueue = new EventQueue();
        this.time = 0.0;
        this.L = radius;
        this.logger = logger;
    }

    public void simulate(double limit) {
        // Schedule initial events
        for (Particle p : particles) {
            predict(p);
        }
        predict(obstacle);
        scheduleUpdate();

        // Main simulation loop
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            if (!event.isValid()) continue;

            Particle a = event.getA();
            Particle b = event.getB();

            // Advance time
            double dt = event.getTime() - time;
            for (Particle p : particles) p.move(dt);
            obstacle.move(dt);
            time = event.getTime();

            // Execute the event
            switch (event.getType()) {
                case PARTICLE_PARTICLE  -> a.bounceOff(b);
                case WALL_CIRCULAR      -> a.bounceOffCircularBoundary();
                case OBSTACLE           -> a.bounceOff(b);
                case UPDATE -> {
                    logState(); // or render
                    scheduleUpdate();
                }
            }

            // Predict new events for the involved particles
            if (a != null) predict(a);
            if (b != null) predict(b);
        }
        logger.close();
    }

    private void predict(Particle p) {
        if (p == null) return;

        // wall collision
        double tBoundary = p.timeToHitCircularBoundary(L / 2.0);
        if (time + tBoundary <= Double.POSITIVE_INFINITY) {
            eventQueue.add(new Event(time + tBoundary, p, null, EventType.WALL_CIRCULAR));
        }

        // Predict particle-particle collisions
        for (Particle other : particles) {
            if (p == other) continue;
            double t = p.timeToHit(other);
            if (time + t <= Double.POSITIVE_INFINITY)
                eventQueue.add(new Event(time + t, p, other, EventType.PARTICLE_PARTICLE));
        }

        // Predict particle-obstacle collisions
        if (p != obstacle) {
            double t = p.timeToHit(obstacle);
            if (time + t <= Double.POSITIVE_INFINITY)
                eventQueue.add(new Event(time + t, p, obstacle, EventType.OBSTACLE));
        }
    }

    private void scheduleUpdate() {
        eventQueue.add(new Event(time + updateInterval, null, null, EventType.UPDATE));
    }

    private void logState() {
        logger.logSnapshot(time, particles, obstacle);
    }

}

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
    private final double updateInterval = 1.0 / 10.0; // PARA RERENDERIZAR el dibujo

    public Simulation(List<Particle> particles, final Obstacle obstacle, final double containerDiameter, final Logger logger) {
        this.particles = particles;
        this.obstacle = obstacle;
        this.eventQueue = new EventQueue();
        this.time = 0.0;
        this.L = containerDiameter;
        this.logger = logger;
    }

    public void simulate(double limit) {
        // Schedule initial events
        for (Particle p : particles) {
            predict(p);
        }
        predict(obstacle);
        //scheduleUpdate();

        logState();
        // Main simulation loop
        while (!eventQueue.isEmpty() && time < limit) {
            Event event = eventQueue.poll();
            if (!event.isValid()) continue;

            Particle a = event.getA();
            Particle b = event.getB();

            // Advance time
            if (!Double.isFinite(event.getTime()) || !Double.isFinite(time)) {
                System.err.printf("⚠️ Skipping invalid event at time %.6f (event time %.6f)%n", time, event.getTime());
                continue;
            }

            double dt = event.getTime() - time;

            if (dt < 0 || Double.isNaN(dt)) {
                System.err.printf("⚠️ Invalid dt=%.6f at time=%.6f for event=%.6f%n", dt, time, event.getTime());
                continue;
            }

            for (Particle p : particles) p.move(dt);

            for (int i = 0; i < particles.size(); i++) {
                for (int j = i + 1; j < particles.size(); j++) {
                    Particle p1 = particles.get(i);
                    Particle p2 = particles.get(j);
                    double dist = p1.getPosition().distanceTo(p2.getPosition());
                    if (dist < p1.getRadius() + p2.getRadius() - 1e-6) {
                        System.err.printf("⚠️ Overlap: p%d and p%d at time %.4f | dist=%.6f\n",
                                p1.getId(), p2.getId(), time, dist);
                    }
                }
            }

            obstacle.move(dt);
            time = event.getTime();

            // Execute the event
            switch (event.getType()) {
                case PARTICLE_PARTICLE  -> a.bounceOff(b);
                case WALL_CIRCULAR      -> a.bounceOffCircularBoundary(L/2);
                case OBSTACLE           -> {
                    if(a == obstacle) {
                        b.bounceOff(a);
                    } else {
                        a.bounceOff(b);
                    }
                }
                case UPDATE -> {
                    //logState(); // or render
                    //scheduleUpdate();
                }
            }
            logState();
            // Predict new events for the involved particles
            if (a != null) predict(a);
            if (b != null) predict(b);

            for (Particle p : particles) {
                double r = p.getPosition().magnitude();
                if (r > (L / 2.0 - p.getRadius())) {
                    System.err.printf("⚠️ Particle %d escaped container: r = %.6f > max = %.6f at t = %.4f\n In event: %s\n",
                            p.getId(), r, (L / 2.0 - p.getRadius()), time, event.getType());
                }
            }

        }
        logger.close();
    }

    private void predict(Particle p) {
        if (p == null) return;

        // wall collision
        double tBoundary = p.timeToHitCircularBoundary(L / 2.0);
        if (tBoundary>0 && Double.isFinite(tBoundary) && Double.isFinite(time + tBoundary)) {
            eventQueue.add(new Event(time + tBoundary, p, null, EventType.WALL_CIRCULAR));
        }

        // Predict particle-particle collisions
        for (Particle other : particles) {
            if (p == other || p.getId() >= other.getId()) continue;
            double t = p.timeToHit(other);
            if (t > 1e-10 && Double.isFinite(t) && Double.isFinite(time + t))
                eventQueue.add(new Event(time + t, p, other, EventType.PARTICLE_PARTICLE));
        }

        // Predict particle-obstacle collisions
        if (p != obstacle) {
            double t = p.timeToHit(obstacle);
            if (t > 0 && Double.isFinite(t) && Double.isFinite(time + t))
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

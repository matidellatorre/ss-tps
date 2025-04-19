package ar.edu.itba.edmd.simulation;

import ar.edu.itba.edmd.events.Event;
import ar.edu.itba.edmd.events.EventQueue;
import ar.edu.itba.edmd.events.EventType;
import ar.edu.itba.edmd.structures.Obstacle;
import ar.edu.itba.edmd.structures.Particle;
import ar.edu.itba.edmd.structures.Vector;
import ar.edu.itba.edmd.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class Simulation {

    private final List<Particle> particles;
    private final Obstacle obstacle;
    private final EventQueue eventQueue;
    private double time;

    private final double L;
    private final Logger logger;
    private final double updateInterval = 1.0 / 10.0; // PARA RERENDERIZAR el dibujo
    private final double OVERLAP_TOLERANCE = 1e-6; // Increased tolerance for numerical precision issues
    private final double SAFETY_FACTOR = 1.2; // Factor to apply when correcting positions

    public Simulation(List<Particle> particles, final Obstacle obstacle, final double containerDiameter, final Logger logger) {
        this.particles = particles;
        this.obstacle = obstacle;
        this.eventQueue = new EventQueue();
        this.time = 0.0;
        this.L = containerDiameter;
        this.logger = logger;
        
        // Verify no initial overlaps and fix them if found
        validateAndFixInitialState();
    }

    private void validateAndFixInitialState() {
        boolean needsRecheck;
        int maxIterations = 5; // Avoid infinite loop
        int iterations = 0;
        
        do {
            needsRecheck = false;
            iterations++;
            
            // Check and fix particle-particle overlaps
            for (int i = 0; i < particles.size(); i++) {
                for (int j = i + 1; j < particles.size(); j++) {
                    Particle p1 = particles.get(i);
                    Particle p2 = particles.get(j);
                    double dist = p1.getPosition().distanceTo(p2.getPosition());
                    double minDist = p1.getRadius() + p2.getRadius();
                    
                    if (dist < minDist - OVERLAP_TOLERANCE) {
                        System.err.printf("⚠️ Initial overlap detected: p%d and p%d | dist=%.6f, min required=%.6f\n",
                                p1.getId(), p2.getId(), dist, minDist);
                        
                        // Strong correction to ensure they are properly separated
                        double overlap = (minDist - dist + OVERLAP_TOLERANCE) * SAFETY_FACTOR;
                        correctOverlap(p1, p2, dist, minDist + overlap);
                        needsRecheck = true;
                    }
                }
                
                // Check and fix particle-boundary overlaps
                double distToCenter = particles.get(i).getPosition().magnitude();
                double maxDist = (L / 2.0) - particles.get(i).getRadius();
                
                if (distToCenter > maxDist) {
                    System.err.printf("⚠️ Initial particle %d outside boundary: dist=%.6f, max=%.6f\n",
                            particles.get(i).getId(), distToCenter, maxDist);
                    
                    // Strong correction to move particle well inside boundary
                    correctBoundaryViolation(particles.get(i), distToCenter, maxDist - OVERLAP_TOLERANCE);
                    needsRecheck = true;
                }
                
                // Check and fix particle-obstacle overlaps
                if (obstacle != null) {
                    double distToObstacle = particles.get(i).getPosition().distanceTo(obstacle.getPosition());
                    double minObstacleDist = particles.get(i).getRadius() + obstacle.getRadius();
                    
                    if (distToObstacle < minObstacleDist - OVERLAP_TOLERANCE) {
                        System.err.printf("⚠️ Initial particle-obstacle overlap: p%d | dist=%.6f, min required=%.6f\n",
                                particles.get(i).getId(), distToObstacle, minObstacleDist);
                        
                        // Get vector from obstacle to particle
                        double dx = particles.get(i).getPosition().getx() - obstacle.getPosition().getx();
                        double dy = particles.get(i).getPosition().gety() - obstacle.getPosition().gety();
                        double distance = Math.sqrt(dx*dx + dy*dy);
                        
                        if (distance > 1e-10) {
                            double ux = dx / distance;
                            double uy = dy / distance;
                            double correctionAmount = (minObstacleDist - distToObstacle + OVERLAP_TOLERANCE) * SAFETY_FACTOR;
                            particles.get(i).correctPosition(ux * correctionAmount, uy * correctionAmount);
                            needsRecheck = true;
                        }
                    }
                }
            }
        } while (needsRecheck && iterations < maxIterations);
        
        if (iterations >= maxIterations) {
            System.err.println("⚠️ WARNING: Failed to completely resolve initial overlaps after " + iterations + " attempts");
        }
    }

    public void simulate(double limit) {
        // Schedule initial events
        for (Particle p : particles) predict(p);
        predict(obstacle);
        logState();

        // Main simulation loop
        while (!eventQueue.isEmpty() && time < limit) {
            Event e = eventQueue.poll();
            if (!e.isValid()) continue;

            double nextTime = e.getTime();

            // Skip invalid times
            if (!Double.isFinite(nextTime) || !Double.isFinite(time)) {
                System.err.printf("⚠️ Skipping invalid event at time %.6f (event time %.6f)%n", time, nextTime);
                continue;
            }

            double dt = nextTime - time;
            if (dt < 0 || Double.isNaN(dt)) {
                System.err.printf("⚠️ Invalid dt=%.6f at time=%.6f for event=%.6f%n", dt, time, nextTime);
                continue;
            }

            // ✅ Move all particles only once
            for (Particle p : particles) p.move(dt);
            obstacle.move(dt);
            time = nextTime;

            // 🔁 Gather all events with the same timestamp
            List<Event> batch = new ArrayList<>();
            batch.add(e);
            while (!eventQueue.isEmpty() && Math.abs(eventQueue.peek().getTime() - time) < 1e-10) {
                batch.add(eventQueue.poll());
            }

            // Before processing events, check and fix any overlaps that may have occurred during movement
            checkAndFixAllOverlaps();
            
            // ✅ Process batch of simultaneous events
            for (Event event : batch) {
                if (!event.isValid()) continue;

                Particle a = event.getA();
                Particle b = event.getB();

                switch (event.getType()) {
                    case PARTICLE_PARTICLE  -> {
                        a.bounceOff(b);
                        // Verify post-collision state and add velocity separation if needed
                        ensureParticlesMovingApart(a, b);
                    }
                    case WALL_CIRCULAR      -> {
                        a.bounceOffCircularBoundary(L / 2);
                        verifyParticleInBounds(a);
                    }
                    case OBSTACLE           -> {
                        if (a == obstacle) {
                            b.bounceOff(a);
                            ensureParticlesMovingApart(b, a);
                        } else {
                            a.bounceOff(b);
                            ensureParticlesMovingApart(a, b);
                        }
                    }
                    case UPDATE -> {
                        // logState(); // or render
                        // scheduleUpdate();
                    }
                }

                logState(); // optional: track state after each event

                // Re-predict events for affected particles
                if (a != null) predict(a);
                if (b != null) predict(b);
            }

            // ✅ Post-frame diagnostics to detect and fix any remaining overlaps
            checkAndFixAllOverlaps();
        }

        logger.close();
    }

    private void ensureParticlesMovingApart(Particle p1, Particle p2) {
        // First check if there's an overlap
        verifyNoOverlap(p1, p2);
        
        // Next, make sure particles are moving away from each other
        Vector deltaV = p2.getVelocity().subtract(p1.getVelocity());
        Vector deltaR = p2.getPosition().subtract(p1.getPosition());
        double dvdr = deltaV.dot(deltaR);
        
        if (dvdr < 0) {
            // Particles moving towards each other after collision - this shouldn't happen
            // Add a small velocity component to ensure separation
            double dist = deltaR.magnitude();
            if (dist > 1e-10) {
                double ux = deltaR.getx() / dist;  // Unit vector from p1 to p2
                double uy = deltaR.gety() / dist;
                
                // Adjust velocities to ensure separation
                double separationSpeed = 0.01 * (p1.getVelocity().magnitude() + p2.getVelocity().magnitude());
                p1.setVelocity(p1.getVelocity().add(new Vector(-ux * separationSpeed, -uy * separationSpeed)));
                p2.setVelocity(p2.getVelocity().add(new Vector(ux * separationSpeed, uy * separationSpeed)));
                
                System.err.printf("⚠️ Added separation velocity between p%d and p%d at time %.4f\n", 
                    p1.getId(), p2.getId(), time);
            }
        }
    }

    private void verifyNoOverlap(Particle p1, Particle p2) {
        double dist = p1.getPosition().distanceTo(p2.getPosition());
        double minDist = p1.getRadius() + p2.getRadius();
        
        if (dist < minDist - OVERLAP_TOLERANCE) {
            System.err.printf("⚠️ Post-collision overlap: p%d and p%d at time %.4f | dist=%.6f, min required=%.6f\n",
                    p1.getId(), p2.getId(), time, dist, minDist);
            
            // Stronger correction with safety factor to prevent repeated overlaps
            correctOverlap(p1, p2, dist, minDist + OVERLAP_TOLERANCE * SAFETY_FACTOR);
        }
    }
    
    private void verifyParticleInBounds(Particle p) {
        double distToCenter = p.getPosition().magnitude();
        double maxDist = (L / 2.0) - p.getRadius();
        
        if (distToCenter > maxDist) {
            System.err.printf("⚠️ Particle %d outside boundary after collision: dist=%.6f, max=%.6f at t=%.4f\n",
                    p.getId(), distToCenter, maxDist, time);
            
            // Emergency correction with safety factor
            correctBoundaryViolation(p, distToCenter, maxDist - OVERLAP_TOLERANCE);
            
            // Also check if velocity is correct (pointing inward)
            Vector pos = p.getPosition();
            Vector vel = p.getVelocity();
            if (pos.dot(vel) > 0) {
                // Velocity pointing outward, reverse it
                p.setVelocity(vel.scale(-1.0));
                System.err.printf("⚠️ Corrected outward velocity for particle %d at t=%.4f\n", p.getId(), time);
            }
        }
    }
    
    private void correctOverlap(Particle p1, Particle p2, double currentDist, double targetDist) {
        // Move particles apart along their center line with extra safety margin
        double correctionAmount = (targetDist - currentDist);
        
        // Get unit vector from p1 to p2
        double dx = p2.getPosition().getx() - p1.getPosition().getx();
        double dy = p2.getPosition().gety() - p1.getPosition().gety();
        double distance = Math.sqrt(dx*dx + dy*dy);
        
        if (distance > 1e-10) {
            double ux = dx / distance;
            double uy = dy / distance;
            
            // Move p1 away from p2
            p1.correctPosition(-ux * correctionAmount / 2.0, -uy * correctionAmount / 2.0);
            
            // Move p2 away from p1
            p2.correctPosition(ux * correctionAmount / 2.0, uy * correctionAmount / 2.0);
            
            System.err.printf("Corrected overlap between p%d and p%d by %.6f units\n", 
                p1.getId(), p2.getId(), correctionAmount);
        }
    }
    
    private void correctBoundaryViolation(Particle p, double currentDist, double targetDist) {
        // Move particle back inside boundary with safety margin
        double x = p.getPosition().getx();
        double y = p.getPosition().gety();
        double magnitude = Math.sqrt(x*x + y*y);
        
        if (magnitude > 1e-10) {
            double correctionAmount = (currentDist - targetDist);
            double ux = x / magnitude;
            double uy = y / magnitude;
            
            p.correctPosition(-ux * correctionAmount, -uy * correctionAmount);
            System.err.printf("Pushed particle %d inside boundary by %.6f units\n", 
                p.getId(), correctionAmount);
        }
    }
    
    private void checkAndFixAllOverlaps() {
        boolean needsMoreFixes = false;
        int maxPasses = 3;
        int passes = 0;
        
        do {
            needsMoreFixes = false;
            passes++;
            
            // Check particle-particle overlaps
            for (int i = 0; i < particles.size(); i++) {
                for (int j = i + 1; j < particles.size(); j++) {
                    Particle p1 = particles.get(i);
                    Particle p2 = particles.get(j);
                    double dist = p1.getPosition().distanceTo(p2.getPosition());
                    double minDist = p1.getRadius() + p2.getRadius();
                    
                    if (dist < minDist - OVERLAP_TOLERANCE) {
                        System.err.printf("⚠️ Overlap detected (pass %d): p%d and p%d at time %.4f | dist=%.6f, min=%.6f\n",
                                passes, p1.getId(), p2.getId(), time, dist, minDist);
                        
                        // Apply stronger correction each pass
                        double extraMargin = OVERLAP_TOLERANCE * (passes * SAFETY_FACTOR);
                        correctOverlap(p1, p2, dist, minDist + extraMargin);
                        needsMoreFixes = true;
                    }
                }
                
                // Check particle-boundary overlap
                double distToCenter = particles.get(i).getPosition().magnitude();
                double maxDist = (L / 2.0) - particles.get(i).getRadius();
                
                if (distToCenter > maxDist) {
                    System.err.printf("⚠️ Particle %d outside boundary (pass %d): dist=%.6f, max=%.6f at t=%.4f\n",
                            particles.get(i).getId(), passes, distToCenter, maxDist, time);
                    
                    // Apply stronger correction each pass
                    double extraMargin = OVERLAP_TOLERANCE * passes;
                    correctBoundaryViolation(particles.get(i), distToCenter, maxDist - extraMargin);
                    needsMoreFixes = true;
                    
                    // Also ensure velocity points inward
                    Vector pos = particles.get(i).getPosition();
                    Vector vel = particles.get(i).getVelocity();
                    if (pos.dot(vel) > 0) {
                        // Velocity pointing outward, reverse it
                        particles.get(i).setVelocity(vel.scale(-1.0));
                    }
                }
                
                // Check particle-obstacle overlap
                if (obstacle != null) {
                    double distToObstacle = particles.get(i).getPosition().distanceTo(obstacle.getPosition());
                    double minObstacleDist = particles.get(i).getRadius() + obstacle.getRadius();
                    
                    if (distToObstacle < minObstacleDist - OVERLAP_TOLERANCE) {
                        System.err.printf("⚠️ Particle-obstacle overlap (pass %d): p%d at time %.4f | dist=%.6f, min=%.6f\n",
                                passes, particles.get(i).getId(), time, distToObstacle, minObstacleDist);
                        
                        // Get vector from obstacle to particle
                        double dx = particles.get(i).getPosition().getx() - obstacle.getPosition().getx();
                        double dy = particles.get(i).getPosition().gety() - obstacle.getPosition().gety();
                        double distance = Math.sqrt(dx*dx + dy*dy);
                        
                        if (distance > 1e-10) {
                            double ux = dx / distance;
                            double uy = dy / distance;
                            double extraMargin = OVERLAP_TOLERANCE * (passes * SAFETY_FACTOR);
                            double correctionAmount = (minObstacleDist - distToObstacle + extraMargin);
                            particles.get(i).correctPosition(ux * correctionAmount, uy * correctionAmount);
                            needsMoreFixes = true;
                        }
                    }
                }
            }
        } while (needsMoreFixes && passes < maxPasses);
        
        if (passes == maxPasses && needsMoreFixes) {
            System.err.println("⚠️ WARNING: Could not completely resolve all overlaps after " + passes + " passes");
        }
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

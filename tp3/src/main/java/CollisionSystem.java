import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Random;

/** Core simulation: event-driven loop. */
public class CollisionSystem {
    private Particle[] particles;
    private PriorityQueue<Event> pq;
    private double time = 0.0;
    private final double containerRadius;
    private final double obstacleRadius;

    public CollisionSystem(Particle[] particles, double L, double R_obs) {
        this.particles = particles;
        this.containerRadius = L/2.0;
        this.obstacleRadius = R_obs;
        this.pq = new PriorityQueue<>();
    }

    // Predict future events for particle a
    private void predict(Particle a) {
        if (a == null) return;
        // particle-particle
        for (Particle b : particles) {
            double dt = a.timeToHit(b);
            if (dt < Double.POSITIVE_INFINITY) pq.add(new Event(time + dt, a, b));
        }
        // particle-wall
        double dtWall = a.timeToHitWall(containerRadius);
        if (dtWall < Double.POSITIVE_INFINITY) pq.add(new Event(time + dtWall, a, null));
        // particle-obstacle
        double dtObs = a.timeToHitObstacle(obstacleRadius);
        if (dtObs < Double.POSITIVE_INFINITY) pq.add(new Event(time + dtObs, a, a));
    }

    // Initialize event queue
    private void initPQ() {
        pq.clear();
        for (Particle p : particles) {
            predict(p);
        }
        // Redraw / record event marker
        pq.add(new Event(time, null, null));
    }

    // Simulate until processedevents events, recording at each
    public void simulate(int maxEvents, String outputFile, int recordEvery) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        initPQ();
        int eventsProcessed = 0;
        while (eventsProcessed < maxEvents && !pq.isEmpty()) {
            Event e = pq.poll();
            if (!e.isValid()) continue;

            // Move all particles to event time
            for (Particle p : particles) {
                p.move(e.time - time);
            }
            time = e.time;

            // Process event
            if      (e.a != null && e.b != null && e.a != e.b) e.a.bounceOff(e.b);
            else if (e.a != null && e.b == null)             e.a.bounceOffWall();
            else if (e.a != null && e.b == e.a)               e.a.bounceOffObstacle(obstacleRadius);
            // else: record-only event marker

            // Re-predict for affected particles
            if (e.a != null) predict(e.a);
            if (e.b != null && e.b != e.a) predict(e.b);

            // record state
            if (e.a == null && e.b == null || eventsProcessed % recordEvery == 0) {
                writer.write(String.format("e%d %.6f\n", eventsProcessed+1, time));
                for (int i = 0; i < particles.length; i++) {
                    Particle p = particles[i];
                    writer.write(String.format("p%d %.6f %.6f %.6f %.6f\n",
                            i+1, p.x, p.y, p.vx, p.vy));
                }
            }
            eventsProcessed++;
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        int N = 200;
        double L = 0.1;
        double R_obs = 0.005;
        double r = 5e-4;
        double m = 1.0;
        double v0 = 1.0;
        int N_events = 10000;
        int recordEvery = 1;

        Particle[] particles = new Particle[N];
        Random rand = new Random();

        double containerLimit = L/2 - r;
        double obstacleLimitSq = (R_obs + r) * (R_obs + r);

        for (int i = 0; i < N; i++) {
            boolean placed = false;
            while (!placed) {
                double x = (rand.nextDouble()*2 - 1) * containerLimit;
                double y = (rand.nextDouble()*2 - 1) * containerLimit;
                double distSq = x*x + y*y;
                // inside container?
                if (distSq > containerLimit*containerLimit) continue;
                // outside obstacle?
                if (distSq < obstacleLimitSq) continue;
                boolean ok = true;
                for (int j = 0; j < i; j++) {
                    double dx = x - particles[j].x;
                    double dy = y - particles[j].y;
                    if (dx*dx + dy*dy < 4*r*r) { ok = false; break; }
                }
                if (!ok) continue;
                double theta = 2*Math.PI*rand.nextDouble();
                double vx = v0*Math.cos(theta);
                double vy = v0*Math.sin(theta);
                particles[i] = new Particle(x, y, vx, vy, r, m);
                placed = true;
            }
        }

        CollisionSystem sim = new CollisionSystem(particles, L, R_obs);
        sim.simulate(N_events, "simulation.txt", recordEvery);
    }
}
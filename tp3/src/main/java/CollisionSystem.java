import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class CollisionSystem {
    private Particle[] particles;
    private PriorityQueue<Event> pq;
    private double time = 0.0;
    private final double containerRadius;
    private final double obstacleRadius;
    private Set<Particle> hasCollidedWithObstacle;  // Track first-time collisions

    public CollisionSystem(Particle[] particles, double L, double R_obs) {
        this.particles = particles;
        this.containerRadius = L/2.0;
        this.obstacleRadius = R_obs;
        this.pq = new PriorityQueue<>();
        this.hasCollidedWithObstacle = new HashSet<>();
    }

    private void predict(Particle a) {
        if (a == null) return;
        for (Particle b : particles) {
            double dt = a.timeToHit(b);
            if (dt < Double.POSITIVE_INFINITY) pq.add(new Event(time + dt, a, b));
        }
        double dtWall = a.timeToHitWall(containerRadius);
        if (dtWall < Double.POSITIVE_INFINITY) pq.add(new Event(time + dtWall, a, null));
        double dtObs = a.timeToHitObstacle(obstacleRadius);
        if (dtObs < Double.POSITIVE_INFINITY) pq.add(new Event(time + dtObs, a, a));
    }

    private void initPQ() {
        pq.clear();
        for (Particle p : particles) predict(p);
        pq.add(new Event(time, null, null));
    }

    /**
     * Runs the simulation, recording state and pressure
     */
    public void simulate(double maxTime, String outputFile, int recordEvery, double v0) throws IOException {
        BufferedWriter stateWriter = new BufferedWriter(new FileWriter(outputFile));
        BufferedWriter pressureWriter = new BufferedWriter(new FileWriter("./results/pressure_time_v"+v0+".txt"));
        BufferedWriter collisionWriter = new BufferedWriter(new FileWriter("./results/collisions_count_v"+v0+".txt"));
        // header for pressure file
        pressureWriter.write("time pressure_walls pressure_obstacle\n");
        collisionWriter.write("event first_obstacle_col all_obstacle_col time\n");

        double interval = 0.01;
        double nextPressureTime = interval;
        int wallHits = 0;
        double wallDeltaPSum = 0.0;
        int obsHits = 0;
        double obsDeltaPSum = 0.0;
        int firstTimeObstacleCollisions = 0;
        int totalObstacleCollisions = 0;

        double containerPerimeter = 2 * Math.PI * containerRadius;
        double obstaclePerimeter  = 2 * Math.PI * obstacleRadius;

        initPQ();
        int eventsProcessed = 0;

        while (time < maxTime && !pq.isEmpty()) {
            Event e = pq.poll();
            if (!e.isValid()) continue;

            // move to next event time
            for (Particle p : particles) p.move(e.time - time);
            time = e.time;

            // process collision and accumulate Δp
            if (e.a != null && e.b == null) {
                // wall hit
                double dp = e.a.bounceOffWall();
                wallHits++;
                wallDeltaPSum += dp;
            }
            else if (e.a != null && e.b == e.a) {
                // obstacle hit
                double dp = e.a.bounceOffObstacle();
                obsHits++;
                obsDeltaPSum += dp;
                totalObstacleCollisions++;
                
                // Check if this is the first collision for this particle
                if (!hasCollidedWithObstacle.contains(e.a)) {
                    hasCollidedWithObstacle.add(e.a);
                    firstTimeObstacleCollisions++;
                }
            }
            else if (e.a != null && e.b != null && e.a != e.b) {
                e.a.bounceOff(e.b);
            }

            // re-predict for affected particles
            if (e.a != null) predict(e.a);
            if (e.b != null && e.b != e.a) predict(e.b);

            // record state if needed
            if ((e.a == null && e.b == null) || eventsProcessed % recordEvery == 0) {
                stateWriter.write(String.format("e%d %.6f\n", eventsProcessed+1, time));
                for (int i = 0; i < particles.length; i++) {
                    Particle p = particles[i];
                    stateWriter.write(String.format("p%d %.6f %.6f %.6f %.6f\n",
                            i+1, p.x, p.y, p.vx, p.vy));
                }
                collisionWriter.write(String.format("e%d %d %d %.6f\n", 
                    eventsProcessed+1, firstTimeObstacleCollisions, totalObstacleCollisions, time));
            }
            eventsProcessed++;

            // output pressure at fixed time intervals
            while (time >= nextPressureTime) {
                double pWall = (wallDeltaPSum / interval) / containerPerimeter;
                double pObs  = (obsDeltaPSum  / interval) / obstaclePerimeter;
                pressureWriter.write(String.format("%.6f %.6f %.6f\n", nextPressureTime, pWall, pObs));
                // reset counters for next interval
                nextPressureTime += interval;
                wallHits = 0; wallDeltaPSum = 0.0;
                obsHits  = 0; obsDeltaPSum  = 0.0;
            }
        }
        stateWriter.close();
        pressureWriter.close();
        collisionWriter.close();
    }

    public static void main(String[] args) throws IOException {
        int N = 200;
        double L = 0.1;
        double R_obs = 0.005;
        double r = 5e-4;
        double m = 1.0;
        double v0 = 10.0; //TODO: IR cambiando esto antes de correrlo
        double maxTime = 10.0;
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
                if (distSq > containerLimit*containerLimit) continue;
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
        sim.simulate(maxTime, "./results/simulation_v"+v0+".txt", recordEvery, v0);
    }
}
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Random;

/** Represents a particle in 2D space. */
class Particle {
    double x, y;      // position
    double vx, vy;    // velocity
    final double radius;
    final double mass;
    int collisionCount = 0;

    public Particle(double x, double y, double vx, double vy, double r, double m) {
        this.x = x; this.y = y;
        this.vx = vx; this.vy = vy;
        this.radius = r;
        this.mass = m;
    }

    // Move particle in straight line for time dt
    public void move(double dt) {
        x += vx * dt;
        y += vy * dt;
    }

    // Time to collide with another particle (elastic)
    public double timeToHit(Particle that) {
        if (this == that) return Double.POSITIVE_INFINITY;
        double dx = that.x - this.x;
        double dy = that.y - this.y;
        double dvx = that.vx - this.vx;
        double dvy = that.vy - this.vy;
        double dvdr = dx*dvx + dy*dvy;
        if (dvdr >= 0) return Double.POSITIVE_INFINITY;
        double dvdv = dvx*dvx + dvy*dvy;
        double drdr = dx*dx + dy*dy;
        double sigma = this.radius + that.radius;
        double d = (dvdr*dvdr) - dvdv * (drdr - sigma*sigma);
        if (d < 0) return Double.POSITIVE_INFINITY;
        return -(dvdr + Math.sqrt(d)) / dvdv;
    }

    // Time to hit circular wall of radius R_container
    public double timeToHitWall(double R_container) {
        // Solve |(x + vx*t, y + vy*t)| = R_container - radius
        double R = R_container - this.radius;
        double b = 2*(x*vx + y*vy);
        double c = x*x + y*y - R*R;
        double disc = b*b - 4* (vx*vx + vy*vy) * c;
        if (disc < 0) return Double.POSITIVE_INFINITY;
        double sqrt = Math.sqrt(disc);
        double t1 = (-b + sqrt) / (2*(vx*vx+vy*vy));
        double t2 = (-b - sqrt) / (2*(vx*vx+vy*vy));
        double t = (t1>1e-10? t1 : (t2>1e-10? t2 : Double.POSITIVE_INFINITY));
        return t;
    }

    // Time to hit a (fixed) circular obstacle at (0,0) of radius R_obs
    public double timeToHitObstacle(double R_obs) {
        // identical to wall but inner circle
        double R = R_obs + this.radius;
        double b = 2*(x*vx + y*vy);
        double c = x*x + y*y - R*R;
        double disc = b*b - 4*(vx*vx+vy*vy)*c;
        if (disc < 0) return Double.POSITIVE_INFINITY;
        double sqrt = Math.sqrt(disc);
        // We want the smallest positive root
        double t1 = (-b + sqrt)/(2*(vx*vx+vy*vy));
        double t2 = (-b - sqrt)/(2*(vx*vx+vy*vy));
        double t = (t2>1e-10? t2 : (t1>1e-10? t1 : Double.POSITIVE_INFINITY));
        return t;
    }

    // Bounce off another particle elastically
    public void bounceOff(Particle that) {
        double dx = that.x - this.x;
        double dy = that.y - this.y;
        double dvx = that.vx - this.vx;
        double dvy = that.vy - this.vy;
        double dvdr = dx*dvx + dy*dvy;
        double dist = this.radius + that.radius;
        // magnitude of normal force
        double J = 2 * this.mass * that.mass * dvdr / ((this.mass + that.mass) * dist);
        double Jx = J * dx / dist;
        double Jy = J * dy / dist;
        this.vx += Jx / this.mass;
        this.vy += Jy / this.mass;
        that.vx -= Jx / that.mass;
        that.vy -= Jy / that.mass;
        this.collisionCount++; that.collisionCount++;
    }

    // Bounce off container wall
    public void bounceOffWall() {
        // normal is radial: (x,y)
        double norm = Math.sqrt(x*x + y*y);
        double nx = x / norm;
        double ny = y / norm;
        double dot = vx*nx + vy*ny;
        vx -= 2*dot*nx;
        vy -= 2*dot*ny;
        collisionCount++;
    }

    // Bounce off fixed obstacle
    public void bounceOffObstacle(double R_obs) {
        // normal from obstacle center at (0,0)
        double norm = Math.sqrt(x*x + y*y);
        double nx = x / norm;
        double ny = y / norm;
        double dot = vx*nx + vy*ny;
        vx -= 2*dot*nx;
        vy -= 2*dot*ny;
        collisionCount++;
    }
}
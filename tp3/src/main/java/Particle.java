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

    public void move(double dt) {
        x += vx * dt;
        y += vy * dt;
    }

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

    public double timeToHitWall(double R_container) {
        double R = R_container - this.radius;
        double b = 2*(x*vx + y*vy);
        double c = x*x + y*y - R*R;
        double disc = b*b - 4*(vx*vx + vy*vy)*c;
        if (disc < 0) return Double.POSITIVE_INFINITY;
        double sqrt = Math.sqrt(disc);
        double denom = 2*(vx*vx + vy*vy);
        double t1 = (-b + sqrt) / denom;
        double t2 = (-b - sqrt) / denom;
        double t = (t1 > 1e-10 ? t1 : (t2 > 1e-10 ? t2 : Double.POSITIVE_INFINITY));
        return t;
    }

    public double timeToHitObstacle(double R_obs) {
        double R = R_obs + this.radius;
        double b = 2*(x*vx + y*vy);
        double c = x*x + y*y - R*R;
        double disc = b*b - 4*(vx*vx + vy*vy)*c;
        if (disc < 0) return Double.POSITIVE_INFINITY;
        double sqrt = Math.sqrt(disc);
        double denom = 2*(vx*vx + vy*vy);
        double t1 = (-b + sqrt) / denom;
        double t2 = (-b - sqrt) / denom;
        double t = (t2 > 1e-10 ? t2 : (t1 > 1e-10 ? t1 : Double.POSITIVE_INFINITY));
        return t;
    }

    /**
     * Elastic collision impulse with another particle
     */
    public void bounceOff(Particle that) {
        double dx = that.x - this.x;
        double dy = that.y - this.y;
        double dvx = that.vx - this.vx;
        double dvy = that.vy - this.vy;
        double dvdr = dx*dvx + dy*dvy;
        double dist = this.radius + that.radius;
        double J = 2 * this.mass * that.mass * dvdr / ((this.mass + that.mass) * dist);
        double Jx = J * dx / dist;
        double Jy = J * dy / dist;
        this.vx += Jx / this.mass;
        this.vy += Jy / this.mass;
        that.vx -= Jx / that.mass;
        that.vy -= Jy / that.mass;
        this.collisionCount++; that.collisionCount++;
    }

    /**
     * Elastic bounce on container wall; returns momentum change magnitude Δp
     */
    public double bounceOffWall() {
        double norm = Math.sqrt(x*x + y*y);
        double nx = x / norm;
        double ny = y / norm;
        double vn = vx*nx + vy*ny;  // normal component
        double deltaP = 2 * mass * Math.abs(vn);
        vx -= 2*vn*nx;
        vy -= 2*vn*ny;
        collisionCount++;
        return deltaP;
    }

    /**
     * Elastic bounce on fixed obstacle; returns momentum change magnitude Δp
     */
    public double bounceOffObstacle() {
        double norm = Math.sqrt(x*x + y*y);
        double nx = x / norm;
        double ny = y / norm;
        double vn = vx*nx + vy*ny;
        double deltaP = 2 * mass * Math.abs(vn);
        vx -= 2*vn*nx;
        vy -= 2*vn*ny;
        collisionCount++;
        return deltaP;
    }
}
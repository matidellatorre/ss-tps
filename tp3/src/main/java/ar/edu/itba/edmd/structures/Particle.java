package ar.edu.itba.edmd.structures;

public class Particle {
    private Vector position;
    private Vector velocity;
    private int collisionCount;
    private final int id;
    private final double radius;
    private final double mass;

    public Particle(final Vector position, final Vector velocity, final int id, final double radius, final double mass) {
        this.position = position;
        this.velocity = velocity;
        this.collisionCount = 0;
        this.id = id;
        this.radius = radius;
        this.mass = mass;
    }

    // Getters
    public final Vector getPosition() { return position; }
    public final Vector getVelocity() { return velocity; }
    public final double getRadius() { return radius; }
    public final double getMass() { return mass; }
    public final int getCollisionCount() { return collisionCount; }
    public final int getId() { return id; }

    // Setters
    public final void setVelocity(final Vector velocity) {
        this.velocity = velocity;
    }

    public final void increaseCollisionCount() {
        this.collisionCount++;
    }

    // Motion: advance in straight line
    public void move(double dt) {
        Vector displacement = velocity.scale(dt);
        this.position = position.add(displacement);
    }

    // Collision time with another particle
    public double timeToHit(Particle other) {
        if (this == other) return Double.POSITIVE_INFINITY;

        Vector deltaR = other.getPosition().subtract(this.position);
        Vector deltaV = other.getVelocity().subtract(this.velocity);
        double dvdr = deltaV.dot(deltaR);

        if (dvdr >= 0) return Double.POSITIVE_INFINITY;

        double dvdv = deltaV.dot(deltaV);
        double drdr = deltaR.dot(deltaR);
        double sigma = this.radius + other.radius;

        double distance = deltaR.magnitude();
        if (distance < this.radius + other.radius - 1e-6) {
            // Particles are too close / overlapping — skip or resolve manually
            return Double.POSITIVE_INFINITY;
        }

        double d = (dvdr * dvdr) - dvdv * (drdr - sigma * sigma);

        if (d < 0) return Double.POSITIVE_INFINITY;

        double t = -(dvdr + Math.sqrt(d)) / dvdv;

        if (Double.isNaN(t)) {
            System.err.printf("🚨 NaN collision time between p%d and p%d with time %.4f\n", this.getId(), other.getId(), t);
        }
        return t;
    }

    // Collision response with another particle (elastic)
    public void bounceOff(Particle other) {
        Vector r1 = this.getPosition();
        Vector r2 = other.getPosition();
        Vector v1 = this.getVelocity();
        Vector v2 = other.getVelocity();
        Vector deltaR = r2.subtract(r1);
        Vector deltaV = v2.subtract(v1);

        double distanceSquared = deltaR.dot(deltaR);
        if (distanceSquared == 0) return;  // overlapping

        double dvdr = deltaV.dot(deltaR);
        if (dvdr >= 0) return; // moving apart

        double m1 = this.mass;
        double m2 = other.mass;

        double scale = (2 * m1 * m2 * dvdr) / ((m1 + m2) * distanceSquared);
        Vector impulse = deltaR.scale(scale / (m1 + m2));

        this.setVelocity(v1.add(impulse.scale(1 / m1)));
        other.setVelocity(v2.subtract(impulse.scale(1 / m2)));

        this.increaseCollisionCount();
        other.increaseCollisionCount();
    }

    // Collision time with circular boundary
    public double timeToHitCircularBoundary(double containerRadius) {
        Vector r = this.position;
        Vector v = this.velocity;
        double effectiveRadius = containerRadius - this.radius;

        double a = v.dot(v);
        double b = 2 * r.dot(v);
        double c = r.dot(r) - effectiveRadius * effectiveRadius;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return Double.POSITIVE_INFINITY;

        double sqrtD = Math.sqrt(discriminant);
        double t1 = (-b + sqrtD) / (2 * a);
        double t2 = (-b - sqrtD) / (2 * a);

        if (t1 > 1e-10 && Double.isFinite(t1)) return t1;
        if (t2 > 1e-10 && Double.isFinite(t2)) return t2;
        return Double.POSITIVE_INFINITY;
    }

    // Reflect off circular boundary
    public void bounceOffCircularBoundary(double containerRadius) {
        Vector pos = this.position;
        Vector vel = this.velocity;

        Vector normal = pos.direction();  // unit vector pointing outward
        Vector reflected = vel.subtract(normal.scale(2 * vel.dot(normal)));  // reflection

        this.setVelocity(reflected);
        this.increaseCollisionCount();

        double maxR = containerRadius - this.getRadius();
        if (pos.magnitude() > maxR) {
            this.position = normal.scale(maxR - 1e-6);
        }
    }

    // Equals and hashCode (for sets/maps)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Particle other = (Particle) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

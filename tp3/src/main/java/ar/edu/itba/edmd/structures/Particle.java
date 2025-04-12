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

    public final Vector getPosition() {
        return position;
    }
    public final Vector getVelocity() {
        return velocity;
    }
    public final double getRadius() {
        return radius;
    }
    public final double getMass() {
        return mass;
    }
    public final int getCollisionCount() {
        return collisionCount;
    }
    public final int getId() {
        return id;
    }

    // linea recta con dt
    public void move(double dt) {
        Vector displacement = velocity.scale(dt);
        this.position = position.add(displacement);
    }


    // tiempo hasta que dos particulas colisionen
    public double timeToHit(Particle other) {
        if (this == other) return Double.POSITIVE_INFINITY;

        Vector deltaR = other.getPosition().subtract(this.position);
        Vector deltaV = other.getVelocity().subtract(this.velocity);
        double dvdr = deltaV.dot(deltaR);

        if (dvdr >= 0) return Double.POSITIVE_INFINITY;

        double dvdv = deltaV.dot(deltaV);
        double drdr = deltaR.dot(deltaR);
        double sigma = this.radius + other.radius;
        double d = (dvdr * dvdr) - dvdv * (drdr - sigma * sigma);

        if (d < 0) return Double.POSITIVE_INFINITY;

        return -(dvdr + Math.sqrt(d)) / dvdv;
    }

    // colision entre dos particulas
    public void bounceOff(Particle other) {
        Vector deltaR = other.getPosition().subtract(this.position);
        Vector deltaV = other.getVelocity().subtract(this.velocity);
        double dvdr = deltaV.dot(deltaR);
        double dist = this.radius + other.getRadius();

        // Magnitud del impulso
        double J = (2 * this.mass * other.getMass() * dvdr) / (dist * (this.mass + other.getMass()));
        Vector impulse = deltaR.scale(J / dist);

        // Actualiza velocidades
        velocity = velocity.add(impulse.scale(1 / mass));
        other.setVelocity(other.getVelocity().subtract(impulse.scale(1 / other.getMass())));

        collisionCount++;
        other.increaseCollisionCount();
    }

    public final void setVelocity(final Vector velocity) {
        this.velocity = velocity;
    }
    public final void increaseCollisionCount() {
        this.collisionCount++;
    }

    public double timeToHitCircularBoundary(double containerRadius) {
        Vector r = getPosition();
        Vector v = getVelocity();

        double effectiveRadius = containerRadius - getRadius(); // max distance from center before bounce

        double a = v.dot(v);
        double b = 2 * r.dot(v);
        double c = r.dot(r) - effectiveRadius * effectiveRadius;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return Double.POSITIVE_INFINITY;

        double sqrtD = Math.sqrt(discriminant);
        double t1 = (-b + sqrtD) / (2 * a);
        double t2 = (-b - sqrtD) / (2 * a);

        double t = Math.min(t1, t2);
        return t > 0 ? t : Double.POSITIVE_INFINITY;
    }

    public void bounceOffCircularBoundary() {
        Vector pos = getPosition();
        Vector vel = getVelocity();

        // Normal vector = unit vector from center to collision point
        Vector normal = pos.direction();

        // Reflect velocity: v' = v - 2 * (v · n) * n
        Vector newVel = vel.subtract(normal.scale(2 * vel.dot(normal)));

        setVelocity(newVel);
        increaseCollisionCount();
    }

}

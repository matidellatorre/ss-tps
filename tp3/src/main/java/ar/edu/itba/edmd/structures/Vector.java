package ar.edu.itba.edmd.structures;

public class Vector {
    private final double x;
    private final double y;

    public Vector(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public final double getx() {
        return x;
    }
    public final double gety() {
        return y;
    }

    public Vector add(Vector v) {
        return new Vector(x + v.getx(), y + v.gety());
    }

    public Vector subtract(Vector v) {
        return new Vector(x - v.getx(), y - v.gety() );
    }

    public Vector scale(double factor) {
        return new Vector(x * factor, y * factor);
    }

    public double dot(Vector v) {
        return x * v.getx()+y * v.gety();
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double distanceTo(Vector v) {
        return this.subtract(v).magnitude();
    }

    public Vector direction() {
        double mag = this.magnitude();
        if (mag == 0) return new Vector(0, 0); // para evitar división por cero
        return this.scale(1.0 / mag);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

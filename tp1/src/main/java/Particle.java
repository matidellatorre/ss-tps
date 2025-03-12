public class Particle {
    private final double x;
    private final double y;
    private final int id;
    private final double radius;

    public Particle(double x, double y, int id, double radius) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.radius = radius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    public double getRadius() {
        return radius;
    }

    public double distanceTo(Particle other) {
        double dx = Constants.getBoundaryCond() ?
                Math.min(Math.abs(this.x - other.x), Constants.getL() - Math.abs(this.x - other.x)) :
                Math.abs(this.x - other.x);
        double dy = Constants.getBoundaryCond() ?
                Math.min(Math.abs(this.y - other.y), Constants.getL() - Math.abs(this.y - other.y)) :
                Math.abs(this.y - other.y);

        return Math.sqrt(dx * dx + dy * dy);
    }
}

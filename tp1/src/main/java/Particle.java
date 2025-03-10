public class Particle {
    private final double x;
    private final double y;
    private final int id;

    public Particle(double x, double y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;

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

    public double distanceTo(Particle p) {
        double dx = Math.abs(this.x - p.x);
        double dy = Math.abs(this.y - p.y);

        if (Constants.getBoundaryCond()) {
            if (dx > Constants.getL() / 2) {
                dx = Constants.getL() - dx;
            }

            if (dy > Constants.getL() / 2) {
                dy = Constants.getL() - dy;
            }
        }

        return Math.sqrt(dx*dx + dy*dy);
    }

    @Override
    public String toString() {
        return "Particle{" +
                "x=" + x +
                ", y=" + y +
                ", id=" + id +
                '}';
    }
}

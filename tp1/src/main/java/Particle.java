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

    public double distanceTo(Particle p){
        return Math.sqrt((Math.pow(this.x-p.x,2)+Math.pow(this.y-p.y, 2)));
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

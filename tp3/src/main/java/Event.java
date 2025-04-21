class Event implements Comparable<Event> {
    final double time;
    final Particle a, b;
    final int countA, countB;

    public Event(double t, Particle a, Particle b) {
        this.time = t;
        this.a = a;
        this.b = b;
        this.countA = (a != null ? a.collisionCount : -1);
        this.countB = (b != null ? b.collisionCount : -1);
    }

    public boolean isValid() {
        if (a != null && a.collisionCount != countA) return false;
        if (b != null && b.collisionCount != countB) return false;
        return true;
    }

    @Override
    public int compareTo(Event that) {
        return Double.compare(this.time, that.time);
    }
}
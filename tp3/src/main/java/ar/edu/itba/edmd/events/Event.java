package ar.edu.itba.edmd.events;

import ar.edu.itba.edmd.structures.Particle;

public class Event implements Comparable<Event> {
    private final double time;
    private final Particle a;
    private final Particle b;
    private final int countA;
    private final int countB;
    private final EventType type;

    public Event(final double time, final Particle a, final Particle b, final EventType type) {
        this.time = time;
        this.a = a;
        this.b = b;
        this.countA = (a != null) ? a.getCollisionCount() : -1;
        this.countB = (b != null) ? b.getCollisionCount() : -1;
        this.type = type;
    }

    public final boolean isValid() {
        if (a != null && a.getCollisionCount() != countA) return false;
        if (b != null && b.getCollisionCount() != countB) return false;
        return true;
    }

    public EventType getType() {
        return type;
    }

    public double getTime() {
        return time;
    }

    public Particle getA() {
        return a;
    }

    public Particle getB() {
        return b;
    }

    @Override
    public int compareTo(Event that) {
        return Double.compare(this.time, that.time);
    }

    @Override
    public String toString() {
        return String.format("Event[%.4f | %s | a=%s, b=%s]",
                time, type.name(),
                (a != null ? a.getId() : "null"),
                (b != null ? b.getId() : "null"));
    }

}

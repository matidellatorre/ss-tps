package ar.edu.itba.edmd.structures;

import java.util.ArrayList;
import java.util.List;

public class Obstacle extends Particle{
    private final ObstacleType type;
    private final List<Double> timeStamps;
    private final List<Vector> positionHistory;

    public Obstacle(final Vector position, final Vector velocity, final double radius, final double mass, final ObstacleType type, final int id) {
        super(position, velocity, id, radius, mass);
        this.type = type;
        this.timeStamps = new ArrayList<>();
        this.positionHistory = new ArrayList<>();
    }

    public final boolean isStatic() {
        return this.type == ObstacleType.STATIC;
    }

    public final ObstacleType getType() {
        return type;
    }

    @Override
    public void move(double dt) {
        if (!isStatic()) {
            super.move(dt);
        }
    }

    @Override
    public void bounceOff(Particle that) {
        if (!isStatic()) {
            super.bounceOff(that);
        } else {
            Vector deltaR = that.getPosition().subtract(this.getPosition());
            Vector deltaV = that.getVelocity().subtract(this.getVelocity());
            double dvdr = deltaV.dot(deltaR);
            double dist = this.getRadius() + that.getRadius();

            double J = (2 * this.getMass() * that.getMass() * dvdr) / (dist * (this.getMass() + that.getMass()));
            Vector impulse = deltaR.scale(J / dist);

            that.setVelocity(that.getVelocity().subtract(impulse.scale(1 / that.getMass())));
            that.increaseCollisionCount();
        }
    }

    public final void logPosition(double currentTime) {
        if (!isStatic()) {
            timeStamps.add(currentTime);
            positionHistory.add(this.getPosition());
        }
    }

    // Calculate displacement squared relative to initial position
    public final List<Double> getDisplacementSquaredHistory() {
        List<Double> dcmList = new ArrayList<>();
        if (positionHistory.isEmpty()) return dcmList;

        Vector initial = positionHistory.getFirst();
        for (Vector pos : positionHistory) {
            double dx = pos.getx() - initial.getx();
            double dy = pos.gety() - initial.gety();
            dcmList.add(dx * dx + dy * dy);
        }
        return dcmList;
    }

    public final List<Double> getTimeStamps() {
        return new ArrayList<>(timeStamps);
    }
}

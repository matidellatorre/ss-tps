package oscillator;

public interface Integrator {
    void step();
    double getPosition();
    // double getVelocity(); // Opcional, si se necesita explícitamente
    void reset(double r0, double v0); // Para reiniciar el estado del integrador
}
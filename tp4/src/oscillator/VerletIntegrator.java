package oscillator;

public class VerletIntegrator implements Integrator {
    private double rCurr;
    private double rPrev;
    private final double dt;
    private final double m;
    private final double k;
    private final double gamma;

    public VerletIntegrator(double r0, double v0, double dt) {
        this.dt = dt;
        this.m = Constants.M;
        this.k = Constants.K;
        this.gamma = Constants.GAMMA;
        reset(r0, v0);
    }

    @Override
    public void reset(double r0, double v0) {
        this.rCurr = r0;
        // Estimar r(-Δt) usando expansión de Taylor: r(-Δt) = r(0) - v(0)Δt + a(0)Δt²/2
        // a(0) = F(r(0), v(0))/m
        double acceleration0 = Force.calculate(r0, v0) / m;
        this.rPrev = r0 - v0 * dt + 0.5 * acceleration0 * dt * dt;
    }

    @Override
    public void step() {
        // Fórmula de Verlet para fuerzas dependientes de velocidad (forma implícita resuelta):
        // r(t+Δt) = ( (2r(t) - r(t-Δt)) - (kΔt²/m)r(t) + (γΔt/(2m))r(t-Δt) ) / (1 + γΔt/(2m))
        double term1 = 2 * rCurr - rPrev;
        double term2 = (k * dt * dt / m) * rCurr;
        double term3 = (gamma * dt / (2 * m)) * rPrev;
        double denominator = 1 + (gamma * dt / (2 * m));

        double rNext = (term1 - term2 + term3) / denominator;

        rPrev = rCurr;
        rCurr = rNext;
    }

    @Override
    public double getPosition() {
        return rCurr;
    }

    //  v(t) = [r(t + Δt) - r(t - Δt)] / (2 * Δt)
    // Para obtener la velocidad actual, necesitaríamos rNext, que no está disponible hasta después del paso.
    // Si rCurr es r(t), entonces rPrev es r(t-dt). rNext sería r(t+dt).
    // Para calcular v(t) actual (asociado a rCurr): (rNext (no calculado aun para el exterior) - rPrev) / (2 * dt)
    // Esta implementación de Verlet no almacena explícitamente la velocidad.
}
package oscillator;

public class BeemanIntegrator implements Integrator {
    private double r, v, a, aPrev;
    private final double dt;
    private final double m;
    private final double k;
    private final double gamma;

    public BeemanIntegrator(double r0, double v0, double dt) {
        this.dt = dt;
        this.m = Constants.M;
        this.k = Constants.K;
        this.gamma = Constants.GAMMA;
        reset(r0, v0);
    }

    @Override
    public void reset(double r0, double v0) {
        this.r = r0;
        this.v = v0;
        this.a = Force.calculate(r, v) / m;
        // Para el primer paso, se asume a(t-Δt) = a(t)
        this.aPrev = this.a;
    }

    @Override
    public void step() {
        // 1. Calcular r(t + Δt)
        double rNext = r + v * dt + (2.0/3.0) * a * dt * dt - (1.0/6.0) * aPrev * dt * dt;

        // 2. Predecir v(t + Δt) usando una fórmula (no la que se usa para la corrección final) o calcular a(t+Δt)
        // La fórmula de v(t+Δt) dada depende de a(t+Δt), que depende de v(t+Δt) y r(t+Δt).
        // v(t+Δt) = (v(t) - (kΔt/(3m))r(t+Δt) + (5/6)a(t)Δt - (1/6)a(t-Δt)Δt) / (1 + γΔt/(3m))

        double vNextNumerator = v - (k * dt / (3.0 * m)) * rNext
                + (5.0/6.0) * a * dt - (1.0/6.0) * aPrev * dt;
        double vNextDenominator = 1.0 + (gamma * dt / (3.0 * m));
        double vNext = vNextNumerator / vNextDenominator;

        // 3. Calcular a(t + Δt) usando r(t + Δt) y v(t + Δt)
        double aNext = Force.calculate(rNext, vNext) / m;

        // Actualizar variables
        r = rNext;
        v = vNext;
        aPrev = a;
        a = aNext;
    }

    @Override
    public double getPosition() {
        return r;
    }
}
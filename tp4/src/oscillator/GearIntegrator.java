package oscillator;

import java.util.Arrays;

public class GearIntegrator implements Integrator {
    private final double dt;
    private final double m;
    private final double k;
    private final double gamma;

    // rp[0]=r, rp[1]=r', rp[2]=r'', rp[3]=r''', rp[4]=r'''', rp[5]=r''''''
    private double[] rp; // Derivadas corregidas r, r', r'', ...
    private final double[] rPred; // Derivadas predichas

    // Coeficientes alpha para Gear de orden 5 (fuerzas dependen de r y v)
    private final double[] alpha = {3.0/16.0, 251.0/360.0, 1.0, 11.0/18.0, 1.0/6.0, 1.0/60.0};
    private final double[] factorials = {1.0, 1.0, 2.0, 6.0, 24.0, 120.0}; // 0! a 5!

    public GearIntegrator(double r0, double v0, double dt) {
        this.dt = dt;
        this.m = Constants.M;
        this.k = Constants.K;
        this.gamma = Constants.GAMMA;
        this.rp = new double[6];
        this.rPred = new double[6];
        reset(r0, v0);
    }

    @Override
    public void reset(double r0, double v0) {
        rp[0] = r0; // r
        rp[1] = v0; // r_1 (velocidad)
        rp[2] = Force.calculate(rp[0], rp[1]) / m; // r_2 (aceleración)
        rp[3] = (-k * rp[1] - gamma * rp[2]) / m;   // r_3
        rp[4] = (-k * rp[2] - gamma * rp[3]) / m;   // r_4
        rp[5] = (-k * rp[3] - gamma * rp[4]) / m;   // r_5
    }

    @Override
    public void step() {
        // 1. Predecir derivadas en t + Δt
        // rp[q]^p (t+Δt) = sum_{j=q to 5} rp[j](t) * (Δt)^(j-q) / (j-q)!
        // O de forma expandida:
        rPred[0] = rp[0] + rp[1]*dt + rp[2]*dt*dt/2.0 + rp[3]*dt*dt*dt/6.0 + rp[4]*dt*dt*dt*dt/24.0 + rp[5]*dt*dt*dt*dt*dt/120.0;
        rPred[1] = rp[1] + rp[2]*dt + rp[3]*dt*dt/2.0 + rp[4]*dt*dt*dt/6.0 + rp[5]*dt*dt*dt*dt/24.0;
        rPred[2] = rp[2] + rp[3]*dt + rp[4]*dt*dt/2.0 + rp[5]*dt*dt*dt/6.0;
        rPred[3] = rp[3] + rp[4]*dt + rp[5]*dt*dt/2.0;
        rPred[4] = rp[4] + rp[5]*dt;
        rPred[5] = rp[5];

        // 2. Evaluar Fuerza y Aceleración con variables predichas
        // F(t+Δt) con r_pred[0] y r_pred[1]
        double forceEval = Force.calculate(rPred[0], rPred[1]);
        double accelEval = forceEval / m; // Esta es a(t+Δt) o r_2_eval(t+Δt)

        // 3. Calcular ΔR2
        // Δa = a_eval(t+Δt) - a_pred(t+Δt) = accelEval - rPred[2]
        // ΔR2 = Δa * (Δt)^2 / 2!
        double deltaAccel = accelEval - rPred[2];
        double deltaR2_term = deltaAccel * dt * dt / 2.0; // Este es el ΔR2 de la fórmula del usuario

        // 4. Corregir las derivadas
        // r_q^c = r_q^p + α_q * ΔR2 * (q! / Δt^q)
        // Esto simplifica a:
        // rp_c[0] = rp_p[0] + α_0 * delta_accel * (dt*dt/2.0)
        // rp_c[1] = rp_p[1] + α_1 * delta_accel * (dt/2.0)
        // rp_c[2] = rp_p[2] + α_2 * delta_accel
        // rp_c[3] = rp_p[3] + α_3 * delta_accel * (3.0/dt)
        // rp_c[4] = rp_p[4] + α_4 * delta_accel * (12.0/(dt*dt))
        // rp_c[5] = rp_p[5] + α_5 * delta_accel * (60.0/(dt*dt*dt))

        rp[0] = rPred[0] + alpha[0] * deltaAccel * (dt*dt / factorials[2]); // factorials[2] = 2! = 2
        rp[1] = rPred[1] + alpha[1] * deltaAccel * (dt*dt / factorials[2]) * (factorials[1] / dt);
        rp[2] = rPred[2] + alpha[2] * deltaAccel * (dt*dt / factorials[2]) * (factorials[2] / (dt*dt));
        rp[3] = rPred[3] + alpha[3] * deltaAccel * (dt*dt / factorials[2]) * (factorials[3] / (dt*dt*dt));
        rp[4] = rPred[4] + alpha[4] * deltaAccel * (dt*dt / factorials[2]) * (factorials[4] / (dt*dt*dt*dt));
        rp[5] = rPred[5] + alpha[5] * deltaAccel * (dt*dt / factorials[2]) * (factorials[5] / (dt*dt*dt*dt*dt));
    }

    @Override
    public double getPosition() {
        return rp[0];
    }
}
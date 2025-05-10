package oscillator;

public class AnalyticalSolution {
    public static double getPosition(double t) {
        // r(t) = A * exp(- (γ / (2m)) * t) * cos( sqrt(k / m - γ² / (4m²)) * t )
        // Con A = 1 (dado por r(0)=1)
        return Math.exp(-Constants.XI * t) * Math.cos(Constants.OMEGA_PRIMA * t);
    }
}
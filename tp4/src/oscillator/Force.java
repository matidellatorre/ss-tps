package oscillator;

public class Force {
    public static double calculate(double r, double v) {
        return -Constants.K * r - Constants.GAMMA * v;
    }
}
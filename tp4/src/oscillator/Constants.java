package oscillator;

public class Constants {
    public static final double M = 70.0; // Masa en kg
    public static final double K = 10000.0; // Constante del resorte en N/m
    public static final double GAMMA = 100.0; // Coeficiente de amortiguamiento en kg/s
    public static final double TF = 5.0; // Tiempo final de simulación en s

    public static final double R0 = 1.0; // Posición inicial r(t=0) en m
    // Amplitud A = R0 ya que r(0) = A * exp(0) * cos(0) = A
    public static final double V0 = -GAMMA / (2.0 * M); // Velocidad inicial v(t=0) en m/s

    // Omega prima para la solución analítica (frecuencia angular amortiguada)
    public static final double OMEGA_PRIMA_SQUARED = K / M - (GAMMA * GAMMA) / (4 * M * M);
    public static final double OMEGA_PRIMA = Math.sqrt(OMEGA_PRIMA_SQUARED);

    // Constante de atenuación para la solución analítica
    public static final double XI = GAMMA / (2.0 * M);

    // Verificar que el sistema sea subamortiguado
    static {
        if (OMEGA_PRIMA_SQUARED <= 0) {
            System.err.println("El sistema no es subamortiguado. Revisar parámetros k, m, gamma.");
            System.err.println("k/m = " + (K/M));
            System.err.println("gamma^2/(4m^2) = " + ((GAMMA*GAMMA)/(4*M*M)));
            // Considerar manejo de casos críticamente amortiguado u oscilatorio sobreamortiguado si es necesario.
        }
    }
}
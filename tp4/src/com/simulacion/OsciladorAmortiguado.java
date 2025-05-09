package com.simulacion;

public class OsciladorAmortiguado {
    // Parámetros del sistema según la diapositiva 36
    public static final double MASA = 70.0; // kg
    public static final double CONSTANTE_RESORTE = 10000.0; // N/m
    public static final double COEFICIENTE_AMORTIGUAMIENTO  = 100.0; // kg/s

    // Condiciones iniciales
    public static final double POSICION_INICIAL = 1.0; // m
    // v(0) = -A * gamma / (2m), y A = r(0)
    public static final double VELOCIDAD_INICIAL = - (POSICION_INICIAL * COEFICIENTE_AMORTIGUAMIENTO) / (2 * MASA); // m/s

    // Tiempo final de la simulación
    public static final double TIEMPO_FINAL  = 5.0; // s

    // Constructor y otros métodos si fueran necesarios
    public OsciladorAmortiguado() {
    }

    public static double calcularFuerza(double posicion, double velocidad) {
        return -CONSTANTE_RESORTE * posicion - COEFICIENTE_AMORTIGUAMIENTO * velocidad;
    }

    public static double calcularAceleracion(double posicion, double velocidad) {
        return calcularFuerza(posicion, velocidad) / MASA;
    }

    // Derivadas de la aceleración para Gear
    // a_dot = (-k*v - gamma*a) / m
    public static double calcularDerivadaAceleracion(double velocidad, double aceleracion) {
        return (-CONSTANTE_RESORTE * velocidad - COEFICIENTE_AMORTIGUAMIENTO * aceleracion) / MASA;
    }

    // a_ddot = (-k*a - gamma*a_dot) / m
    public static double calcularSegundaDerivadaAceleracion(double aceleracion, double derivadaAceleracion) {
        return (-CONSTANTE_RESORTE * aceleracion - COEFICIENTE_AMORTIGUAMIENTO * derivadaAceleracion) / MASA;
    }

    // a_dddot = (-k*a_dot - gamma*a_ddot) / m
    public static double calcularTerceraDerivadaAceleracion(double derivadaAceleracion, double segundaDerivadaAceleracion) {
        return (-CONSTANTE_RESORTE * derivadaAceleracion - COEFICIENTE_AMORTIGUAMIENTO * segundaDerivadaAceleracion) / MASA;
    }
}

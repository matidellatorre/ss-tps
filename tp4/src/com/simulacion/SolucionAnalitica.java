package com.simulacion;

public class SolucionAnalitica {

    public static double calcularPosicion(double t) {
        double m = OsciladorAmortiguado.MASA;
        double k = OsciladorAmortiguado.CONSTANTE_RESORTE;
        double gamma = OsciladorAmortiguado.COEFICIENTE_AMORTIGUAMIENTO;
        double r0 = OsciladorAmortiguado.POSICION_INICIAL; // A = r0

        double beta = gamma / (2 * m);
        double omega_term_sq = (k / m) - (gamma * gamma) / (4 * m * m);

        if (omega_term_sq < 0) {
            // Caso sobreamortiguado o críticamente amortiguado (no esperado según parámetros)
            // Para simplificar, nos enfocamos en el subamortiguado dado por la consigna.
            // La fórmula cambiaría. La consigna implica subamortiguado.
            System.err.println("Advertencia: El sistema podría no ser subamortiguado con estos parámetros para la fórmula provista.");
            // Si omega_term_sq es 0 (crítico) o negativo (sobreamortiguado), la solución cos(...) no es la forma correcta
            // Pero los parámetros dados (k/m = 142.8, (gamma/2m)^2 = 0.51) aseguran subamortiguamiento.
        }

        double omega_d = Math.sqrt(omega_term_sq); // Frecuencia angular amortiguada

        // r(t) = A * exp(-beta * t) * cos(omega_d * t)
        // Dado que v(0) = -A * gamma / (2m) y r(0) = A, el término de fase phi es 0.
        return r0 * Math.exp(-beta * t) * Math.cos(omega_d * t);
    }
}
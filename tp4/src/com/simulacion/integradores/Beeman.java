package com.simulacion.integradores;

import com.simulacion.OsciladorAmortiguado;
import com.simulacion.SolucionAnalitica;

import java.io.PrintWriter;
import java.util.List;

public class Beeman implements Integrador {
    @Override
    public String getNombre() {
        return "Beeman";
    }

    @Override
    public void simular(double dt, double tiempoTotal, PrintWriter writer, List<Double> tiempos, List<Double> posicionesNumericas) {
        double rCurr = OsciladorAmortiguado.POSICION_INICIAL;
        double vCurr = OsciladorAmortiguado.VELOCIDAD_INICIAL;
        double aCurr = OsciladorAmortiguado.calcularAceleracion(rCurr, vCurr);
        double aPrev = OsciladorAmortiguado.calcularAceleracion(rCurr - vCurr * dt, vCurr - aCurr*dt); // Estimar a(-dt)
        // O más simple: aPrev = aCurr para el primer paso.
        //aPrev = aCurr; // Simplificación común para el primer paso de Beeman

        double t = 0;
        tiempos.clear();
        posicionesNumericas.clear();

        // writer.println("tiempo,posicion_analitica,posicion_beeman");

        for (int step = 0; t <= tiempoTotal + dt/2.0; step++) {
            double rAnalitica = SolucionAnalitica.calcularPosicion(t);
            if (writer != null) {
                writer.printf("%.16f,%.16f,%.16f\n", t, rAnalitica, rCurr);
            }
            tiempos.add(t);
            posicionesNumericas.add(rCurr);

            if (t >= tiempoTotal - dt/2.0 && t > 0) break;

            // Predictor
            double rNext = rCurr + vCurr * dt + (1.0/6.0) * (4 * aCurr - aPrev) * dt * dt;
            double vPredNext = vCurr + (1.0/2.0) * (3 * aCurr - aPrev) * dt;

            // Evaluar aceleración en t+dt
            double aNext = OsciladorAmortiguado.calcularAceleracion(rNext, vPredNext); // Usa vPredNext

            // Corrector para velocidad
            double vCorrNext = vCurr + (1.0/6.0) * (2 * aNext + 5 * aCurr - aPrev) * dt;

            // Actualizar variables
            rCurr = rNext;
            vCurr = vCorrNext;
            aPrev = aCurr;
            aCurr = aNext;
            t += dt;
        }
    }
}
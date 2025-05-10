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
        // Estado actual
        double rCurr = OsciladorAmortiguado.POSICION_INICIAL;
        double vCurr = OsciladorAmortiguado.VELOCIDAD_INICIAL;
        double aCurr = OsciladorAmortiguado.calcularAceleracion(rCurr, vCurr);

        // Inicialización del estado anterior usando Euler
        double rPrev = rCurr - vCurr * dt + (dt * dt / 2) * aCurr;
        double vPrev = vCurr - (dt) * aCurr;
        double aPrev = OsciladorAmortiguado.calcularAceleracion(rPrev, vPrev);

        double t = 0;
        tiempos.clear();
        posicionesNumericas.clear();

        for (int step = 0; t <= tiempoTotal + dt/2.0; step++) {
            double rAnalitica = SolucionAnalitica.calcularPosicion(t);
            if (writer != null) {
                writer.printf("%.16f,%.16f,%.16f\n", t, rAnalitica, rCurr);
            }
            tiempos.add(t);
            posicionesNumericas.add(rCurr);

            if (t >= tiempoTotal - dt/2.0 && t > 0) break;

            // Predictor
            double rNext = rCurr + vCurr * dt + (2.0/3.0) * aCurr * dt * dt - (1.0/6.0) * aPrev * dt * dt;
            double vPredNext = vCurr + (3.0/2.0) * aCurr * dt - (1.0/2.0) * aPrev * dt;

            // Evaluar aceleración en t+dt
            double aNext = OsciladorAmortiguado.calcularAceleracion(rNext, vPredNext);

            // Corrector para velocidad
            double vCorrNext = vCurr + (1.0/3.0) * aNext * dt + (5.0/6.0) * aCurr * dt - (1.0/6.0) * aPrev * dt;

            // Actualizar variables
            aPrev = aCurr;

            rCurr = rNext;
            vCurr = vCorrNext;
            aCurr = aNext;

            t += dt;
        }
    }
}
package com.simulacion.integradores;

import com.simulacion.OsciladorAmortiguado;
import com.simulacion.SolucionAnalitica;

import java.io.PrintWriter;
import java.util.List;

public class VerletOriginal implements Integrador {
    @Override
    public String getNombre() {
        return "Verlet";
    }

    @Override
    public void simular(double dt, double tiempoTotal, PrintWriter writer, List<Double> tiempos, List<Double> posicionesNumericas) {
        double rCurr = OsciladorAmortiguado.POSICION_INICIAL;
        double vCurr = OsciladorAmortiguado.VELOCIDAD_INICIAL; // Necesaria para la fuerza y rPrev

        // Estimar r(t-dt) para el primer paso: r(-dt) = r(0) - v(0)*dt + a(0)*dt^2/2
        double a0 = OsciladorAmortiguado.calcularAceleracion(rCurr, vCurr);
        double rPrev = rCurr - vCurr * dt + 0.5 * a0 * dt * dt;

        double t = 0;
        tiempos.clear();
        posicionesNumericas.clear();

        // Cabecera específica para Verlet en su archivo (o usar una general en MainSimulacion)
        // writer.println("tiempo,posicion_analitica,posicion_verlet");


        for (int step = 0; t <= tiempoTotal; step++) {
            double rAnalitica = SolucionAnalitica.calcularPosicion(t);
            // Guardar datos para el tiempo actual ANTES de avanzar
            if (writer != null) {
                writer.printf("%.16f,%.16f,%.16f\n", t, rAnalitica, rCurr);
            }
            tiempos.add(t);
            posicionesNumericas.add(rCurr);

            if (t >= tiempoTotal) break; // Evitar un paso extra si t es exactamente tiempoTotal

            // Calcular la aceleración actual usando rCurr y vCurr
            // La velocidad vCurr se puede estimar mejor una vez que se tiene rNext,
            // pero para la fuerza en este paso se necesita v(t).
            // Una aproximación común para v(t) en el esquema de Størmer-Verlet
            // cuando la fuerza depende de la velocidad es calcular v(t) con las posiciones
            // conocidas *antes* de calcular rNext.
            // Si es el primer paso (step 0), vCurr es v0.
            // Para los siguientes, vCurr = (rCurr - rPrev) / dt; NO, esto es v(t-dt/2)
            // O vCurr = (rNext - rPrev) / (2*dt) -> esta v está centrada en t
            // Para el cálculo de a(t), necesitamos v(t).
            // Usaremos la velocidad inicial para a(0). Para t>0, v(t) es más tricky.
            // Opción 1: Usar v(t) del paso anterior (puede introducir error)
            // Opción 2: Iterar o usar una forma de Velocity Verlet.
            // La forma más simple de Verlet con amortiguamiento:
            // r(t+dt) = 2r(t) - r(t-dt) + (F(r(t), v(t)) / m) * dt^2
            // Donde v(t) se estima como (r(t) - r(t-dt)) / dt;  (hacia atrás)
            // o (r(t+dt) - r(t-dt)) / (2dt) (centrada, requiere r(t+dt)) -> implícito o predictor-corrector

            // Mantengamos la estimación de vCurr del paso anterior o inicial.
            // Para el cálculo de la aceleración actual, necesitamos r(t) y v(t).
            // r(t) es rCurr.
            // v(t) es vCurr.
            // En el primer paso (t=0), rCurr=r0, vCurr=v0.
            // En pasos subsiguientes, vCurr debe actualizarse.
            // La velocidad en Verlet se calcula a posteriori: v(t) = (r(t+dt) - r(t-dt)) / (2dt)
            // Para la fuerza F(t) = -k*r(t) - gamma*v(t), necesitamos v(t).
            // Usaremos la vCurr del paso anterior para calcular aCurr.
            // Esto es una simplificación. Un método más robusto sería una variante de Velocity Verlet.

            double aCurr = OsciladorAmortiguado.calcularAceleracion(rCurr, vCurr);
            double rNext = 2 * rCurr - rPrev + aCurr * dt * dt;

            // Actualizar vCurr para el *próximo* cálculo de aceleración (en t+dt)
            // Esta velocidad es v(t+dt)
            vCurr = (rNext - rPrev) / (2 * dt); // v(t) centrado alrededor de rCurr

            rPrev = rCurr;
            rCurr = rNext;
            t += dt;
        }
    }
}
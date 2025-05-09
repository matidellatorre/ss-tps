package com.simulacion.integradores;

import com.simulacion.OsciladorAmortiguado;
import com.simulacion.SolucionAnalitica;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GearPredictorCorrector5 implements Integrador {

    // Coeficientes de Gear para orden 5 (6 valores: r, r', r'', r''', r'''', r''''')
    // donde r'_i = r^(i) * dt^i / i!
    // Para la corrección: r_i_corr = r_i_pred + COEF_CORRECCION[i] * delta_R2
    private static final double[] COEF_CORRECCION_GEAR_ORDEN_5 = {
            3.0 / 20.0,    // Para r0 (posición)
            251.0 / 360.0, // Para r1 (velocidad * dt)
            1.0,           // Para r2 (aceleración * dt^2 / 2)
            11.0 / 18.0,   // Para r3
            1.0 / 6.0,     // Para r4
            1.0 / 60.0     // Para r5
    };
    // Alternativamente, los coeficientes usados en Allen & Tildesley son para la fórmula
    // r_i_corr = r_i_pred + alpha_i * delta_R2, donde alpha_i pueden ser los mismos que arriba
    // dependiendo de la derivación exacta. Los valores de arriba son comunes.

    private double[] rp; // Almacena las derivadas escaladas: r, v*dt, a*dt^2/2, ...

    @Override
    public String getNombre() {
        return "GearPC5";
    }

    // Constructor para inicializar rp si es necesario fuera de simular
    public GearPredictorCorrector5() {
        rp = new double[6]; // r, r(1), r(2), r(3), r(4), r(5) -> derivadas escaladas
    }

    private void inicializarDerivadas(double r0, double v0, double dt) {
        double m = OsciladorAmortiguado.MASA;
        double k = OsciladorAmortiguado.CONSTANTE_RESORTE;
        double gamma = OsciladorAmortiguado.COEFICIENTE_AMORTIGUAMIENTO;

        double r_act = r0;
        double v_act = v0;
        double a_act = OsciladorAmortiguado.calcularAceleracion(r_act, v_act);
        double b_act = OsciladorAmortiguado.calcularDerivadaAceleracion(v_act, a_act);       // a_dot
        double c_act = OsciladorAmortiguado.calcularSegundaDerivadaAceleracion(a_act, b_act); // a_ddot
        double d_act = OsciladorAmortiguado.calcularTerceraDerivadaAceleracion(b_act, c_act); // a_dddot

        rp[0] = r_act;
        rp[1] = v_act * dt;
        rp[2] = a_act * dt * dt / 2.0;
        rp[3] = b_act * dt * dt * dt / 6.0;
        rp[4] = c_act * dt * dt * dt * dt / 24.0;
        rp[5] = d_act * dt * dt * dt * dt * dt / 120.0;
    }


    @Override
    public void simular(double dt, double tiempoTotal, PrintWriter writer, List<Double> tiempos, List<Double> posicionesNumericas) {
        inicializarDerivadas(OsciladorAmortiguado.POSICION_INICIAL, OsciladorAmortiguado.VELOCIDAD_INICIAL, dt);

        double t = 0;
        tiempos.clear();
        posicionesNumericas.clear();

        // writer.println("tiempo,posicion_analitica,posicion_gear");

        // Almacenar rp predichos temporalmente
        double[] rp_pred = new double[6];

        for (int step = 0; t <= tiempoTotal + dt/2.0; step++) {
            double rAnalitica = SolucionAnalitica.calcularPosicion(t);
            if (writer != null) {
                writer.printf("%.16f,%.16f,%.16f\n", t, rAnalitica, rp[0]); // rp[0] es la posición actual
            }
            tiempos.add(t);
            posicionesNumericas.add(rp[0]);

            if (t >= tiempoTotal - dt/2.0 && t > 0) break;

            // 1. Predecir (Expansión de Taylor con derivadas escaladas)
            // rp_pred[0] = rp[0] + rp[1] + rp[2] + rp[3] + rp[4] + rp[5];
            // rp_pred[1] = rp[1] + 2*rp[2] + 3*rp[3] + 4*rp[4] + 5*rp[5];
            // rp_pred[2] = rp[2] + 3*rp[3] + 6*rp[4] + 10*rp[5];
            // rp_pred[3] = rp[3] + 4*rp[4] + 10*rp[5]; // Corrección: 4rp4 + 10rp5
            // rp_pred[4] = rp[4] + 5*rp[5];
            // rp_pred[5] = rp[5];
            //Coeficientes de predicción (matriz triangular de Pascal)
            rp_pred[5] = rp[5];
            rp_pred[4] = rp[4] + rp_pred[5]; // En realidad es rp[4] + 1*rp[5]
            rp_pred[3] = rp[3] + rp_pred[4]; // rp[3] + (rp[4]+rp[5]) = rp[3]+rp[4]+rp[5]
            rp_pred[2] = rp[2] + rp_pred[3];
            rp_pred[1] = rp[1] + rp_pred[2];
            rp_pred[0] = rp[0] + rp_pred[1];
            // La forma correcta usando coeficientes binomiales (más clara):
            // y_i_pred = sum_{j=i to p} C(j,i) * y_j_actual
            // O, para derivadas escaladas y_k = f^(k) h^k / k!
            // y_0_p = y_0 + y_1 + y_2 + y_3 + y_4 + y_5
            // y_1_p = y_1 + 2*y_2 + 3*y_3 + 4*y_4 + 5*y_5
            // y_2_p = y_2 + 3*y_3 + 6*y_4 + 10*y_5
            // y_3_p = y_3 + 4*y_4 + 10*y_5
            // y_4_p = y_4 + 5*y_5
            // y_5_p = y_5
            // (Usando los rp actuales para predecir los rp_pred)
            rp_pred[0] = rp[0] + rp[1] + rp[2] + rp[3] + rp[4] + rp[5];
            rp_pred[1] = rp[1] + 2*rp[2] + 3*rp[3] + 4*rp[4] + 5*rp[5];
            rp_pred[2] = rp[2] + 3*rp[3] + 6*rp[4] + 10*rp[5];
            rp_pred[3] = rp[3] + 4*rp[4] + 10*rp[5];
            rp_pred[4] = rp[4] + 5*rp[5];
            rp_pred[5] = rp[5];


            // 2. Evaluar la aceleración con los valores predichos
            // Necesitamos r_pred y v_pred
            double r_p = rp_pred[0];
            double v_p = rp_pred[1] / dt; // ya que rp_pred[1] = v_pred * dt
            double a_eval = OsciladorAmortiguado.calcularAceleracion(r_p, v_p);

            // 3. Calcular la diferencia en la aceleración (escalada)
            // delta_R2 = (a_eval * dt^2 / 2) - rp_pred[2]
            double delta_R2 = (a_eval * dt * dt / 2.0) - rp_pred[2];

            // 4. Corregir
            for (int i = 0; i < 6; i++) {
                rp[i] = rp_pred[i] + COEF_CORRECCION_GEAR_ORDEN_5[i] * delta_R2;
            }

            // Asegurar que rp[2] corresponda exactamente a la aceleración evaluada (escalada)
            // rp[2] = a_eval * dt * dt / 2.0; // Esto es lo que hace el coeficiente de corrección 1.0 para rp[2]

            t += dt;
        }
    }
}

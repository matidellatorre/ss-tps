package oscillator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OscillatorSimulation {

    public static void main(String[] args) {
        // Pasos temporales a probar
        double[] dts = {1e-2, 1e-3, 1e-4, 1e-5, 1e-6};

        System.out.println("Simulación de Oscilador Amortiguado");
        System.out.println("Parámetros: m=" + Constants.M + " kg, k=" + Constants.K + " N/m, gamma=" + Constants.GAMMA + " kg/s");
        System.out.println("Condiciones Iniciales: r(0)=" + Constants.R0 + " m, v(0)=" + String.format(Locale.US, "%.4f", Constants.V0) + " m/s");
        System.out.println("Tiempo final: " + Constants.TF + " s");
        System.out.println("----------------------------------------------------");


        for (double dt : dts) {
            System.out.printf(Locale.US, "Simulando con dt = %.0e s\n", dt);
            String fileName = String.format(Locale.US, "resultados_dt_%.0e.csv", dt).replace(',', '.');

            Integrator verlet = new VerletIntegrator(Constants.R0, Constants.V0, dt);
            Integrator beeman = new BeemanIntegrator(Constants.R0, Constants.V0, dt);
            Integrator gear = new GearIntegrator(Constants.R0, Constants.V0, dt);

            List<Double> errorsVerlet = new ArrayList<>();
            List<Double> errorsBeeman = new ArrayList<>();
            List<Double> errorsGear = new ArrayList<>();

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println("tiempo,posicion_analitica,posicion_verlet,posicion_beeman,posicion_gear");

                double currentTime = 0;
                // El número de pasos es TF/dt. El bucle va de 0 a N_STEPS inclusive (N_STEPS+1 puntos)
                int numSteps = (int) (Constants.TF / dt);

                for (int i = 0; i <= numSteps; i++) {
                    currentTime = i * dt;
                    // Asegurar que no excedemos TF por errores de punto flotante en la última iteración
                    if (currentTime > Constants.TF + dt / 2.0 && i == numSteps) { // Corrección si currentTime se pasa un poco
                        currentTime = Constants.TF;
                    } else if (currentTime > Constants.TF + dt / 2.0) { // Si se pasa antes, algo está mal
                        break;
                    }


                    double rAna = AnalyticalSolution.getPosition(currentTime);
                    double rVerlet = verlet.getPosition();
                    double rBeeman = beeman.getPosition();
                    double rGear = gear.getPosition();

                    writer.printf("%.16f,%.16f,%.16f,%.16f,%.16f\n",
                            currentTime, rAna, rVerlet, rBeeman, rGear);

                    // Acumular errores (evitar el primer punto si es t=0 ya que el error sería 0 por CI)
                    // Sin embargo, para el ECM es mejor incluirlos si el primer paso ya tiene error numérico.
                    // Aquí, las posiciones iniciales de los integradores son exactas.
                    // El primer error real vendrá después del primer step().
                    // Si currentTime > ~0 (o i > 0) sería más preciso para el ECM del método.
                    // Por simplicidad, incluimos todos los puntos.
                    errorsVerlet.add(Math.pow(rVerlet - rAna, 2));
                    errorsBeeman.add(Math.pow(rBeeman - rAna, 2));
                    errorsGear.add(Math.pow(rGear - rAna, 2));

                    if (i < numSteps) { // Solo dar un paso si no es la última iteración (ya se registró el estado en TF)
                        verlet.step();
                        beeman.step();
                        gear.step();
                    }
                }

                // Calcular ECM (Error Cuadrático Medio)
                double ecmVerlet = Math.sqrt(errorsVerlet.stream().mapToDouble(val -> val).average().orElse(0.0));
                double ecmBeeman = Math.sqrt(errorsBeeman.stream().mapToDouble(val -> val).average().orElse(0.0));
                double ecmGear = Math.sqrt(errorsGear.stream().mapToDouble(val -> val).average().orElse(0.0));

                System.out.printf(Locale.US, "  Archivo de salida: %s\n", fileName);
                System.out.printf(Locale.US, "  ECM Verlet: %.4e\n", ecmVerlet);
                System.out.printf(Locale.US, "  ECM Beeman: %.4e\n", ecmBeeman);
                System.out.printf(Locale.US, "  ECM Gear  : %.4e\n", ecmGear);


            } catch (IOException e) {
                System.err.println("Error al escribir el archivo para dt=" + dt + ": " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("----------------------------------------------------");
        }
        System.out.println("Simulación completada.");
    }
}
package com.simulacion;

import com.simulacion.integradores.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainSimulacion {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US); // Para que los doubles usen "." como separador decimal

        // Diferentes pasos temporales a probar
        double[] dts = {1E-2, 5E-3, 1E-3, 5E-4, 1E-4, 5E-5, 1E-5};
//        double[] dts = {1E-4}; // Para pruebas rápidas

        List<Integrador> integradores = List.of(
                new VerletOriginal(),
                new Beeman(),
                new GearPredictorCorrector5()
        );

        // Archivo para los errores cuadráticos medios
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (PrintWriter ecmWriter = new PrintWriter(new File(outputDir, "errores_ecm.txt"))) {
            ecmWriter.println("dt,ecm_verlet,ecm_beeman,ecm_gear");

            for (double dt : dts) {
                System.out.printf("Simulando con dt = %.0E\n", dt);
                ecmWriter.printf("%.6E", dt);

                // Archivo de salida para este dt (un solo archivo con todas las columnas)
                String nombreArchivoSalida = String.format("output/resultados_dt_%.0E.txt", dt).replace(",", ".");

                // Para almacenar temporalmente las posiciones de cada método para este dt
                List<List<Double>> todasLasPosicionesNumericas = new ArrayList<>();
                List<Double> tiemposSimulacion = new ArrayList<>(); // Solo necesitamos una lista de tiempos

                // Ejecutar cada integrador
                for (int i = 0; i < integradores.size(); i++) {
                    Integrador integrador = integradores.get(i);
                    List<Double> posicionesNum = new ArrayList<>();
                    List<Double> tiemposActuales = new ArrayList<>(); // Tiempos para este integrador

                    // La simulación del integrador llenará tiemposActuales y posicionesNum
                    // No le pasamos el writer aquí, lo haremos después para armar el archivo consolidado
                    integrador.simular(dt, OsciladorAmortiguado.TIEMPO_FINAL, null, tiemposActuales, posicionesNum);

                    todasLasPosicionesNumericas.add(posicionesNum);
                    if (i == 0) { // Tomar los tiempos del primer integrador (deberían ser iguales)
                        tiemposSimulacion.addAll(tiemposActuales);
                    }

                    // Calcular ECM
                    double ecm = calcularECM(tiemposActuales, posicionesNum);
                    ecmWriter.printf(",%.18E", ecm);
                    System.out.printf("  ECM %s: %.3E\n", integrador.getNombre(), ecm);
                }
                ecmWriter.println();

                // Escribir el archivo de resultados consolidado para este dt
                try (PrintWriter resultadosWriter = new PrintWriter(new File(nombreArchivoSalida))) {
                    // Cabecera
                    resultadosWriter.print("tiempo,posicion_analitica");
                    for (Integrador integrador : integradores) {
                        resultadosWriter.print("," + "posicion_" + integrador.getNombre().toLowerCase());
                    }
                    resultadosWriter.println();

                    // Datos
                    for (int i = 0; i < tiemposSimulacion.size(); i++) {
                        double tiempoActual = tiemposSimulacion.get(i);
                        double posAnalitica = SolucionAnalitica.calcularPosicion(tiempoActual);
                        resultadosWriter.printf("%.16f,%.16f", tiempoActual, posAnalitica);
                        for (List<Double> posicionesNumMetodo : todasLasPosicionesNumericas) {
                            if (i < posicionesNumMetodo.size()) { // Asegurar que haya datos
                                resultadosWriter.printf(",%.16f", posicionesNumMetodo.get(i));
                            } else {
                                resultadosWriter.print(","); // Dato faltante si las longitudes no coinciden
                            }
                        }
                        resultadosWriter.println();
                    }
                    System.out.println("Archivo de resultados generado: " + nombreArchivoSalida);
                } catch (FileNotFoundException e) {
                    System.err.println("Error al escribir el archivo de resultados para dt=" + dt + ": " + e.getMessage());
                }
            }
            System.out.println("Archivo de ECM generado: output/errores_ecm.txt");

        } catch (FileNotFoundException e) {
            System.err.println("Error al crear el archivo de ECM: " + e.getMessage());
        }
        System.out.println("Simulación completada.");
    }

    public static double calcularECM(List<Double> tiempos, List<Double> posicionesNumericas) {
        if (tiempos.isEmpty() || posicionesNumericas.isEmpty() || tiempos.size() != posicionesNumericas.size()) {
            return Double.NaN; // No se puede calcular
        }
        double sumaErroresCuadrados = 0;
        int n = tiempos.size();

        for (int i = 0; i < n; i++) {
            double t = tiempos.get(i);
            double rNum = posicionesNumericas.get(i);
            double rAnalitica = SolucionAnalitica.calcularPosicion(t);
            sumaErroresCuadrados += Math.pow(rNum - rAnalitica, 2);
        }
        return Math.sqrt(sumaErroresCuadrados / n);
    }
}
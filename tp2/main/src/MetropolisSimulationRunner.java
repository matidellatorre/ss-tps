import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MetropolisSimulationRunner {
    public static void main(String[] args) {
        // Parámetros de simulación
        int N = 50; // Tamaño de la grilla
        int monteCarloSteps = 30000; // Pasos de Monte Carlo

        // Valores de p a simular (con mayor densidad cerca de la transición)
        // La transición crítica ocurre alrededor de p=0.075-0.085 para este modelo
        double[] pValues = {0.05, 0.07, 0.0825, 0.084, 0.0875, 0.093, 0.0945, 0.095, 0.1, 0.105, 0.11, 0.13, 0.15, 0.17, 0.19, 0.21, 0.23};

        // Crear directorio para guardar los resultados
        File resultsDir = new File("resultados");
        if (!resultsDir.exists()) {
            resultsDir.mkdir();
        }

        // Ejecutar simulaciones para cada valor de p
        for (double p : pValues) {
            System.out.println("\n==================================================");
            System.out.println("Iniciando simulación con p = " + p);

            // Crear archivo de configuración para este valor de p
            String configFilePath = createConfigFile(N, p);

            // Ejecutar la simulación
            MetropolisMonteCarloC simulation = new MetropolisMonteCarloC(configFilePath);
            String outputFilePath = "resultados/resultados_p" + p + ".txt";
            simulation.runSimulation(monteCarloSteps, outputFilePath);

            // Renombrar el archivo de magnetización
            File magnetFile = new File("magnetizacion.txt");
            File newMagnetFile = new File("resultados/magnetizacion_p" + p + ".txt");
            magnetFile.renameTo(newMagnetFile);

            System.out.println("Simulación completada para p = " + p);
            System.out.println("==================================================");
        }

        System.out.println("Todas las simulaciones completadas. Los resultados están en el directorio 'resultados/'");
    }

    private static String createConfigFile(int N, double p) {

        try {
            Files.createDirectories(Paths.get("./configs"));
        } catch (IOException e) {
            System.err.println("Error al crear el path de configs: " + e.getMessage());
            System.exit(1);
        }

        String configFilePath = "./configs/config_p" + p + ".properties";
        try (FileWriter writer = new FileWriter(configFilePath)) {
            writer.write("N=" + N + "\n");
            writer.write("p=" + p + "\n");
        } catch (IOException e) {
            System.err.println("Error al crear el archivo de configuración: " + e.getMessage());
            System.exit(1);
        }
        return configFilePath;
    }
}
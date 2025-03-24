import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MetropolisMonteCarloC {
    private int[][] grid; // Grilla de opiniones binarias
    private int N; // Tamaño de la grilla NxN
    private double p; // Probabilidad de cambiar de opinión
    private Random random;
    private FileWriter outputFile;

    public MetropolisMonteCarloC(String configFilePath) {
        loadConfiguration(configFilePath);
        grid = new int[N][N];
        random = new Random(123);
        initializeGrid();
    }

    private void loadConfiguration(String configFilePath) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            properties.load(fis);
            N = Integer.parseInt(properties.getProperty("N"));
            p = Double.parseDouble(properties.getProperty("p"));
            System.out.println("Configuración cargada: N=" + N + ", p=" + p);
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo de configuración: " + e.getMessage());
            System.exit(1);
        }
    }

    private void initializeGrid() {
        // Inicializar la grilla con valores aleatorios (1 o -1)
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                grid[i][j] = random.nextBoolean() ? 1 : -1;
            }
        }
    }

    public void runSimulation(int monteCarloSteps, String outputFilePath) {
        try {
            outputFile = new FileWriter(outputFilePath);
            List<Double> magnetizationHistory = new ArrayList<>();
            List<Double> magnetizationSquaredHistory = new ArrayList<>();

            // Escribir estado inicial
            writeGridState(0);
            double mag = calculateMagnetization();
            double magSquared = mag * mag;
            magnetizationHistory.add(mag);
            magnetizationSquaredHistory.add(magSquared);

            // Ejecutar la simulación
            for (int mcs = 1; mcs <= monteCarloSteps; mcs++) {
                performMonteCarloStep();

                if (mcs % 10 == 0) { // Guardar cada 10 pasos para reducir el tamaño del archivo
                    writeGridState(mcs);
                }

                // Calcular magnetización y su cuadrado (para susceptibilidad)
                mag = calculateMagnetization();
                magSquared = mag * mag;
                magnetizationHistory.add(mag);
                magnetizationSquaredHistory.add(magSquared);
            }

            // Guardar el historial de magnetización y magnetización al cuadrado
            FileWriter magFile = new FileWriter("magnetizacion.txt");
            magFile.write("# MCS\tM\tM^2\tStationary\n");
            for (int i = 0; i < magnetizationHistory.size(); i++) {
                magFile.write(i + "\t" + magnetizationHistory.get(i) + "\t" +
                        magnetizationSquaredHistory.get(i) + "\t" + "\n");
            }
            magFile.close();

            outputFile.close();
            System.out.println("Simulación completada. Resultados guardados en " + outputFilePath);

        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo de salida: " + e.getMessage());
            System.exit(1);
        }
    }

    private void performMonteCarloStep() {
        // Un paso de Monte Carlo consiste en N² intentos de actualización
        int totalSites = N * N;

        for (int attempt = 0; attempt < totalSites; attempt++) {
            // Elegir un sitio (i,j) al azar
            int i = random.nextInt(N);
            int j = random.nextInt(N);

            // Calcular el signo de la suma de los 4 vecinos
            int sumNeighbors = getNeighborSum(i, j);
            int majorityOpinion;

            if (sumNeighbors != 0) {
                majorityOpinion = sumNeighbors > 0 ? 1 : -1;
            } else {
                majorityOpinion = grid[i][j]; // En caso de empate, la opinión de la mayoría es la del sitio
            }

            // Decidir si cambiar o no el estado del sitio
            double randomProb = random.nextDouble();

            if (randomProb < p) {
                // Con probabilidad p, cambiar el estado del sitio
                grid[i][j] = -grid[i][j];
            } else {
                // Con probabilidad 1-p, adoptar el estado de la mayoría
                grid[i][j] = majorityOpinion;
            }
        }
    }

    private int getNeighborSum(int i, int j) {
        // Calcular la suma de los 4 vecinos con condiciones periódicas de contorno
        int sum = 0;

        // Vecino superior
        sum += grid[(i - 1 + N) % N][j];

        // Vecino inferior
        sum += grid[(i + 1) % N][j];

        // Vecino izquierdo
        sum += grid[i][(j - 1 + N) % N];

        // Vecino derecho
        sum += grid[i][(j + 1) % N];

        return sum;
    }

    private double calculateMagnetization() {
        double sum = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                sum += grid[i][j];
            }
        }
        return sum / (N * N);
    }

    private boolean isStationary(List<Double> magnetizationHistory, double threshold) {
        if (magnetizationHistory.size() < 100) return false; // Necesitamos suficientes muestras

        // Tomar los últimos 100 valores
        int start = Math.max(0, magnetizationHistory.size() - 100);
        List<Double> recent = magnetizationHistory.subList(start, magnetizationHistory.size());

        // Usar el valor absoluto para evitar oscilaciones alrededor de cero
        List<Double> absMag = new ArrayList<>();
        for (Double m : recent) {
            absMag.add(Math.abs(m));
        }

        double mean = absMag.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
        double variance = absMag.stream().mapToDouble(m -> Math.pow(m - mean, 2)).sum() / absMag.size();
        double stdDev = Math.sqrt(variance);

        // Calcular también la pendiente de la regresión lineal
        double slope = calculateSlope(recent);

        // Es estacionario si la desviación estándar es baja y la pendiente cercana a cero
        return stdDev < threshold && Math.abs(slope) < 0.0001;
    }

    private double calculateSlope(List<Double> values) {
        int n = values.size();
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumXX = 0.0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = Math.abs(values.get(i)); // Valor absoluto

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        // Fórmula de regresión lineal
        return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
    }

    private void writeGridState(int mcs) throws IOException {
        outputFile.write("MCS=" + mcs + "\n");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                outputFile.write(grid[i][j] + " ");
            }
            outputFile.write("\n");
        }
        outputFile.write("\n");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java MetropolisMonteCarlo <archivo_config> <pasos_monte_carlo>");
            System.exit(1);
        }

        String configFile = args[0];
        int monteCarloSteps = Integer.parseInt(args[1]);
        String outputFile = "resultados_simulacion.txt";

        MetropolisMonteCarloC simulation = new MetropolisMonteCarloC(configFile);
        simulation.runSimulation(monteCarloSteps, outputFile);
    }
}
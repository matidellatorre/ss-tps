import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {

    static int getNextPosition(int coord) {
        return Constants.getBoundaryCond() ? (coord + 1) % Constants.getM() : coord + 1;
    }

    static int getPrevPosition(int coord) {
        return Constants.getBoundaryCond() ? (coord - 1 + Constants.getM()) % Constants.getM() : coord - 1;
    }

    static Cell getParticleCell(Particle particle) {
        int cellX = (int)Math.floor(particle.getX() / Constants.getCellLen());
        int cellY = (int)Math.floor(particle.getY() / Constants.getCellLen());
        return new Cell(cellX, cellY);
    }

    static Map<Integer, Set<Integer>> findNeighborsBruteForce(List<Particle> particles) {
        long startTime = System.currentTimeMillis();

        Map<Integer, Set<Integer>> nearParticles = new HashMap<>();
        particles.forEach(p -> nearParticles.put(p.getId(), new HashSet<>()));
        double rc = Constants.getRc();

        for (int i = 0; i < particles.size(); i++) {
            Particle particle1 = particles.get(i);
            for (int j = 0; j < particles.size(); j++) {
                if (i == j) continue; // Skip same particle

                Particle particle2 = particles.get(j);

                if(particle1.distanceTo(particle2) - particle1.getRadius() - particle2.getRadius() <= rc) {
                    nearParticles.get(particle1.getId()).add(particle2.getId());
                }
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Brute force execution time: " + (endTime - startTime) + " ms");

        return nearParticles;
    }

    static Map<Integer, Set<Integer>> findNeighborsCIM(List<Particle> particles, Map<Cell, List<Particle>> particlesByCell) {
        long startTime = System.currentTimeMillis();

        Map<Integer, Set<Integer>> nearParticles = new HashMap<>();
        particles.forEach(p -> nearParticles.put(p.getId(), new HashSet<>()));
        double rc = Constants.getRc();

        particles.forEach(particle -> {
            Cell cell = getParticleCell(particle);
            List<Cell> neighbourCells = getNeighbourCells(cell);
            neighbourCells.add(cell);
            neighbourCells.forEach(n -> {
                List<Particle> nearParticlesList = particlesByCell.getOrDefault(n, new ArrayList<>());

                for (Particle neighbor : nearParticlesList) {
                    if (particle.getId() != neighbor.getId() &&
                            particle.distanceTo(neighbor) - particle.getRadius() - neighbor.getRadius() <= rc) {
                        nearParticles.get(particle.getId()).add(neighbor.getId());
                        nearParticles.get(neighbor.getId()).add(particle.getId());
                    }
                }
            });
        });

        long endTime = System.currentTimeMillis();
        System.out.println("Cell Index Method execution time: " + (endTime - startTime) + " ms");

        return nearParticles;
    }

    static void writeResultsToFile(String fileName, Map<Integer, Set<Integer>> results) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            results.forEach((key, value) -> {
                try {
                    fileWriter.write(String.format("%d: %s\n", key, value.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    static List<Cell> getNeighbourCells(Cell cell) {
        return new ArrayList<>(List.of(new Cell(cell.getX(), getPrevPosition(cell.getY())), new Cell(getNextPosition(cell.getX()), getPrevPosition(cell.getY())), new Cell(getNextPosition(cell.getX()), cell.getY()), new Cell(getNextPosition(cell.getX()), getNextPosition(cell.getY()))));
    }

    public static void main(String[] args) {

        //Command line arguments: static_file_path, dynamic_file_path, m, rc, boolean indicating if using condicion de contorno o no

        String staticFile = args[0];
        String dynamicFile = args[1];
        List<Particle> particles;
        AtomicInteger counter = new AtomicInteger(1);
        Map<Cell, List<Particle>> particlesByCell = new HashMap<>();
        List<Double> particleRadii = new ArrayList<>();

        Path staticPath = Path.of(staticFile);
        try {
            List<String> lines = Files.lines(staticPath).toList();
            int n = Integer.parseInt(lines.get(0));
            double l = Double.parseDouble(lines.get(1));
            int m = Integer.parseInt(args[2]);
            double rc = Double.parseDouble(args[3]);
            Constants.initialize(m, n, l, rc, Boolean.parseBoolean(args[4]));

            if (l/m < rc) throw new InvalidParameterException("L/M must be lower than Rc");

            // Read particle radius from the static file
            for (int i = 2; i < Math.min(n + 2, lines.size()); i++) {
                String line = lines.get(i);
                String[] parts = line.trim().split("\\s+");
                double radius = Double.parseDouble(parts[0]);
                particleRadii.add(radius);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        Path dynamicPath = Path.of(dynamicFile);
        try {
            particles = Files.lines(dynamicPath)
                    .skip(1)
                    .filter(line -> !line.trim().isEmpty()) // Skip empty lines
                    .map(line -> line.trim().split("\\s+"))
                    .filter(parts -> parts.length >= 2) // Ensure we have enough parts
                    .map(parts -> new Particle(
                            Double.parseDouble(parts[0]),
                            Double.parseDouble(parts[1]),
                            counter.getAndIncrement(),
                            particleRadii.get(counter.get() - 2)
                    ))
                    .toList();
        } catch (IOException e) {
            System.out.println("Error reading dynamic file: " + e.getMessage());
            return;
        }

        // Create cell mapping
        particles.forEach(particle -> {
            Cell cell = getParticleCell(particle);
            List<Particle> list = particlesByCell.getOrDefault(cell, new ArrayList<>());
            list.add(particle);
            particlesByCell.put(cell, list);
        });

        // Execute and measure CIM
        Map<Integer, Set<Integer>> cimResults = findNeighborsCIM(particles, particlesByCell);

        // Execute and measure Brute Force
        Map<Integer, Set<Integer>> bruteForceResults = findNeighborsBruteForce(particles);

        // Verify results are the same (optional)
        boolean resultsMatch = true;
        for (Integer particleId : cimResults.keySet()) {
            if (!cimResults.get(particleId).equals(bruteForceResults.get(particleId))) {
                resultsMatch = false;
                System.out.println("Results don't match for particle ID: " + particleId);
                break;
            }
        }
        System.out.println("Results match: " + resultsMatch);

        writeResultsToFile("output_" + Constants.getN() + "_" + "rc" + Constants.getRc() + "_cim.txt", cimResults);
        writeResultsToFile("output_" + Constants.getN() + "_" + "rc" + Constants.getRc() + "_bruteForce.txt", bruteForceResults);
    }
}
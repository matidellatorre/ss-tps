import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {

    static int getNextPosition(int coord) {
        return Constants.getBoundaryCond()?(coord+1)%Constants.getM():coord+1;
    }

    static int getPrevPosition(int coord) {
        return Constants.getBoundaryCond()?(coord-1)%Constants.getM():coord-1;
    }

    static Cell getParticleCell(Particle particle) {
        int cellX = (int)Math.floor(particle.getX()/Constants.getCellLen());
        int cellY = (int)Math.floor(particle.getY()/Constants.getCellLen());
        return new Cell(cellX, cellY);
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

        Path staticPath = Path.of(staticFile);
        try {
            List<String> lines = Files.lines(staticPath).toList();
            int n = Integer.parseInt(lines.get(0));
            double l = Double.parseDouble(lines.get(1));
            int m = Integer.parseInt(args[2]);
            double rc = Double.parseDouble(args[3]);
            Constants.initialize(m, n, l, rc, Boolean.parseBoolean(args[4]));
        } catch (IOException e) {
            System.out.println("Error reading static file: "+ e.getMessage());
            return;
        }

        Path dynamicPath = Path.of(dynamicFile);
        try {
            particles = Files.lines(dynamicPath)
                    .skip(1)
                    .map(line -> line.trim().split("\\s+"))
                    .map(parts -> new Particle(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), counter.getAndIncrement()))
                    .toList();
        } catch (IOException e) {
            System.out.println("Error reading dynamic file: "+ e.getMessage());
            return;
        }

        particles.forEach(particle -> {
            Cell cell = getParticleCell(particle);
            List<Particle> list = particlesByCell.get(cell);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(particle);
            particlesByCell.put(cell, list);
        }); {
        }

        Map<Integer, Set<Integer>> nearParticles = new HashMap<>();
        particles.forEach(p -> nearParticles.put(p.getId(), new HashSet<>()));

        particles.forEach(particle -> {
            Cell cell = getParticleCell(particle);
            List<Cell> neighbourCells = getNeighbourCells(cell);
            neighbourCells.add(cell);
            neighbourCells.forEach(n -> {
                List<Particle> nearParticlesList = particlesByCell.getOrDefault(n, new ArrayList<>());

                for (Particle neighbor : nearParticlesList) {
                    if (particle.getId() != neighbor.getId() && particle.distanceTo(neighbor) <= Constants.getRc()) {
                        nearParticles.get(particle.getId()).add(neighbor.getId());
                        nearParticles.get(neighbor.getId()).add(particle.getId());
                    }
                }
            });
        });

        try (FileWriter fileWriter = new FileWriter("output_" + Constants.getN() + "_" + "rc" + Constants.getRc())) {
            nearParticles.forEach((key, value) ->
            {
                try {
                    fileWriter.write(String.format("[%d: %s]\n", key, value.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(" "))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}
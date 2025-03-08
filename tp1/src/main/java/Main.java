import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    static Cell getParticleCell(Particle particle) {
        int cellX = (int)Math.floor(particle.getX()/Constants.cellLen);
        int cellY = (int)Math.floor(particle.getY()/Constants.cellLen);
        return new Cell(cellX, cellY);
    }

    static List<Cell> getNeighbourCells(Cell cell) {
        return List.of(new Cell(cell.getX(), cell.getY()-1), new Cell(cell.getX()+1, cell.getY()-1), new Cell(cell.getX()+1, cell.getY()), new Cell(cell.getX()+1, cell.getY()+1));
    }

    public static void main(String[] args) {

        List<Particle> particles;
        AtomicInteger counter = new AtomicInteger(1);
        Map<Cell, List<Particle>> particlesByCell = new HashMap<>();

        Path path = Path.of(System.getProperty("user.home"), "Desktop", "DynamicTest.txt"); //TODO: Change this path
        try {
            particles = Files.lines(path)
                    .skip(1)
                    .map(line -> line.trim().split("\\s+"))
                    .map(parts -> new Particle(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), counter.getAndIncrement()))
                    .toList();
        } catch (IOException e) {
            System.out.println("Error reading file");
            return;
        }

        //Calculate which particles go to each cell
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

//        particlesByCell.forEach((key, value) -> System.out.println(key + " has particles " + value));

        //Print list of neighbours of each particle
        particles.forEach(particle -> {
            Cell cell = getParticleCell(particle);
            List<Cell> neighbourCells = getNeighbourCells(cell);
            List<Particle> nearParticles = new ArrayList<>();
            neighbourCells.forEach(n -> nearParticles.addAll(particlesByCell.getOrDefault(n, new ArrayList<>()).stream().filter(p -> p.distanceTo(particle) <= Constants.rc).toList()));
            System.out.printf("Neigbours of particle %d: %s%n", particle.getId(), nearParticles.stream().map(p -> p.getId()).toList()); //TODO: Write this to file
        });


    }

}

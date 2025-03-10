import java.io.FileWriter;
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
        int cellX = (int)Math.floor(particle.getX()/Constants.getCellLen());
        int cellY = (int)Math.floor(particle.getY()/Constants.getCellLen());
        return new Cell(cellX, cellY);
    }

    static List<Cell> getNeighbourCells(Cell cell) {
        return List.of(new Cell(cell.getX(), cell.getY()-1), new Cell(cell.getX()+1, cell.getY()-1), new Cell(cell.getX()+1, cell.getY()), new Cell(cell.getX()+1, cell.getY()+1));
    }

    public static void main(String[] args) {

        //Command line arguments: static_file_path, dynamic_file_path, m, rc, boolean indicating if using condicion de contorno o no

        String staticFile = args[0];
        String dynamicFile = args[1];
        List<Particle> particles;
        AtomicInteger counter = new AtomicInteger(1);
        Map<Cell, List<Particle>> particlesByCell = new HashMap<>();

        System.out.println(args);


        Path staticPath = Path.of(staticFile);
        try {
            List<String> lines = Files.lines(staticPath).toList();
            int n = Integer.parseInt(lines.get(0));
            double l = Double.parseDouble(lines.get(1));
            int m = Integer.parseInt(args[2]);
            double rc = Double.parseDouble(args[3]);
            Constants.initialize(m, n, l, rc);
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

        //Create output file with list of neighbours of each particle

        try {
            FileWriter fileWriter = new FileWriter("output_" + Constants.getN() + "_" + "rc" + Constants.getRc());
            particles.forEach(particle -> {
                Cell cell = getParticleCell(particle);
                List<Cell> neighbourCells = getNeighbourCells(cell);
                List<Particle> nearParticles = new ArrayList<>();
                neighbourCells.forEach(n -> nearParticles.addAll(particlesByCell.getOrDefault(n, new ArrayList<>()).stream().filter(p -> p.distanceTo(particle) <= Constants.getRc()).toList()));
                ;
                try {
                    fileWriter.write(String.format("[%d: %s]\n", particle.getId(), String.join(", ", nearParticles.stream().map(p -> String.valueOf(p.getId())).toList())));
                } catch (IOException e) {
                    System.out.println("Error writing to file: "+e.getMessage());;
                }
            });
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error creating fileWriter: "+e.getMessage());
        }

    }

}


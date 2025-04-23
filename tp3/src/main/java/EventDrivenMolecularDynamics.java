import java.io.*;
import java.util.*;

public class EventDrivenMolecularDynamics {
    // Constantes del sistema
    private static final double L = 0.1; // Diámetro del recinto circular en metros
    private static final double R = 0.005; // Radio de la partícula especial en metros
    private static final double r = 5e-4; // Radio de las partículas normales en metros
    private static final double EPSILON = 1e-10; // Para evitar problemas numéricos

    // Parámetros configurables
    private int N; // Número de partículas normales
    private double v0; // Velocidad inicial de las partículas normales
    private int N_eventos; // Número de eventos a simular
    private int savingFrequency; // Frecuencia para guardar el estado del sistema

    // Estado del sistema
    private Particle[] particles; // Array de partículas
    private PriorityQueue<Event> eventQueue; // Cola de prioridad de eventos
    private double currentTime; // Tiempo actual de la simulación

    // Para salida
    private PrintWriter outputWriter;
    private PrintWriter specialCollisionsWriter;
    private int eventCount;
    private int specialCollisionCount;

    // Clase para representar una partícula
    private static class Particle {
        double x, y; // Posición
        double vx, vy; // Velocidad
        double radius; // Radio
        double mass; // Masa
        boolean isSpecial; // Indica si es la partícula especial

        public Particle(double x, double y, double vx, double vy, double radius, double mass, boolean isSpecial) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.mass = mass;
            this.isSpecial = isSpecial;
        }

        // Actualiza la posición de la partícula al tiempo dado
        public void updatePosition(double time, double deltaTime) {
            x += vx * deltaTime;
            y += vy * deltaTime;
        }

        // Calcula la distancia al cuadrado entre esta partícula y otra
        public double distanceSquared(Particle other) {
            double dx = x - other.x;
            double dy = y - other.y;
            return dx * dx + dy * dy;
        }

        // Calcula la distancia al centro del recinto
        public double distanceToCenter() {
            return Math.sqrt(x * x + y * y);
        }

        @Override
        public String toString() {
            return String.format("%.6f %.6f %.6f %.6f", x, y, vx, vy);
        }
    }

    // Clase para representar un evento (colisión)
    private static class Event implements Comparable<Event> {
        double time; // Tiempo en que ocurre el evento
        int particle1; // Índice de la primera partícula involucrada
        int particle2; // Índice de la segunda partícula (-1 si es colisión con pared)
        int countP1; // Contador de colisiones de la partícula 1
        int countP2; // Contador de colisiones de la partícula 2

        public Event(double time, int particle1, int particle2, int countP1, int countP2) {
            this.time = time;
            this.particle1 = particle1;
            this.particle2 = particle2;
            this.countP1 = countP1;
            this.countP2 = countP2;
        }

        @Override
        public int compareTo(Event other) {
            return Double.compare(this.time, other.time);
        }
    }

    // Constructor
    public EventDrivenMolecularDynamics(int N, double v0, int N_eventos, int savingFrequency) {
        this.N = N;
        this.v0 = v0;
        this.N_eventos = N_eventos;
        this.savingFrequency = savingFrequency;
        this.particles = new Particle[N + 1]; // +1 para la partícula especial
        this.eventQueue = new PriorityQueue<>();
        this.currentTime = 0.0;
        this.eventCount = 0;
        this.specialCollisionCount = 0;
    }

    // Inicializa el sistema
    public void initialize() {
        Random random = new Random(System.currentTimeMillis());

        // Crear la partícula especial en el centro
        particles[0] = new Particle(0.0, 0.0, 0.0, 0.0, R, 3.0, true);

        // Crear las N partículas normales
        for (int i = 1; i <= N; i++) {
            double x, y;
            boolean overlap;

            do {
                // Generar posición aleatoria dentro del recinto
                double radius = random.nextDouble() * (L/2 - r - EPSILON);
                double angle = random.nextDouble() * 2 * Math.PI;
                x = radius * Math.cos(angle);
                y = radius * Math.sin(angle);

                // Verificar que no haya superposición con partículas existentes
                overlap = false;
                for (int j = 0; j < i; j++) {
                    double dx = x - particles[j].x;
                    double dy = y - particles[j].y;
                    double minDist = (j == 0) ? r + R : r + r;
                    if (dx * dx + dy * dy < minDist * minDist) {
                        overlap = true;
                        break;
                    }
                }
            } while (overlap);

            // Generar velocidad aleatoria
            double angle = random.nextDouble() * 2 * Math.PI;
            double vx = v0 * Math.cos(angle);
            double vy = v0 * Math.sin(angle);

            particles[i] = new Particle(x, y, vx, vy, r, 1.0, false);
        }

        // Programar todos los eventos iniciales
        predictAllEvents();
    }

    // Predice todos los eventos para una partícula
    private void predictEvents(int particleIndex, int[] collisionCounts) {
        Particle p = particles[particleIndex];

        // Predecir colisión con pared
        double timeToWall = predictWallCollision(p);
        if (timeToWall < Double.POSITIVE_INFINITY) {
            eventQueue.add(new Event(currentTime + timeToWall, particleIndex, -1,
                    collisionCounts[particleIndex], -1));
        }

        // Predecir colisiones con otras partículas
        for (int j = 0; j < particles.length; j++) {
            if (j != particleIndex) {
                double timeToCollision = predictParticleCollision(p, particles[j]);
                if (timeToCollision < Double.POSITIVE_INFINITY) {
                    eventQueue.add(new Event(currentTime + timeToCollision, particleIndex, j,
                            collisionCounts[particleIndex], collisionCounts[j]));
                }
            }
        }
    }

    // Predice todos los eventos iniciales
    private void predictAllEvents() {
        int[] collisionCounts = new int[particles.length];
        for (int i = 0; i < particles.length; i++) {
            predictEvents(i, collisionCounts);
        }
    }

    // Predice el tiempo hasta la colisión con la pared
    private double predictWallCollision(Particle p) {
        double dx = p.vx;
        double dy = p.vy;

        // Si la partícula no se mueve, no habrá colisión
        if (Math.abs(dx) < EPSILON && Math.abs(dy) < EPSILON) {
            return Double.POSITIVE_INFINITY;
        }

        // Calcular tiempo hasta colisión con pared
        double x = p.x;
        double y = p.y;
        double r = p.radius;

        // Resolver ecuación cuadrática para encontrar el tiempo de colisión con la pared
        double a = dx * dx + dy * dy;
        double b = 2 * (x * dx + y * dy);
        double c = x * x + y * y - (L/2 - r) * (L/2 - r);

        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return Double.POSITIVE_INFINITY; // No hay solución real
        }

        double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);

        // Tomar el tiempo positivo menor
        if (t1 > EPSILON && t2 > EPSILON) {
            return Math.min(t1, t2);
        } else if (t1 > EPSILON) {
            return t1;
        } else if (t2 > EPSILON) {
            return t2;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    // Predice el tiempo hasta la colisión con otra partícula
    private double predictParticleCollision(Particle p1, Particle p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        double dvx = p1.vx - p2.vx;
        double dvy = p1.vy - p2.vy;

        double dvdr = dx * dvx + dy * dvy;

        // Si las partículas se están alejando, no colisionarán
        if (dvdr >= 0) {
            return Double.POSITIVE_INFINITY;
        }

        double dvdv = dvx * dvx + dvy * dvy;
        double drdr = dx * dx + dy * dy;
        double sigma = p1.radius + p2.radius;

        // Discriminante de la ecuación cuadrática
        double d = (dvdr * dvdr) - dvdv * (drdr - sigma * sigma);

        // Si el discriminante es negativo, las partículas no colisionarán
        if (d < 0) {
            return Double.POSITIVE_INFINITY;
        }

        // Calcular tiempo hasta colisión
        double t = -(dvdr + Math.sqrt(d)) / dvdv;

        if (t > EPSILON) {
            return t;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    // Actualiza el sistema después de un evento
    private void handleEvent(Event event) {
        int p1 = event.particle1;
        int p2 = event.particle2;

        // Actualizar posiciones de todas las partículas
        double dt = event.time - currentTime;
        for (Particle p : particles) {
            p.updatePosition(currentTime, dt);
        }
        currentTime = event.time;

        // Manejar colisión
        if (p2 == -1) {
            // Colisión con pared
            handleWallCollision(particles[p1]);
        } else {
            // Colisión entre partículas
            handleParticleCollision(particles[p1], particles[p2]);

            // Registrar colisión si involucra a la partícula especial
            if (particles[p1].isSpecial || particles[p2].isSpecial) {
                Particle special = particles[p1].isSpecial ? particles[p1] : particles[p2];
                specialCollisionsWriter.printf("c%d %s %.6f%n",
                        ++specialCollisionCount, special, currentTime);
            }
        }

        // Limpiar la cola de eventos y predecir nuevos eventos
        eventQueue.clear();

        int[] collisionCounts = new int[particles.length];
        for (int i = 0; i < particles.length; i++) {
            predictEvents(i, collisionCounts);
        }
    }

    // Maneja la colisión con la pared
    private void handleWallCollision(Particle p) {
        // Calcular vector normal a la pared en el punto de colisión
        double nx = p.x / (p.distanceToCenter() + EPSILON);
        double ny = p.y / (p.distanceToCenter() + EPSILON);

        // Calcular componente de la velocidad en dirección normal
        double vn = p.vx * nx + p.vy * ny;

        // Actualizar velocidad (rebote elástico)
        p.vx -= 2 * vn * nx;
        p.vy -= 2 * vn * ny;
    }

    // Maneja la colisión entre dos partículas
    private void handleParticleCollision(Particle p1, Particle p2) {
        // Vector entre centros de partículas
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Vector normal
        double nx = dx / distance;
        double ny = dy / distance;

        // Componentes de velocidad relativa a lo largo de la normal
        double dvx = p1.vx - p2.vx;
        double dvy = p1.vy - p2.vy;
        double dvn = dvx * nx + dvy * ny;

        // Impulso y coeficientes para la conservación de momento
        double impulse = 2 * p1.mass * p2.mass * dvn / (p1.mass + p2.mass);
        double p1Coef = impulse / p1.mass;
        double p2Coef = impulse / p2.mass;

        // Actualizar velocidades
        p1.vx -= p1Coef * nx;
        p1.vy -= p1Coef * ny;
        p2.vx += p2Coef * nx;
        p2.vy += p2Coef * ny;
    }

    // Guarda el estado actual del sistema
    private void saveSystemState() {
        outputWriter.printf("e%d %.6f%n", ++eventCount, currentTime);
        for (int i = 0; i < particles.length; i++) {
            outputWriter.printf("p%d %s%n", i, particles[i]);
        }
    }

    // Ejecuta la simulación
    public void simulate(String outputFile, String specialCollisionsFile) {
        try {
            outputWriter = new PrintWriter(new FileWriter(outputFile));
            specialCollisionsWriter = new PrintWriter(new FileWriter(specialCollisionsFile));

            // Guardar estado inicial
            saveSystemState();

            // Procesar eventos
            int processedEvents = 0;
            while (processedEvents < N_eventos && !eventQueue.isEmpty()) {
                Event event = eventQueue.poll();
                handleEvent(event);
                processedEvents++;

                // Guardar estado con la frecuencia especificada
                if (processedEvents % savingFrequency == 0) {
                    saveSystemState();
                }
            }

            // Guardar estado final
            saveSystemState();

            // Cerrar archivos
            outputWriter.close();
            specialCollisionsWriter.close();

            System.out.println("Simulación completada. Eventos procesados: " + processedEvents);
            System.out.println("Colisiones de la partícula especial: " + specialCollisionCount);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Parámetros configurables
        int N = 200;              // Número de partículas (puede cambiarse)
        double v0 = 1.0;          // Velocidad inicial
        int N_eventos = 10000;    // Número de eventos a simular
        int savingFrequency = 1; // Guardar cada cuántos eventos

        // Crear y ejecutar la simulación
        EventDrivenMolecularDynamics simulation = new EventDrivenMolecularDynamics(N, v0, N_eventos, savingFrequency);
        simulation.initialize();
        simulation.simulate("simulation_output.txt", "special_collisions.txt");
    }
}
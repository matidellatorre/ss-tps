from matplotlib.animation import FuncAnimation
import matplotlib.pyplot as plt
import matplotlib.patches as patches

class SimulationStream:
    def __init__(self, file_path):
        self.file = open(file_path, "r")
        self.current_time = None
        self.particle_states = []
        self.buffer = []
        self.buffer_size = 100
        self.is_reading = True

    def read_next_snapshot(self):
        if not self.is_reading:
            return None

        try:
            while True:
                line = self.file.readline()
                if not line:
                    self.is_reading = False
                    return None

                if line.startswith("e"):
                    if self.current_time is not None:
                        snapshot = (self.current_time, self.particle_states.copy())
                        self.current_time = float(line.split()[1])
                        self.particle_states = []
                        return snapshot
                    self.current_time = float(line.split()[1])
                elif line.startswith("p"):
                    parts = line.split()
                    pid = int(parts[0][1:])
                    x, y = float(parts[1]), float(parts[2])
                    vx, vy = float(parts[3]), float(parts[4])
                    self.particle_states.append((pid, x, y, vx, vy))
        except Exception as e:
            print(f"Error: {e}")
            self.is_reading = False
            return None

    def get_next_snapshot(self):
        if self.buffer:
            return self.buffer.pop(0)
        return self.read_next_snapshot()

    def preload_buffer(self):
        while len(self.buffer) < self.buffer_size and self.is_reading:
            snapshot = self.read_next_snapshot()
            if snapshot:
                self.buffer.append(snapshot)
            else:
                break

    def close(self):
        self.file.close()

def main():
    # Configuración
    container_radius = 0.05  # Radio del contenedor (L/2)
    obstacle_radius = 0.005  # Radio del obstáculo
    particle_radius = 0.0005  # Radio de las partículas
    file_path = "/home/gasti/Documents/ITBA/Materias/CuatriActual/SS/ss-tps/tp3/simulation.txt"

    stream = SimulationStream(file_path)
    stream.preload_buffer()

    fig, ax = plt.subplots()

    # Dibujar obstáculo central
    obstacle = patches.Circle((0, 0), obstacle_radius, color='red', alpha=0.8)
    ax.add_patch(obstacle)

    # Calcular tamaño de partículas en puntos
    points_per_meter = 1/(0.0254/72)
    particle_size = (2 * particle_radius * points_per_meter) ** 2

    # Scatter plot para partículas
    particles = ax.scatter([], [], s=particle_size, color='blue')

    # Configurar gráfico
    ax.set_xlim(-container_radius*1.2, container_radius*1.2)
    ax.set_ylim(-container_radius*1.2, container_radius*1.2)
    ax.set_aspect('equal')
    ax.axis('off')

    # Contenedor circular
    container = patches.Circle((0, 0), container_radius, fill=False, color='black')
    ax.add_patch(container)

    def update(frame):
        snapshot = stream.get_next_snapshot()
        if snapshot is None:
            return particles,

        time, particle_states = snapshot
        positions = [(p[1], p[2]) for p in particle_states if p[0] != 0]
        particles.set_offsets(positions if positions else [])
        return particles,

    anim = FuncAnimation(
        fig, update,
        frames=None,
        interval=10,
        blit=True,
        repeat=False
    )

    plt.show()
    stream.close()

if __name__ == "__main__":
    main()
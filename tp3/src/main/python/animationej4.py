import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
from matplotlib.patches import Circle
import argparse

class SimpleMolecularDynamicsAnimation:
    def __init__(self, filename, fps=30, save_animation=False, output_file=None):
        # Constantes físicas
        self.L = 0.1  # Diámetro del recinto en metros
        self.R = 0.005  # Radio de la partícula especial en metros
        self.r = 5e-4  # Radio de las partículas normales en metros

        # Parámetros de la animación
        self.fps = fps
        self.save_animation = save_animation
        self.output_file = output_file

        # Cargar datos de la simulación
        self.states = self.load_simulation_data(filename)

        if not self.states:
            print("No se pudieron cargar datos de la simulación.")
            return

        # Configurar la figura y los ejes
        self.fig, self.ax = plt.subplots(figsize=(8, 8))
        self.ax.set_xlim(-self.L/2, self.L/2)
        self.ax.set_ylim(-self.L/2, self.L/2)
        self.ax.set_aspect('equal')

        # Eliminar ejes y marcas
        self.ax.set_axis_off()

        # Dibujar el recinto circular
        circle = plt.Circle((0, 0), self.L/2, fill=False, color='black', linestyle='-', linewidth=1)
        self.ax.add_patch(circle)

        # Crear colecciones vacías para las partículas
        self.particles = []

    def load_simulation_data(self, filename):
        """Carga datos de simulación desde un archivo de texto"""
        states = []
        current_state = None

        try:
            with open(filename, 'r') as f:
                for line in f:
                    parts = line.strip().split()
                    if not parts:
                        continue

                    if parts[0].startswith('e'):
                        # Nuevo evento
                        if current_state is not None:
                            states.append(current_state)

                        current_state = {'particles': {}}

                    elif parts[0].startswith('p'):
                        # Datos de una partícula
                        particle_id = int(parts[0][1:])
                        x = float(parts[1])
                        y = float(parts[2])

                        if current_state is not None:
                            current_state['particles'][particle_id] = {
                                'x': x,
                                'y': y,
                                'special': (particle_id == 0)  # Partícula especial es la 0
                            }

            # Añadir el último estado si existe
            if current_state is not None:
                states.append(current_state)

        except Exception as e:
            print(f"Error al cargar el archivo: {e}")
            return []

        return states

    def init_animation(self):
        """Inicializa la animación"""
        # Eliminar partículas existentes
        for particle in self.particles:
            particle.remove()
        self.particles = []

        # Crear partículas iniciales
        state = self.states[0]
        for p_id, p_data in state['particles'].items():
            color = 'red' if p_data['special'] else 'blue'
            radius = self.R if p_data['special'] else self.r
            circle = Circle((p_data['x'], p_data['y']), radius, color=color, alpha=0.8)
            self.ax.add_patch(circle)
            self.particles.append(circle)

        return self.particles

    def update_animation(self, frame):
        """Actualiza la animación para el frame dado"""
        state = self.states[frame]

        # Actualizar posiciones de las partículas
        for i, (p_id, p_data) in enumerate(state['particles'].items()):
            if i < len(self.particles):
                self.particles[i].center = (p_data['x'], p_data['y'])

        return self.particles

    def create_animation(self):
        """Crea y muestra la animación"""
        anim = FuncAnimation(
            self.fig,
            self.update_animation,
            frames=len(self.states),
            init_func=self.init_animation,
            blit=True,
            interval=1000/self.fps,
            repeat=True
        )

        # Guardar la animación si se solicita
        if self.save_animation and self.output_file:
            print(f"Guardando animación en {self.output_file}...")
            if self.output_file.endswith('.gif'):
                anim.save(self.output_file, writer='pillow', fps=self.fps)
            else:
                anim.save(self.output_file, writer='ffmpeg', fps=self.fps)
            print("Animación guardada correctamente.")

        plt.tight_layout()
        plt.show()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Animación simple de dinámica molecular')
    parser.add_argument('input_file', type=str, help='Archivo de entrada con datos de la simulación')
    parser.add_argument('--fps', type=int, default=30, help='Frames por segundo (default: 30)')
    parser.add_argument('--save', action='store_true', help='Guardar la animación como archivo')
    parser.add_argument('--output', type=str, default='animation.mp4',
                        help='Nombre del archivo de salida (default: animation.mp4)')

    args = parser.parse_args()

    animation = SimpleMolecularDynamicsAnimation(
        args.input_file,
        fps=args.fps,
        save_animation=args.save,
        output_file=args.output
    )
    animation.create_animation()
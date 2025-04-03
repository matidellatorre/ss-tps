import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import matplotlib.patches as mpatches
import argparse
import configparser
import re
import os

class GridVisualizer:
    def __init__(self, results_file, config_file, fps=5):
        self.results_file = results_file
        self.config_file = config_file
        self.fps = fps
        self.frames = []
        self.grid_size = 0
        self.mcs_numbers = []  # Para almacenar los números de pasos MCS
        self.load_config()
        self.load_data()

    def load_config(self):
        """Carga la configuración desde el archivo de configuración"""
        # Si el archivo de configuración es de tipo .properties
        if self.config_file.endswith('.properties'):
            with open(self.config_file, 'r') as f:
                for line in f:
                    if line.startswith('N='):
                        self.grid_size = int(line.strip().split('=')[1])
                        break
        else:
            # Asumimos que es un archivo de configuración tipo INI
            config = configparser.ConfigParser()
            config.read(self.config_file)
            self.grid_size = int(config.get('DEFAULT', 'N', fallback=10))

        print(f"Tamaño de la grilla cargado: {self.grid_size}x{self.grid_size}")

    def load_data(self):
        """Carga los datos del archivo de resultados"""
        print(f"Cargando datos desde {self.results_file}...")
        with open(self.results_file, 'r') as f:
            content = f.read()

        # Encontrar los números de paso MCS usando expresiones regulares
        mcs_pattern = re.compile(r'MCS=(\d+)')
        mcs_matches = mcs_pattern.findall(content)
        self.mcs_numbers = [int(mcs) for mcs in mcs_matches]

        # Dividir el contenido por los bloques separados por MCS=X
        blocks = re.split(r'MCS=\d+\n', content)
        if not blocks[0].strip():  # Si el primer bloque está vacío, lo eliminamos
            blocks = blocks[1:]

        # Procesar cada bloque
        for block in blocks:
            if not block.strip():
                continue

            # Convertir el bloque en una matriz numpy
            lines = block.strip().split('\n')
            grid = np.zeros((self.grid_size, self.grid_size))

            for i, line in enumerate(lines):
                if i >= self.grid_size:
                    break
                values = line.split()
                for j, val in enumerate(values):
                    if j >= self.grid_size:
                        break
                    grid[i, j] = int(val)

            self.frames.append(grid)

        print(f"Se cargaron {len(self.frames)} pasos de Monte Carlo.")

    def create_animation(self, output_file=None):
        """Crea una animación con los frames cargados"""
        fig, ax = plt.subplots(figsize=(10, 10))

        # Configurar el mapa de colores: -1 -> blanco, 1 -> negro
        cmap = plt.cm.get_cmap('binary')
        vmin, vmax = -1, 1

        # Inicializar el primer frame
        im = ax.imshow(self.frames[0], cmap=cmap, vmin=vmin, vmax=vmax, animated=True)

        # Crear leyenda con parches de color en lugar de una barra de colores
        white_patch = mpatches.Patch(color='white', label='Estado -1')
        black_patch = mpatches.Patch(color='black', label='Estado 1')
        ax.legend(handles=[white_patch, black_patch], loc='upper right')

        # Función para actualizar los frames
        def update_frame(i):
            im.set_array(self.frames[i])
            title.set_text(f'Paso de Monte Carlo: {self.mcs_numbers[i]}')
            return [im, title]

        # Crear la animación
        ani = animation.FuncAnimation(
            fig, update_frame, frames=len(self.frames),
            interval=1000/self.fps, blit=False
        )

        # Guardar la animación si se especifica un archivo de salida
        if output_file:
            print(f"Guardando animación en {output_file}...")
            if output_file.endswith('.mp4'):
                ani.save(output_file, writer='ffmpeg', fps=self.fps)
            elif output_file.endswith('.gif'):
                ani.save(output_file, writer='pillow', fps=self.fps)
            else:
                print("Formato de salida no reconocido. Usando .mp4")
                ani.save(f"{output_file}.mp4", writer='ffmpeg', fps=self.fps)
            print("Animación guardada exitosamente.")

        plt.tight_layout()
        plt.show()

    def create_snapshot(self, mcs_step, output_file=None):
        """Crea una imagen estática para un paso específico de Monte Carlo"""
        if mcs_step >= len(self.frames):
            print(f"Error: Solo hay {len(self.frames)} pasos disponibles.")
            return

        fig, ax = plt.subplots(figsize=(10, 10))

        # Usar el mapa de colores 'binary' para blanco y negro
        im = ax.imshow(self.frames[mcs_step], cmap=plt.cm.binary, vmin=-1, vmax=1)

        # Crear leyenda con parches de color
        white_patch = mpatches.Patch(color='white', label='Estado -1')
        black_patch = mpatches.Patch(color='black', label='Estado 1')
        ax.legend(handles=[white_patch, black_patch], loc='upper right')

        if output_file:
            plt.savefig(output_file, dpi=300)
            print(f"Imagen guardada en {output_file}")

        plt.tight_layout()
        plt.show()

    def calculate_magnetization(self):
        """Calcula la magnetización promedio para cada paso de Monte Carlo"""
        magnetizations = [np.abs(np.mean(frame)) for frame in self.frames]

        plt.figure(figsize=(10, 6))
        plt.plot(self.mcs_numbers, magnetizations, '-o')
        plt.xlabel('MCS')
        plt.ylabel('$\\langle M \\rangle$')
        plt.grid(True)
        plt.tight_layout()
        plt.show()

        return magnetizations


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Visualizador de simulación de Metropolis-Monte Carlo')
    parser.add_argument('results_file', help='Archivo de resultados generado por la simulación')
    parser.add_argument('config_file', help='Archivo de configuración con los parámetros N y p')
    parser.add_argument('--fps', type=int, default=5, help='Frames por segundo para la animación (default: 5)')
    parser.add_argument('--output', '-o', help='Archivo de salida para la animación (opcional)')
    parser.add_argument('--step', '-s', type=int, help='Mostrar un paso específico (opcional)')
    parser.add_argument('--magnetization', '-m', action='store_true', help='Mostrar gráfico de magnetización')

    args = parser.parse_args()

    visualizer = GridVisualizer(args.results_file, args.config_file, args.fps)

    if args.step is not None:
        # Convertir índice a número de paso
        if args.step < len(visualizer.mcs_numbers):
            step_index = args.step
        else:
            print(f"El paso {args.step} está fuera de rango. Usando el último paso disponible.")
            step_index = len(visualizer.mcs_numbers) - 1

        visualizer.create_snapshot(step_index, args.output)
    elif args.magnetization:
        visualizer.calculate_magnetization()
    else:
        visualizer.create_animation(args.output)
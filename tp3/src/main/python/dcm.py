import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit

# Función para leer los datos
def read_collision_data(file_path):
    data = []
    with open(file_path, 'r') as file:
        for line in file:
            if line.strip() and not line.startswith('#'):
                parts = line.split()
                if len(parts) >= 6:
                    # collision_num, x, y, vx, vy, time
                    data.append([float(parts[1]), float(parts[2]),
                                float(parts[3]), float(parts[4]), float(parts[5])])
    return np.array(data)

# Función para calcular el desplazamiento cuadrático medio
def calculate_msd(positions, times):
    """
    Calcula el desplazamiento cuadrático medio.

    Args:
        positions: Array de posiciones (x, y)
        times: Array de tiempos correspondientes

    Returns:
        time_bins: Puntos temporales para el MSD
        msd: Valores del MSD
    """
    # Definimos bins de tiempo logarítmicos para tener una distribución más uniforme
    # en la escala logarítmica (más puntos en tiempos cortos)
    min_time = min(times[times > 0])
    max_time = max(times)
    num_bins = 30
    time_bins = np.logspace(np.log10(min_time), np.log10(max_time), num_bins)

    # Inicializamos arrays para almacenar los MSD y contadores
    msd_values = np.zeros(len(time_bins))
    counts = np.zeros(len(time_bins))

    # Posición inicial
    x0, y0 = positions[0]

    # Para cada tiempo, calculamos el desplazamiento cuadrático
    for i, t in enumerate(times):
        x, y = positions[i]
        r_squared = (x - x0)**2 + (y - y0)**2

        # Asignar al bin de tiempo más cercano
        bin_idx = np.abs(time_bins - t).argmin()
        msd_values[bin_idx] += r_squared
        counts[bin_idx] += 1

    # Promediamos los valores por bin
    valid_bins = counts > 0
    msd_values[valid_bins] /= counts[valid_bins]

    return time_bins[valid_bins], msd_values[valid_bins]

# Función para ajuste lineal: MSD = 4Dt en 2D
def linear_fit(t, D):
    return 4 * D * t

# Función principal
def analyze_diffusion():
    data = read_collision_data('/home/gasti/Documents/ITBA/Materias/CuatriActual/SS/ss-tps/tp3/special_collisions.txt')

    positions = data[:, :2]  # x, y
    times = data[:, 4]       # tiempo

    # Calculamos el MSD
    time_bins, msd = calculate_msd(positions, times)

    # Ajuste lineal para obtener D
    params, covariance = curve_fit(linear_fit, time_bins, msd)
    D = params[0]
    D_error = np.sqrt(covariance[0, 0])

    # Creamos la figura y los ejes para los gráficos
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))

    # Gráfico 1: Trayectoria de la partícula
    ax1.plot(positions[:, 0], positions[:, 1], 'r-', alpha=0.6)
    ax1.plot(positions[:, 0], positions[:, 1], 'bo', markersize=1, alpha=0.5)
    ax1.set_xlabel('Posición x (m)')
    ax1.set_ylabel('Posición y (m)')
    ax1.set_title('Trayectoria de la partícula')
    ax1.grid(True, alpha=0.3)

    # Gráfico 2: MSD vs tiempo con ajuste lineal
    ax2.scatter(time_bins, msd, color='blue', s=30, alpha=0.7, label='DCM calculado')

    # Línea de ajuste
    t_fit = np.linspace(min(time_bins), max(time_bins), 100)
    msd_fit = linear_fit(t_fit, D)
    ax2.plot(t_fit, msd_fit, 'r-', lw=2,
             label=f'Ajuste: DCM = 4Dt\nD = {D:.6e} ± {D_error:.1e} m²/s')

    ax2.set_xlabel('Tiempo (s)')
    ax2.set_ylabel('DCM (m²)')
    ax2.set_title('Desplazamiento Cuadrático Medio vs Tiempo')
    ax2.grid(True, alpha=0.3)
    ax2.legend()

    # Escala logarítmica para mejor visualización
    ax2.set_xscale('log')
    ax2.set_yscale('log')

    plt.tight_layout()
    plt.savefig('coeficiente_difusion.png', dpi=300)
    plt.show()

    return D, D_error

# Ejecutar el análisis
D, D_error = analyze_diffusion()
print(f"Coeficiente de difusión (D): {D:.6e} ± {D_error:.1e} m²/s")

# Explicación detallada de la elección de tiempos
print("\nExplicación de la elección de tiempos para el cálculo del DCM:")
print("1. Dado que los eventos de colisión ocurren en tiempos no uniformes, utilizamos")
print("   bins temporales logarítmicos para distribuir mejor los puntos de análisis.")
print("2. Para cada tiempo de colisión, calculamos el desplazamiento cuadrático desde")
print("   la posición inicial.")
print("3. Asignamos cada desplazamiento al bin temporal más cercano y promediamos los")
print("   valores en cada bin.")
print("4. Este enfoque nos permite obtener una estadística más robusta incluso con")
print("   distribuciones temporales heterogéneas.")
print("5. El ajuste lineal DCM = 4Dt nos permite obtener el coeficiente de difusión D")
print("   siguiendo la relación teórica para un sistema bidimensional.")
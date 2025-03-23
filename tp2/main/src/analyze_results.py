import os
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.ticker import ScalarFormatter

def read_magnetization_file(filename):
    """Lee un archivo de magnetización y devuelve los datos."""
    data = {}
    with open(filename, 'r') as f:
        # Saltar la línea de encabezado
        header = f.readline()

        # Leer los datos
        mcs = []
        mag = []
        mag_squared = []
        stationary = []

        for line in f:
            if line.strip() and not line.startswith('#'):
                parts = line.strip().split('\t')
                if len(parts) >= 4:
                    mcs.append(int(parts[0]))
                    mag.append(float(parts[1]))
                    mag_squared.append(float(parts[2]))
                    stationary.append(int(parts[3]))

        data['mcs'] = np.array(mcs)
        data['mag'] = np.array(mag)
        data['mag_squared'] = np.array(mag_squared)
        data['stationary'] = np.array(stationary)

    return data

def find_summary_files():
    """Encuentra los archivos de resumen de magnetización en el directorio de resultados."""
    summary_files = []
    for filename in os.listdir('resultados'):
        if filename.startswith('magnetizacion_summary_p'):
            summary_files.append(os.path.join('resultados', filename))
    return summary_files

def read_all_summaries():
    """Lee todos los archivos de resumen y compila los resultados."""
    p_values = []
    avg_mag = []
    avg_mag_squared = []
    susceptibility = []

    # Primero, buscar archivos de resumen en el directorio principal
    for filename in os.listdir():
        if filename == 'magnetizacion_summary.txt':
            with open(filename, 'r') as f:
                # Saltar la línea de encabezado
                header = f.readline()

                # Leer los datos
                line = f.readline().strip()
                if line:
                    parts = line.split('\t')
                    if len(parts) >= 4:
                        p_values.append(float(parts[0]))
                        avg_mag.append(float(parts[1]))
                        avg_mag_squared.append(float(parts[2]))
                        susceptibility.append(float(parts[3]))

    # Buscar archivos de magnetización individual en la carpeta de resultados
    for filename in os.listdir('resultados'):
        if filename.startswith('magnetizacion_p') and filename.endswith('.txt'):
            # Extraer el valor de p del nombre del archivo
            p_str = filename.replace('magnetizacion_p', '').replace('.txt', '')
            p = float(p_str)

            # Leer los datos
            file_path = os.path.join('resultados', filename)
            data = read_magnetization_file(file_path)

            # Filtrar solo los puntos en estado estacionario
            stationary_mask = data['stationary'] == 1

            # Si no hay puntos marcados como estacionarios, usar la segunda mitad de los datos
            if not np.any(stationary_mask):
                stationary_mask = data['mcs'] >= len(data['mcs']) // 2

            stationary_mag = np.abs(data['mag'][stationary_mask])  # Valor absoluto
            stationary_mag_squared = data['mag_squared'][stationary_mask]

            # Calcular promedios
            if len(stationary_mag) > 0:
                m_avg = np.mean(stationary_mag)
                m2_avg = np.mean(stationary_mag_squared)
                susc = 50 * 50 * (m2_avg - m_avg * m_avg)  # N^2 * (<M^2> - <M>^2)

                p_values.append(p)
                avg_mag.append(m_avg)
                avg_mag_squared.append(m2_avg)
                susceptibility.append(susc)

    # Ordenar los resultados por p
    sorted_indices = np.argsort(p_values)
    p_values = np.array(p_values)[sorted_indices]
    avg_mag = np.array(avg_mag)[sorted_indices]
    avg_mag_squared = np.array(avg_mag_squared)[sorted_indices]
    susceptibility = np.array(susceptibility)[sorted_indices]

    return p_values, avg_mag, avg_mag_squared, susceptibility

def plot_magnetization_evolution():
    """Grafica la evolución de la magnetización para todos los valores de p."""
    plt.figure(figsize=(10, 6))

    for filename in os.listdir('resultados'):
        if filename.startswith('magnetizacion_p') and filename.endswith('.txt'):
            # Extraer el valor de p del nombre del archivo
            p_str = filename.replace('magnetizacion_p', '').replace('.txt', '')
            p = float(p_str)

            # Leer los datos
            file_path = os.path.join('resultados', filename)
            data = read_magnetization_file(file_path)

            # Graficar la magnetización
            plt.plot(data['mcs'], np.abs(data['mag']), label=f'p = {p}')

    plt.xlabel('Pasos de Monte Carlo (MCS)')
    plt.ylabel('|Magnetización|')
    plt.title('Evolución de la magnetización para diferentes valores de p')
    plt.legend()
    plt.grid(True)
    plt.savefig('evolucion_magnetizacion.png', dpi=300)
    plt.close()

def plot_results(p_values, avg_mag, susceptibility):
    """Grafica la magnetización y susceptibilidad en función de p."""
    # Configurar el estilo de la gráfica
    plt.style.use('seaborn-v0_8-whitegrid')

    # Crear una figura con dos subgráficas
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(10, 12), sharex=True)

    # Gráfica de magnetización
    ax1.plot(p_values, avg_mag, 'o-', color='blue', linewidth=2, markersize=8)
    ax1.set_ylabel('$\\langle |M| \\rangle$', fontsize=14)
    ax1.set_title('Magnetización promedio en estado estacionario', fontsize=16)
    ax1.grid(True)

    # Gráfica de susceptibilidad
    ax2.plot(p_values, susceptibility, 'o-', color='red', linewidth=2, markersize=8)
    ax2.set_xlabel('Probabilidad p', fontsize=14)
    ax2.set_ylabel('Susceptibilidad $\\chi = N(\\langle M^2 \\rangle - \\langle M \\rangle^2)$', fontsize=14)
    ax2.set_title('Susceptibilidad en estado estacionario', fontsize=16)
    ax2.grid(True)

    # Escala logarítmica para la susceptibilidad
    ax2.set_yscale('log')
    ax2.yaxis.set_major_formatter(ScalarFormatter())

    # Ajustar la presentación
    plt.tight_layout()
    plt.savefig('resultados_observables.png', dpi=300)
    plt.close()

def main():
    """Función principal para ejecutar el análisis."""
    # Crear directorio para las gráficas si no existe
    if not os.path.exists('graficas'):
        os.mkdir('graficas')

    # Graficar la evolución de la magnetización
    plot_magnetization_evolution()

    # Leer los datos de los resúmenes
    p_values, avg_mag, avg_mag_squared, susceptibility = read_all_summaries()

    # Guardar los resultados en un archivo CSV
    with open('resultados_finales.txt', 'w') as f:
        f.write('p,avg_mag,avg_mag_squared,susceptibility\n')
        for i in range(len(p_values)):
            f.write(f'{p_values[i]},{avg_mag[i]},{avg_mag_squared[i]},{susceptibility[i]}\n')

    # Graficar los resultados
    plot_results(p_values, avg_mag, susceptibility)

    print("Análisis completado. Resultados guardados en 'resultados_finales.txt'")
    print("Gráficas generadas:")
    print("  - evolucion_magnetizacion.png")
    print("  - resultados_observables.png")

if __name__ == "__main__":
    main()
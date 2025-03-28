import os
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker

def read_magnetization_file(filename):
    """Lee un archivo de magnetización y devuelve los datos."""
    data = {}
    with open(filename, 'r') as f:
        # Leer línea de encabezado
        header = f.readline().strip()

        # Leer paso estacionario
        stationary_step = int(f.readline().strip())

        # Leer los datos
        mcs = []
        mag = []
        mag_squared = []
        stationary = []

        for line in f:
            line = line.strip()
            if line and not line.startswith('#'):
                parts = line.split()
                if len(parts) >= 2:
                    mcs_value = int(parts[0])
                    mag_value = float(parts[1])
                    mag_squared_value = float(parts[2]) if len(parts) > 2 else 0

                    mcs.append(mcs_value)
                    mag.append(mag_value)
                    mag_squared.append(mag_squared_value)
                    stationary.append(1 if mcs_value > stationary_step else 0)

        data['mcs'] = np.array(mcs)
        data['mag'] = np.array(mag)
        data['mag_squared'] = np.array(mag_squared)
        data['stationary_step'] = stationary_step

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
            stationary_mask = data['mcs'] > data['stationary_step']



            stationary_mag = np.abs(data['mag'][stationary_mask])  # Valor absoluto
            stationary_mag_squared = data['mag_squared'][stationary_mask]

            # Calcular promedios
            if len(stationary_mag) > 0:
                m_avg = np.mean(stationary_mag)
                m2_avg = np.mean(stationary_mag_squared)
                susc = 50 * 50 * (m2_avg - m_avg * m_avg)  # N^2 * (<M(t)^2> - <M(t)>^2)

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

#Funcion de ploteo de la evolución de la magnetización vieja que incluye todos los valores de p
# def plot_magnetization_evolution():
#     """Grafica la evolución de la magnetización para todos los valores de p."""
#     plt.figure(figsize=(10, 6))
#
#     for filename in os.listdir('resultados'):
#         if filename.startswith('magnetizacion_p') and filename.endswith('.txt'):
#             # Extraer el valor de p del nombre del archivo
#             p_str = filename.replace('magnetizacion_p', '').replace('.txt', '')
#             p = float(p_str)
#
#             # Leer los datos
#             file_path = os.path.join('resultados', filename)
#             data = read_magnetization_file(file_path)
#
#             # Graficar la magnetización
#             plt.plot(data['mcs'], np.abs(data['mag']), label=f'p = {p}')
#
#     plt.xlabel('Pasos de Monte Carlo (MCS)')
#     plt.ylabel('Magnetización')
#     plt.title('Evolución de la magnetización para diferentes valores de p')
#     plt.legend()
#     plt.grid(True)
#     plt.savefig('./graficas/evolucion_magnetizacion.png', dpi=300)
#     plt.close()

def plot_magnetization_evolution():
    """Grafica la evolución de la magnetización para 3 valores representativos de p."""
    plt.figure(figsize=(10, 6))

    # Obtener todos los valores de p
    p_values = []
    file_paths = {}

    for filename in os.listdir('resultados'):
        if filename.startswith('magnetizacion_p') and filename.endswith('.txt'):
            # Extraer el valor de p del nombre del archivo
            p_str = filename.replace('magnetizacion_p', '').replace('.txt', '')
            p = float(p_str)
            p_values.append(p)
            file_paths[p] = os.path.join('resultados', filename)

    # Ordenar los valores de p
    p_values.sort()

    # Seleccionar el mínimo, el medio y el máximo
    if len(p_values) >= 3:
        selected_p = [p_values[0], p_values[len(p_values) // 2], p_values[-1]]
    else:
        selected_p = p_values  # Si hay menos de 3, usa todos los disponibles

    # Graficar solo los p seleccionados
    for p in selected_p:
        data = read_magnetization_file(file_paths[p])
        plt.plot(data['mcs'], np.abs(data['mag']), label=f'p = {p:.3f}')

    # Configuración de la gráfica
    plt.xlabel('Pasos de Monte Carlo (MCS)')
    plt.ylabel('Magnetización')
    plt.title('Evolución de la magnetización para 3 valores de p')
    plt.legend()
    plt.grid(True)

    # Guardar la figura
    plt.savefig('./graficas/evolucion_magnetizacion.png', dpi=300)
    plt.close()

#Funcion de ploteo de resultados con 2 gráficos separados, uno encima del otro
# def plot_results(p_values, avg_mag, susceptibility):
#     """Grafica la magnetización y susceptibilidad en función de p."""
#     # Configurar el estilo de la gráfica
#     plt.style.use('seaborn-whitegrid')
#
#     # Crear una figura con dos subgráficas
#     fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(10, 12), sharex=True)
#
#     # Gráfica de magnetización
#     ax1.plot(p_values, avg_mag, 'o-', color='blue', linewidth=2, markersize=8)
#     ax1.set_ylabel('$\\langle M(t) \\rangle$', fontsize=14)
#     ax1.set_title('Magnetización promedio en estado estacionario', fontsize=16)
#     ax1.grid(True)
#
#     # Gráfica de susceptibilidad
#     ax2.plot(p_values, susceptibility, 'o-', color='red', linewidth=2, markersize=8)
#     ax2.set_xlabel('Probabilidad p', fontsize=14)
#     ax2.set_ylabel('Susceptibilidad $\\chi = N(\\langle M(t)^2 \\rangle - \\langle M(t) \\rangle^2)$', fontsize=14)
#     ax2.set_title('Susceptibilidad en estado estacionario', fontsize=16)
#     ax2.grid(True)
#
#     # Escala logarítmica para la susceptibilidad
#     ax2.set_yscale('log')
#     ax2.yaxis.set_major_formatter(ScalarFormatter())
#
#     # Ajustar la presentación
#     plt.tight_layout()
#     plt.savefig('./graficas/resultados_observables.png', dpi=300)
#     plt.close()

def plot_results(p_values, avg_mag, susceptibility):
    """Grafica la magnetización y susceptibilidad en función de p en una sola gráfica con doble eje Y."""
    plt.style.use('seaborn-whitegrid')

    fig, ax1 = plt.subplots(figsize=(10, 6))

    # Primer eje Y (magnetización)
    ax1.plot(p_values, avg_mag, 'o-', color='blue', linewidth=2, markersize=8, label='$\\langle M(t) \\rangle$')
    ax1.set_ylabel('$\\langle M(t) \\rangle$', fontsize=14, color='blue')
    ax1.tick_params(axis='y', labelcolor='blue')
    ax1.set_title('Magnetización y Susceptibilidad en Estado Estacionario', fontsize=16)
    ax1.grid(True)

    # Segundo eje Y (susceptibilidad)
    ax2 = ax1.twinx()
    ax2.plot(p_values, susceptibility, 'o-', color='red', linewidth=2, markersize=8, label='Susceptibilidad')
    ax2.set_ylabel('Susceptibilidad $\\chi$', fontsize=14, color='red')
    ax2.tick_params(axis='y', labelcolor='red')
    ax2.set_yscale('log')
    ax2.yaxis.set_major_formatter(ticker.ScalarFormatter())

    # Etiqueta del eje X
    ax1.set_xlabel('Probabilidad p', fontsize=14)

    # Guardar la figura
    plt.savefig('./graficas/resultados_observables.png', dpi=300)
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
    with open('./resultados_finales/resultados_finales.txt', 'w') as f:
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
import pandas as pd
import matplotlib.pyplot as plt
import glob
import os

# Directorio donde están los archivos de resultados
output_dir = '../output' # Asumiendo que este script está en python_graficadores

# Encontrar el archivo de resultados para un dt específico (ej., el más pequeño o uno intermedio)
# Puedes modificar esto para seleccionar el dt que quieras graficar
dt_a_graficar = "1E-04" # Ejemplo, asegúrate que el formato coincida con el nombre del archivo
# Los nombres de archivo pueden ser "resultados_dt_1E-02.txt", "resultados_dt_5E-03.txt", etc.
# Es importante que el dt_a_graficar coincida con cómo se nombran en Java (ej. "1E-04" o "0.0001")
# Java puede generar "1E-04", Python espera esto o "1.000000E-04" a veces. Ajustar si es necesario.

# Intenta encontrar un archivo que coincida con el dt_a_graficar
target_file_pattern = os.path.join(output_dir, f"resultados_dt_*{dt_a_graficar.replace('E', 'e')}*.txt")
archivos_dt = glob.glob(target_file_pattern)

if not archivos_dt:
    # Si no se encuentra, intenta con un formato más genérico o toma el primero que encuentre
    print(f"No se encontró archivo para dt={dt_a_graficar}. Intentando con el primer archivo de resultados...")
    archivos_dt = sorted(glob.glob(os.path.join(output_dir, "resultados_dt_*.txt")))

if not archivos_dt:
    print("No se encontraron archivos de resultados en la carpeta 'output'.")
    exit()

archivo_a_graficar = archivos_dt[0] # Grafica el primero encontrado si el específico falla
print(f"Graficando datos de: {archivo_a_graficar}")

try:
    data = pd.read_csv(archivo_a_graficar)
except FileNotFoundError:
    print(f"Error: El archivo {archivo_a_graficar} no fue encontrado.")
    exit()
except pd.errors.EmptyDataError:
    print(f"Error: El archivo {archivo_a_graficar} está vacío.")
    exit()


plt.figure(figsize=(12, 7))
plt.plot(data['tiempo'], data['posicion_analitica'], label='Solución Analítica', color='black', linestyle='--')

if 'posicion_verlet' in data.columns:
    plt.plot(data['tiempo'], data['posicion_verlet'], label='Verlet Original', alpha=0.7)
if 'posicion_beeman' in data.columns:
    plt.plot(data['tiempo'], data['posicion_beeman'], label='Beeman', alpha=0.7)
if 'posicion_gearpc5' in data.columns: # Corregido para coincidir con el nombre en Java (toLowerCase)
    plt.plot(data['tiempo'], data['posicion_gearpc5'], label='Gear PC Orden 5', alpha=0.7)

# plt.title(f'Comparación de Métodos de Integración (dt = {os.path.basename(archivo_a_graficar).split("_")[2].replace(".txt","")})')
plt.xlabel('Tiempo (s)')
plt.ylabel('Posición (m)')
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.savefig(os.path.join(output_dir, f'grafico_posiciones_{os.path.basename(archivo_a_graficar).split("_")[2].replace(".txt","")}.png'))
plt.show()

print(f"Gráfico de posiciones guardado en {output_dir}")
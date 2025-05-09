import pandas as pd
import matplotlib.pyplot as plt
import os
import numpy as np

output_dir = '../output'
archivo_errores = os.path.join(output_dir, 'errores_ecm.txt')

try:
    data_errores = pd.read_csv(archivo_errores)
except FileNotFoundError:
    print(f"Error: El archivo {archivo_errores} no fue encontrado.")
    exit()
except pd.errors.EmptyDataError:
    print(f"Error: El archivo {archivo_errores} está vacío.")
    exit()

if data_errores.empty:
    print("El archivo de errores está vacío. No se puede graficar.")
    exit()

plt.figure(figsize=(10, 6))

# Es importante que los nombres de las columnas coincidan con los del archivo ecm_verlet, ecm_beeman, ecm_gear
if 'ecm_verlet' in data_errores.columns:
    plt.plot(data_errores['dt'], data_errores['ecm_verlet'], marker='o', linestyle='-', label='Verlet Original')
if 'ecm_beeman' in data_errores.columns:
    plt.plot(data_errores['dt'], data_errores['ecm_beeman'], marker='s', linestyle='-', label='Beeman')
if 'ecm_gear' in data_errores.columns:
    plt.plot(data_errores['dt'], data_errores['ecm_gear'], marker='^', linestyle='-', label='Gear PC Orden 5')

# Líneas de referencia para pendientes (opcional, pero útil para ver el orden)
# Orden 2: ECM ~ dt^2 -> log(ECM) ~ 2*log(dt)
# Orden 4: ECM ~ dt^4 -> log(ECM) ~ 4*log(dt)
# Orden 5: ECM ~ dt^5 -> log(ECM) ~ 5*log(dt)
dts_ref = np.array(data_errores['dt'])
if len(dts_ref) > 1:
    # Escalar para que se vean bien en la gráfica
    factor_o2 = data_errores['ecm_verlet'].iloc[0] / (dts_ref[0]**2) if 'ecm_verlet' in data_errores.columns and not data_errores['ecm_verlet'].empty else 1e-5/(dts_ref[0]**2 if dts_ref[0]!=0 else 1)
    factor_o4 = data_errores['ecm_beeman'].iloc[0] / (dts_ref[0]**4) if 'ecm_beeman' in data_errores.columns and not data_errores['ecm_beeman'].empty else 1e-9/(dts_ref[0]**4 if dts_ref[0]!=0 else 1)
    factor_o5 = data_errores['ecm_gear'].iloc[0] / (dts_ref[0]**5) if 'ecm_gear' in data_errores.columns and not data_errores['ecm_gear'].empty else 1e-12/(dts_ref[0]**5 if dts_ref[0]!=0 else 1)

    if dts_ref[0] > 0: # Evitar log(0)
      # plt.plot(dts_ref, factor_o2 * dts_ref**2, linestyle=':', color='gray', label='Pendiente Orden 2 ($dt^2$)')
      # plt.plot(dts_ref, factor_o4 * dts_ref**4, linestyle=':', color='darkgray', label='Pendiente Orden 4 ($dt^4$)')
      # plt.plot(dts_ref, factor_o5 * dts_ref**5, linestyle=':', color='lightgray', label='Pendiente Orden 5 ($dt^5$)')
      pass # Las líneas de referencia pueden ser difíciles de ajustar visualmente, se pueden añadir manualmente

plt.xscale('log')
plt.yscale('log')
# plt.title('Error Cuadrático Medio (ECM) vs. Paso Temporal (dt)')
plt.xlabel('Paso Temporal dt (s)')
plt.ylabel('Error Cuadrático Medio (ECM)')
plt.legend()
plt.grid(True, which="both", ls="-") # Grid para ambas escalas log
plt.tight_layout()
plt.savefig(os.path.join(output_dir, 'grafico_errores_ecm.png'))
plt.show()

print(f"Gráfico de errores ECM guardado en {output_dir}")
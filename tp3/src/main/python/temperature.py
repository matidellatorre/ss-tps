import numpy as np
import matplotlib.pyplot as plt
import os

# Parámetros
velocidades = [1.0, 3.0, 6.0, 10.0]
masa = 1.0
Ts = [0.5 * masa * v**2 for v in velocidades]

mean_pressures = []
std_pressures = []

for v in velocidades:
    filepath = f"./results/pressure_time_v{v}.txt"
    data = np.loadtxt(filepath, skiprows=1)

    tiempo = data[:, 0]
    presion = data[:, 1] + data[:, 2]

    idx_inicio = np.where(tiempo >= 0)[0][0]
    presion_filtrada = presion[idx_inicio:]

    mean_pressures.append(np.mean(presion_filtrada))
    std_pressures.append(np.std(presion_filtrada))

# Gráfico
plt.errorbar(Ts, mean_pressures, yerr=std_pressures, fmt='o')
plt.ticklabel_format(style='plain', axis='y')
plt.xlabel("Temperatura", fontsize=20)
plt.ylabel("Presión promedio (N/m)", fontsize=20)
plt.xticks(fontsize=18)
plt.yticks(fontsize=18)
plt.grid(True)
plt.tight_layout()
plt.show()
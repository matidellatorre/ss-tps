import numpy as np
import matplotlib.pyplot as plt
from glob import glob
from scipy.stats import linregress

# Leer archivos y procesar trayectorias (igual que antes)
archivos = sorted(glob("/home/gasti/Documents/ITBA/Materias/CuatriActual/SS/ss-tps/tp3/special_collisions*.txt"))
trayectorias = []

for archivo in archivos:
    datos = np.loadtxt(archivo, usecols=(1, 2, 5))  # x, y, t
    mask = datos[:, 2] <= 2.0
    datos_filtrados = datos[mask]
    if len(datos_filtrados) > 0:
        trayectorias.append((datos_filtrados[:, 0], datos_filtrados[:, 1], datos_filtrados[:, 2]))

# Interpolar y calcular DCM (igual que antes)
t_max = min(2.0, min([traj[2][-1] for traj in trayectorias]))
t_ref = np.linspace(0, t_max, 100)
X_interp = np.array([np.interp(t_ref, t, x) for x, y, t in trayectorias])
Y_interp = np.array([np.interp(t_ref, t, y) for x, y, t in trayectorias])

dcm = np.mean((X_interp - X_interp[:, 0][:, np.newaxis])**2 + (Y_interp - Y_interp[:, 0][:, np.newaxis])**2, axis=0)
mask = t_ref <= 0.425  # Filtrar hasta t = 0.425 s
t_filtrado, dcm_filtrado = t_ref[mask], dcm[mask]

# Ajuste lineal para obtener D_opt (valor de referencia)
slope, intercept, _, _, _ = linregress(t_filtrado, dcm_filtrado)
D_opt = slope / 4  # D óptimo teórico

# Función para calcular el error cuadrático medio (MSE) en m²
def calcular_error(D):
    f = 4 * D * t_filtrado  # Función modelo f(t, D) = 4Dt
    return np.mean((dcm_filtrado - f)**2)  # Error cuadrático medio (MSE)

# Rango de valores de D alrededor de D_opt
D_values = np.linspace(D_opt - 1e-3, D_opt + 1e-3, 100)  # Ajustar rango según necesidad
E_values = [calcular_error(D) for D in D_values]

# Graficar E(D) vs D
plt.figure(figsize=(10, 6))
plt.plot(D_values, E_values, 'b-', label='Error cuadrático medio $E(D)$')
plt.axvline(x=D_opt, color='r', linestyle='--', label=f'$D_{{óptimo}} = {D_opt:.2e}\, \mathrm{{m^2/s}}$')
plt.scatter(D_opt, calcular_error(D_opt), color='red', s=100, zorder=5)  # Punto mínimo

plt.xlabel('Coeficiente de difusión $D$ ($\mathrm{m^2/s}$)')
plt.ylabel('Error cuadrático medio $E(D)$ ($\mathrm{m^2}$)')
plt.grid(True)
plt.legend()
plt.title('Error cuadrático medio vs Coeficiente de Difusión')
plt.show()

print(f"Coeficiente de difusión óptimo (mínimo error): D = {D_opt:.4e} m²/s")
print(f"Error mínimo: E(D_opt) = {calcular_error(D_opt):.4e} m²")
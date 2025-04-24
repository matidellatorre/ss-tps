import numpy as np
import matplotlib.pyplot as plt
from glob import glob
from scipy.stats import linregress

# ------------------------------------------------------------------
# Configuración de fuente: leyendas en 20, etiquetas y números en 18
# ------------------------------------------------------------------
plt.rcParams.update({
    'legend.fontsize': 20,
    'axes.labelsize': 18,
    'xtick.labelsize': 18,
    'ytick.labelsize': 18
})

# ------------------------------------------------------------------
# Configuración: ajustar la ruta de los archivos si es necesario
# ------------------------------------------------------------------
pattern = "/home/gasti/Documents/ITBA/Materias/CuatriActual/SS/ss-tps/tp3/special_collisions*.txt"

# Paso 1: Leer todos los archivos
archivos = sorted(glob(pattern))
print("Archivos encontrados:", archivos)

# Paso 2: Cargar trayectorias de posición y tiempo hasta 2 s
trayectorias = []
for archivo in archivos:
    datos = np.loadtxt(archivo, usecols=(1, 2, 5))  # x, y, t
    mask = datos[:, 2] <= 2.0
    df = datos[mask]
    if df.size > 0:
        trayectorias.append((df[:, 0], df[:, 1], df[:, 2]))

# Paso 3: Definir tiempos de referencia comunes e interpolar
t_max = min(2.0, min(t[-1] for *_, t in trayectorias))
t_ref = np.linspace(0, t_max, 100)
X_interp = np.array([np.interp(t_ref, t, x) for x, y, t in trayectorias])
Y_interp = np.array([np.interp(t_ref, t, y) for x, y, t in trayectorias])

# Paso 4: Calcular desplazamiento cuadrático medio (DCM)
x0 = X_interp[:, 0][:, np.newaxis]
y0 = Y_interp[:, 0][:, np.newaxis]
dx2 = (X_interp - x0) ** 2
dy2 = (Y_interp - y0) ** 2
dcm = np.mean(dx2 + dy2, axis=0)
std_dcm = np.std(dx2 + dy2, axis=0) / np.sqrt(len(trayectorias))

# ------------------------------------------------------------------
# Gráfico 1: DCM vs tiempo hasta 2s con ajuste lineal clásico para t >= 0.425s
# ------------------------------------------------------------------
lim_inf = 0.425
idx1 = np.searchsorted(t_ref, lim_inf)
slope1, intercept1, *_ = linregress(t_ref[idx1:], dcm[idx1:])
D1 = slope1 / 4.0

plt.figure()
plt.errorbar(t_ref, dcm, yerr=std_dcm, fmt='o', label='DCM')
plt.axvline(t_ref[idx1], color='red', linestyle='--', label=f't = {lim_inf}s')
plt.xlabel('Tiempo (s)')
plt.ylabel('DCM')
plt.legend()
plt.grid(True)

# ------------------------------------------------------------------
# Gráfico 2: DCM vs tiempo hasta 0.425s con ajuste forzado a origen
# ------------------------------------------------------------------
mask2 = t_ref <= lim_inf
t2 = t_ref[mask2]
dcm2 = dcm[mask2]
std2 = std_dcm[mask2]
# Ajuste forzado: f(t) = 4·D·t
slope2 = np.sum(t2 * dcm2) / np.sum(t2 ** 2)
D2 = slope2 / 4.0

plt.figure()
plt.errorbar(t2, dcm2, yerr=std2, fmt='o', label='DCM truncado')
plt.plot(t2, slope2 * t2, 'r-', label='Ajuste forzado a origen')
plt.xlabel('Tiempo (s)')
plt.ylabel('DCM')
plt.legend()
plt.grid(True)

# ------------------------------------------------------------------
# Gráfico 3: Error cuadrático medio E(D) = (1/N)Σ[z_i - 4D t_i]^2 vs D
# ------------------------------------------------------------------
# Barrido de valores de D alrededor de la estimación
D_vals = np.linspace(0, 2 * D2, 100)
# Calcular MSE para cada D
mse_vals = [np.mean((dcm2 - 4 * Dv * t2) ** 2) for Dv in D_vals]
# Mejor D según MSE
best_idx = np.argmin(mse_vals)
best_D = D_vals[best_idx]
mse_min = mse_vals[best_idx]

plt.figure()
plt.plot(D_vals, mse_vals, 'o-', label='E(D)')
plt.axvline(best_D, color='red', linestyle='--', label=f'Mejor D = {best_D:.1e}')
plt.xlabel('D (m²/s)')
plt.ylabel('Error cuadrático medio (m²)')
plt.legend()
plt.grid(True)

plt.show()

# Impresión de resultados finales
print(f"D clásico (t ≥ 0.425s): {D1:.4e} m²/s")
print(f"D forzado (ajuste origen): {D2:.4e} m²/s")
print(f"Mejor D por MSE: {best_D:.4e} m²/s con E(D) mínima = {mse_min:.4e} m²")

import numpy as np
import matplotlib.pyplot as plt
from glob import glob
from scipy.stats import linregress

# Paso 1: Leer todos los archivos
archivos = sorted(glob("/home/gasti/Documents/ITBA/Materias/CuatriActual/SS/ss-tps/tp3/special_collisions*.txt"))
print("Archivos encontrados:", archivos)

# Guardamos las trayectorias de posición y tiempo
trayectorias = []

for archivo in archivos:
    datos = np.loadtxt(archivo, usecols=(1, 2, 5))  # x, y, t
    # Filtrar datos hasta 2 segundos
    mask = datos[:, 2] <= 2.0
    datos_filtrados = datos[mask]

    if len(datos_filtrados) > 0:  # Verificar que hay datos después del filtrado
        x = datos_filtrados[:, 0]
        y = datos_filtrados[:, 1]
        t = datos_filtrados[:, 2]
        trayectorias.append((x, y, t))

# Paso 2: Seleccionar un conjunto común de tiempos de referencia
t_max = min(2.0, min([traj[2][-1] for traj in trayectorias]))
t_ref = np.linspace(0, t_max, 100)  # 100 puntos uniformemente espaciados

# Paso 3: Interpolar las trayectorias
X_interp = []
Y_interp = []

for x, y, t in trayectorias:
    x_interp = np.interp(t_ref, t, x)
    y_interp = np.interp(t_ref, t, y)
    X_interp.append(x_interp)
    Y_interp.append(y_interp)

X_interp = np.array(X_interp)
Y_interp = np.array(Y_interp)

# Paso 4: Calcular el desplazamiento cuadrático medio (DCM)
x0 = X_interp[:, 0][:, np.newaxis]
y0 = Y_interp[:, 0][:, np.newaxis]

dx2 = (X_interp - x0) ** 2
dy2 = (Y_interp - y0) ** 2

dcm = np.mean(dx2 + dy2, axis=0)
std_dcm = np.std(dx2 + dy2, axis=0) / np.sqrt(len(trayectorias))

# Filtrar datos hasta 0.425 segundos
limite_superior = 0.425
mask = t_ref <= limite_superior
t_ref = t_ref[mask]
dcm = dcm[mask]
std_dcm = std_dcm[mask]

# Paso 5: Ajuste lineal
slope, intercept, r_value, p_value, std_err = linregress(t_ref, dcm)
D = slope / 4  # Para 2D: DCM = 4Dt

# Paso 6: Graficar
plt.errorbar(t_ref, dcm, yerr=std_dcm, fmt='o', label='DCM observado')
plt.plot(t_ref, slope * t_ref + intercept, 'r-', label='Ajuste Lineal')
plt.xlabel('Tiempo (s)')
plt.ylabel('DCM (m²)')
plt.legend()
plt.grid(True)
plt.show()

print(f"Coeficiente de difusión D: {D:.4e} m²/s")
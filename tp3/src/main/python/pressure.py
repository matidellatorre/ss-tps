import matplotlib.pyplot as plt
import pandas as pd

# Read file
df = pd.read_csv("/home/gasti/Documents/ITBA/Materias/CuatriActual/SS/ss-tps/tp3/pressure_time.txt", delim_whitespace=True)

# Set font sizes
plt.rcParams.update({'font.size': 20})

# Plot
plt.plot(df['time'], df['pressure_obstacle'], label='Presión obstáculo', color='#ff8b1f')
plt.plot(df['time'], df['pressure_walls'], label='Presión paredes', color='#1f80ff')

plt.xlabel('Tiempo [s]', fontsize=20)
plt.ylabel('Presión (N/m)', fontsize=20)
plt.legend(fontsize=18)
plt.xticks(fontsize=18)
plt.yticks(fontsize=18)
plt.grid(True)
plt.tight_layout()
plt.show()
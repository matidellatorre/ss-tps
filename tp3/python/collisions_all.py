import matplotlib.pyplot as plt
import numpy as np

# Velocities to analyze
velocities = [1.0, 3.0, 6.0, 10.0]

# Create figure and axis
plt.figure(figsize=(10, 6))

# Read and plot data for each velocity
for v in velocities:
    # Read data from file
    with open(f'./results/collisions_count_v{v}.txt', 'r') as f:
        # Skip header
        next(f)
        # Read data
        time = []
        all_collisions = []
        for line in f:
            parts = line.strip().split()
            if len(parts) >= 4:  # Ensure we have all required columns
                all_collisions.append(int(parts[2]))  # all_obstacle_col is the third column
                time.append(float(parts[3]))  # time is the fourth column
    
    # Plot the data
    plt.plot(time, all_collisions, label=f'v = {v} m/s')

# Customize the plot
plt.xlabel('Tiempo (s)', fontsize=12)
plt.ylabel('Particulas colisionadas', fontsize=12)
plt.title('Evolución de colisiones con el obstáculo', fontsize=14)
plt.grid(True, linestyle='--', alpha=0.7)
plt.legend(fontsize=10)

# Save the plot
plt.savefig('./results/collisions_evolution.png', dpi=300, bbox_inches='tight')
plt.close() 
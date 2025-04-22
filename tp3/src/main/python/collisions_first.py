import matplotlib.pyplot as plt
import numpy as np

# Create figure
plt.figure(figsize=(10, 7))

# Velocities to analyze
velocities = [1.0, 3.0, 6.0, 10.0]
colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728']  # Default matplotlib colors


# Read and plot data for each velocity
max_time = 0
all_times = []
all_first_cols = []

for v, color in zip(velocities, colors):
    filename = f'./results/collisions_count_v{v}.txt'
    
    # Read the data
    events = []
    times = []
    first_cols = []
    
    with open(filename, 'r') as f:
        # Skip header
        next(f)
        for line in f:
            parts = line.strip().split()
            event = int(parts[0][1:])  # Remove 'e' from event number
            first_col = int(parts[1])
            events.append(event)
            first_cols.append(first_col)
            # Calculate time based on event number (assuming constant time steps)
            times.append(event * 0.01)  # Adjust this factor if needed
            
            # Stop reading if we reach 200 collisions
            if first_col >= 200:
                break
    
    all_times.append(times)
    all_first_cols.append(first_cols)
    max_time = max(max_time, times[-1])

    # Plot the data
    plt.plot(times, first_cols, label=f'v={v} (m/s)', color=color)

# Customize the plot
plt.xlabel('Tiempo (s)', fontsize=12)
plt.ylabel('Partículas colisionadas', fontsize=12)
plt.xlim(0, max_time)

# Save the plot
plt.show()
plt.close()
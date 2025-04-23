import numpy as np
import matplotlib.pyplot as plt

# Velocities and corresponding temperatures
velocities = [1.0, 3.0, 6.0, 10.0]
masa = 1.0
Ts = [0.5 * masa * v**2 for v in velocities]

# Calculate collision frequencies and their standard deviations
collision_freqs = []
collision_freqs_std = []

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
    
    # Calculate collision frequency (collisions per second)
    total_time = time[-1] - time[0]
    total_collisions = all_collisions[-1] - all_collisions[0]
    collision_freq = total_collisions / total_time
    
    # Calculate error using block averaging
    # Divide the data into 10 blocks and calculate frequency for each block
    num_blocks = 10
    block_size = len(time) // num_blocks
    block_freqs = []
    
    for i in range(num_blocks):
        start_idx = i * block_size
        end_idx = (i + 1) * block_size if i < num_blocks - 1 else len(time)
        
        block_time = time[end_idx-1] - time[start_idx]
        block_collisions = all_collisions[end_idx-1] - all_collisions[start_idx]
        
        if block_time > 0:  # Avoid division by zero
            block_freq = block_collisions / block_time
            block_freqs.append(block_freq)
    
    # Calculate mean and standard deviation of block frequencies
    if block_freqs:  # Ensure we have valid frequencies
        collision_freqs.append(collision_freq)
        collision_freqs_std.append(np.std(block_freqs))
    else:
        collision_freqs.append(collision_freq)
        collision_freqs_std.append(0)  # No error if we couldn't calculate block frequencies

# Create the plot
plt.figure(figsize=(10, 6))
plt.errorbar(Ts, collision_freqs, yerr=collision_freqs_std, fmt='o', markersize=8, capsize=5)

# Customize the plot
plt.xlabel('Temperatura', fontsize=20)
plt.ylabel('Frecuencia de colisiones (colisiones/s)', fontsize=20)
plt.xticks(fontsize=18)
plt.yticks(fontsize=18)
plt.grid(True, linestyle='--', alpha=0.7)
plt.tight_layout()

# Show the plot
plt.show()
plt.close() 
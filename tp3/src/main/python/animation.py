from matplotlib.animation import FuncAnimation
import matplotlib.pyplot as plt
import pandas as pd
import re
import matplotlib.patches as patches
import numpy as np


def parse_simulation_output(file_path):
    snapshots = []
    with open(file_path, "r") as f:
        lines = f.readlines()

    current_time = None
    particle_states = []

    for line in lines:
        if line.startswith("e"):
            if current_time is not None:
                snapshots.append((current_time, particle_states))
            current_time = float(line.split()[1])
            particle_states = []
        elif line.startswith("p"):
            parts = line.strip().split()
            pid = int(parts[0][1:])  # remove 'p'
            x = float(parts[1])
            y = float(parts[2])
            vx = float(parts[3])
            vy = float(parts[4])
            particle_states.append((pid, x, y, vx, vy))

    if current_time is not None:
        snapshots.append((current_time, particle_states))

    return snapshots

# Parse the file
snapshots = parse_simulation_output("../../results/output_1.1.txt")

# Determine container radius from your simulation (for drawing boundary)
container_radius = 0.05  # 0.1m diameter

# Initialize figure
fig, ax = plt.subplots()
# Create two scatter plots - one for p0 (the obstacle) and one for other particles
obstacle = ax.scatter([], [], s=450, color='red')  # s=50 for radius=0.005
other_particles = ax.scatter([], [], s=10, color='blue')
ax.set_xlim(-container_radius * 1.2, container_radius * 1.2)
ax.set_ylim(-container_radius * 1.2, container_radius * 1.2)
ax.set_aspect('equal')
ax.axis('off')

# Add circular container boundary
container_circle = patches.Circle((0, 0), container_radius, fill=False, color='black', linewidth=1)
ax.add_patch(container_circle)

def update(frame_idx):
    time, particles = snapshots[frame_idx]

    # Separate p0 from the rest of particles
    p0_data = []
    other_particles_data = []

    for p in particles:
        if p[0] == 0:  # This is p0 (obstacle)
            p0_data.append((p[1], p[2]))
        else:
            other_particles_data.append((p[1], p[2]))

    # Update scatter plots
    if p0_data:
        obstacle.set_offsets(p0_data)
    else:
        obstacle.set_offsets([])

    if other_particles_data:
        other_particles.set_offsets(other_particles_data)
    else:
        other_particles.set_offsets([])

    return obstacle, other_particles

anim = FuncAnimation(fig, update, frames=len(snapshots), interval=10, blit=True)
plt.show()
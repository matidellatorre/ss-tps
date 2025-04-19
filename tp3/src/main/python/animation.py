from matplotlib.animation import FuncAnimation
import matplotlib.pyplot as plt
import pandas as pd
import re
import matplotlib.patches as patches


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
scat = ax.scatter([], [], s=10)
ax.set_xlim(-container_radius * 1.2, container_radius * 1.2)
ax.set_ylim(-container_radius * 1.2, container_radius * 1.2)
ax.set_aspect('equal')
time_text = ax.text(0.02, 0.95, '', transform=ax.transAxes)

# Add circular container boundary
container_circle = patches.Circle((0, 0), container_radius, fill=False, color='black', linewidth=1)
ax.add_patch(container_circle)

def update(frame_idx):
    time, particles = snapshots[frame_idx]
    x = [p[1] for p in particles]
    y = [p[2] for p in particles]
    scat.set_offsets(list(zip(x, y)))
    # time_text.set_text(f'time = {time:.4f}s')
    return scat, time_text

anim = FuncAnimation(fig, update, frames=len(snapshots), interval=10, blit=True)
plt.show()

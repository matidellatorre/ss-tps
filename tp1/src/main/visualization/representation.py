import numpy as np
import matplotlib
# Force matplotlib to use Agg backend which doesn't require a display
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as patches
import argparse
import os

def read_neighbors_file(file_path):
    """Read the neighbors file and return a dictionary mapping particle IDs to neighbor IDs."""
    neighbors = {}
    with open(file_path, 'r') as f:
        for i, line in enumerate(f, 1):  # Particle IDs start from 1
            if line.strip():  # Skip empty lines
                neighbor_ids = [int(n.strip()) for n in line.strip().split(',') if n.strip()]
                neighbors[i] = neighbor_ids
            else:
                neighbors[i] = []
    return neighbors

def read_particle_positions(file_path):
    """Read particle positions from a file (assuming format: x y)."""
    positions = {}
    try:
        with open(file_path, 'r') as f:
            lines = f.readlines()

            # Skip first line if it doesn't contain coordinates
            start_idx = 0
            if len(lines) > 0:
                try:
                    # Try to parse the first line as coordinates
                    parts = lines[0].strip().split()
                    float(parts[0]), float(parts[1])
                except (ValueError, IndexError):
                    # If parsing fails, it's likely a header
                    start_idx = 1

            # Parse the rest of the lines
            for i, line in enumerate(lines[start_idx:], 1):
                parts = line.strip().split()
                if len(parts) >= 2:
                    try:
                        x, y = float(parts[0]), float(parts[1])
                        positions[i] = (x, y)
                    except ValueError:
                        print(f"Warning: Could not parse coordinates on line {i + start_idx}: {line}")
    except Exception as e:
        print(f"Error reading positions file: {e}")

    print(f"Loaded {len(positions)} particle positions")
    return positions

def visualize_particles(positions, neighbors, L, m, selected_particle=1, output_file="particle_grid.png"):
    """
    Visualize particles on a grid with the selected particle in yellow and its neighbors in green.

    Args:
        positions: Dictionary mapping particle IDs to (x, y) positions
        neighbors: Dictionary mapping particle IDs to lists of neighbor IDs
        L: Total grid length (width and height)
        m: Number of rows/columns in the grid
        selected_particle: ID of the particle to highlight
        output_file: Path to save the output image
    """
    # Calculate cell size
    cell_size = L / m

    # Create figure and axis with explicit DPI setting
    plt.figure(figsize=(10, 10), dpi=100)
    ax = plt.gca()

    # Set up the grid
    ax.set_xlim(0, L)
    ax.set_ylim(0, L)
    ax.set_aspect('equal')

    # Draw grid lines
    for i in range(m + 1):
        position = i * cell_size
        ax.axhline(y=position, color='gray', linestyle='-', alpha=0.3)
        ax.axvline(x=position, color='gray', linestyle='-', alpha=0.3)

    # Get selected particle's neighbors
    selected_neighbors = neighbors.get(selected_particle, [])

    print(f"Selected particle: {selected_particle}")
    print(f"Neighbors: {selected_neighbors}")
    print(f"Grid size: {L}x{L} with {m}x{m} cells (cell size: {cell_size}x{cell_size})")

    # Draw all particles
    for particle_id, (x, y) in positions.items():
        print(f"Drawing particle {particle_id} at position ({x}, {y})")
        if particle_id == selected_particle:
            color = 'yellow'
            zorder = 3  # Make sure selected particle is on top
        elif particle_id in selected_neighbors:
            color = 'green'
            zorder = 2  # Neighbors above regular particles
        else:
            color = 'blue'
            zorder = 1

        # Draw particle
        circle_radius = cell_size * 0.15  # Scale circle size relative to cell size
        circle = plt.Circle((x, y), circle_radius, color=color, alpha=0.7, zorder=zorder)
        ax.add_patch(circle)

        # Add particle ID
        ax.text(x, y, str(particle_id), ha='center', va='center', color='black',
                fontsize=9, fontweight='bold', zorder=4)

    # Add cell boundaries
    for i in range(m):
        for j in range(m):
            x_pos = i * cell_size
            y_pos = j * cell_size
            rect = patches.Rectangle((x_pos, y_pos), cell_size, cell_size,
                                   linewidth=1, edgecolor='gray', facecolor='none', alpha=0.5)
            ax.add_patch(rect)

    # Add legend
    selected_patch = patches.Patch(color='yellow', label=f'Selected Particle ({selected_particle})')
    neighbor_patch = patches.Patch(color='green', label='Neighbors')
    other_patch = patches.Patch(color='blue', label='Other Particles')
    ax.legend(handles=[selected_patch, neighbor_patch, other_patch], loc='upper right')

    # Set title and labels
    ax.set_title(f'Particle Grid (L={L}, m={m}x{m}, cell size={cell_size:.2f})')
    ax.set_xlabel('X')
    ax.set_ylabel('Y')

    # Add tick marks at cell boundaries
    plt.xticks(np.arange(0, L + 0.1, cell_size))
    plt.yticks(np.arange(0, L + 0.1, cell_size))

    # Save figure
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"Visualization saved to {output_file}")

def generate_sample_data(L, m, output_dir="."):
    """Generate sample data files for testing."""
    # Generate sample positions
    pos_file = os.path.join(output_dir, "sample_positions.txt")
    with open(pos_file, "w") as f:
        f.write("x y\n")  # Header
        for i in range(1, m*m + 1):
            # Generate some positions within an LxL grid
            x = np.random.uniform(0.1, L - 0.1)
            y = np.random.uniform(0.1, L - 0.1)
            f.write(f"{x:.2f} {y:.2f}\n")

    # Generate sample neighbors
    nei_file = os.path.join(output_dir, "sample_neighbors.txt")
    with open(nei_file, "w") as f:
        for i in range(1, m*m + 1):
            neighbors = np.random.choice([j for j in range(1, m*m + 1) if j != i],
                                         size=np.random.randint(0, 3),
                                         replace=False)
            f.write(", ".join(map(str, neighbors)) + "\n")

    return pos_file, nei_file

def main():
    parser = argparse.ArgumentParser(description='Visualize particles and their neighbors on a grid')
    parser.add_argument('--positions', type=str, help='File containing particle positions')
    parser.add_argument('--neighbors', type=str, help='File containing neighbor relationships')
    parser.add_argument('--L', type=float, default=10.0, help='Total grid length (width and height)')
    parser.add_argument('--m', type=int, default=5, help='Number of rows/columns in the grid')
    parser.add_argument('--selected', type=int, default=1, help='ID of the particle to highlight')
    parser.add_argument('--output', type=str, default='particle_grid.png', help='Output file name')
    parser.add_argument('--generate-sample', action='store_true', help='Generate sample data for testing')

    args = parser.parse_args()

    # Generate sample data if requested
    if args.generate_sample:
        pos_file, nei_file = generate_sample_data(args.L, args.m)
        print(f"Generated sample data:\n  Positions: {pos_file}\n  Neighbors: {nei_file}")
        if not args.positions:
            args.positions = pos_file
        if not args.neighbors:
            args.neighbors = nei_file

    # Ensure we have input files
    if not args.positions or not args.neighbors:
        parser.error("Please provide both --positions and --neighbors files, or use --generate-sample")

    # Read particle data
    neighbors = read_neighbors_file(args.neighbors)
    positions = read_particle_positions(args.positions)

    # Visualize
    visualize_particles(positions, neighbors, args.L, args.m, args.selected, args.output)

    print("Visualization completed successfully!")

if __name__ == "__main__":
    main()
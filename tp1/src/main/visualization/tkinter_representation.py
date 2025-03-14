import tkinter as tk
from tkinter import filedialog, messagebox, ttk
import numpy as np
import matplotlib
matplotlib.use("TkAgg")
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure
import matplotlib.patches as patches
import matplotlib.pyplot as plt
import os

class ParticleGridApp:
    def __init__(self, root):
        # Existing initialization code
        self.root = root
        self.root.title("Particle Grid Visualization")
        self.root.geometry("1000x800")

        # Initialize variables
        self.positions = {}
        self.neighbors = {}
        self.radii = {}  # New variable to store particle radii
        self.L = 10.0
        self.m = 5
        self.rc = 1.0  # Default search radius
        self.selected_particle = None
        self.particles_loaded = False

        # Create main frame
        self.main_frame = tk.Frame(root)
        self.main_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)

        # Create input frame
        self.input_frame = tk.Frame(self.main_frame)
        self.input_frame.pack(fill=tk.X, pady=10)

        # File input section
        self.file_frame = tk.LabelFrame(self.input_frame, text="Input Files")
        self.file_frame.pack(fill=tk.X, pady=5)

        # Positions file
        tk.Label(self.file_frame, text="Positions File:").grid(row=0, column=0, padx=5, pady=5, sticky="w")
        self.positions_path = tk.StringVar()
        self.positions_entry = tk.Entry(self.file_frame, textvariable=self.positions_path, width=50)
        self.positions_entry.grid(row=0, column=1, padx=5, pady=5)
        tk.Button(self.file_frame, text="Browse", command=self.browse_positions).grid(row=0, column=2, padx=5, pady=5)

        # Neighbors file
        tk.Label(self.file_frame, text="Neighbors File:").grid(row=1, column=0, padx=5, pady=5, sticky="w")
        self.neighbors_path = tk.StringVar()
        self.neighbors_entry = tk.Entry(self.file_frame, textvariable=self.neighbors_path, width=50)
        self.neighbors_entry.grid(row=1, column=1, padx=5, pady=5)
        tk.Button(self.file_frame, text="Browse", command=self.browse_neighbors).grid(row=1, column=2, padx=5, pady=5)

        # Radii file - NEW
        tk.Label(self.file_frame, text="Radii File:").grid(row=2, column=0, padx=5, pady=5, sticky="w")
        self.radii_path = tk.StringVar()
        self.radii_entry = tk.Entry(self.file_frame, textvariable=self.radii_path, width=50)
        self.radii_entry.grid(row=2, column=1, padx=5, pady=5)
        tk.Button(self.file_frame, text="Browse", command=self.browse_radii).grid(row=2, column=2, padx=5, pady=5)

        # Parameters section
        self.param_frame = tk.LabelFrame(self.input_frame, text="Grid Parameters")
        self.param_frame.pack(fill=tk.X, pady=5)

        # L parameter
        tk.Label(self.param_frame, text="Grid Length (L):").grid(row=0, column=0, padx=5, pady=5, sticky="w")
        self.L_var = tk.DoubleVar(value=10.0)
        self.L_entry = tk.Entry(self.param_frame, textvariable=self.L_var, width=10)
        self.L_entry.grid(row=0, column=1, padx=5, pady=5)

        # m parameter
        tk.Label(self.param_frame, text="Grid Cells (m×m):").grid(row=0, column=2, padx=5, pady=5, sticky="w")
        self.m_var = tk.IntVar(value=5)
        self.m_entry = tk.Entry(self.param_frame, textvariable=self.m_var, width=10)
        self.m_entry.grid(row=0, column=3, padx=5, pady=5)

        # rc parameter (search radius)
        tk.Label(self.param_frame, text="Search Radius (rc):").grid(row=0, column=4, padx=5, pady=5, sticky="w")
        self.rc_var = tk.DoubleVar(value=1.0)
        self.rc_entry = tk.Entry(self.param_frame, textvariable=self.rc_var, width=10)
        self.rc_entry.grid(row=0, column=5, padx=5, pady=5)

        # Load button
        self.load_button = tk.Button(self.param_frame, text="Load & Visualize", command=self.load_and_visualize)
        self.load_button.grid(row=0, column=6, padx=20, pady=5)

        # Generate sample button
        self.sample_button = tk.Button(self.param_frame, text="Generate Sample Data", command=self.generate_sample)
        self.sample_button.grid(row=0, column=7, padx=5, pady=5)

        # Create plot frame
        self.plot_frame = tk.Frame(self.main_frame)
        self.plot_frame.pack(fill=tk.BOTH, expand=True, pady=5)

        # Create status frame
        self.status_frame = tk.Frame(self.main_frame)
        self.status_frame.pack(fill=tk.X, pady=5)

        # Selection info
        tk.Label(self.status_frame, text="Selected Particle:").pack(side=tk.LEFT, padx=5)
        self.selected_var = tk.StringVar(value="None")
        tk.Label(self.status_frame, textvariable=self.selected_var, font=("Arial", 10, "bold")).pack(side=tk.LEFT, padx=5)

        tk.Label(self.status_frame, text="Neighbors:").pack(side=tk.LEFT, padx=20)
        self.neighbors_var = tk.StringVar(value="None")
        tk.Label(self.status_frame, textvariable=self.neighbors_var).pack(side=tk.LEFT, padx=5)

        # Add radius information - NEW
        tk.Label(self.status_frame, text="Radius:").pack(side=tk.LEFT, padx=20)
        self.radius_var = tk.StringVar(value="N/A")
        tk.Label(self.status_frame, textvariable=self.radius_var).pack(side=tk.LEFT, padx=5)

        # Create figure for visualization
        self.create_figure()

    def create_figure(self):
        # Create a figure
        self.fig = Figure(figsize=(10, 8), dpi=100)
        self.ax = self.fig.add_subplot(111)

        # Create canvas
        self.canvas = FigureCanvasTkAgg(self.fig, master=self.plot_frame)
        self.canvas.get_tk_widget().pack(fill=tk.BOTH, expand=True)

        # Connect events
        self.canvas.mpl_connect("button_press_event", self.on_click)

        # Display empty grid with message
        self.ax.clear()
        self.ax.set_xlim(0, 10)
        self.ax.set_ylim(0, 10)
        self.ax.set_title("Load particle data to begin")
        self.ax.set_xlabel("X")
        self.ax.set_ylabel("Y")
        self.fig.tight_layout()
        self.canvas.draw()

    def browse_positions(self):
        file_path = filedialog.askopenfilename(
            title="Select Positions File",
            filetypes=(("Text files", "*.txt"), ("All files", "*.*"))
        )
        if file_path:
            self.positions_path.set(file_path)

    def browse_neighbors(self):
        file_path = filedialog.askopenfilename(
            title="Select Neighbors File",
            filetypes=(("Text files", "*.txt"), ("All files", "*.*"))
        )
        if file_path:
            self.neighbors_path.set(file_path)

    def browse_radii(self):
        # NEW function to browse for radii file
        file_path = filedialog.askopenfilename(
            title="Select Radii File",
            filetypes=(("Text files", "*.txt"), ("All files", "*.*"))
        )
        if file_path:
            self.radii_path.set(file_path)

    def read_positions_file(self, file_path):
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
            return positions
        except Exception as e:
            messagebox.showerror("Error", f"Error reading positions file: {e}")
            return {}

    def read_neighbors_file(self, file_path):
        neighbors = {}
        try:
            with open(file_path, 'r') as f:
                for line in f:
                    line = line.strip()
                    if not line:
                        continue

                    # Parse the new format: "ID: neighbor1, neighbor2, ..."
                    parts = line.split(':', 1)
                    if len(parts) != 2:
                        continue

                    try:
                        particle_id = int(parts[0].strip())
                        neighbors_str = parts[1].strip()

                        if neighbors_str:
                            # Parse the comma-separated list of neighbors
                            neighbor_ids = [int(n.strip()) for n in neighbors_str.split(',') if n.strip()]
                            neighbors[particle_id] = neighbor_ids
                        else:
                            # Empty neighbors list
                            neighbors[particle_id] = []
                    except ValueError as e:
                        print(f"Warning: Could not parse line: {line}, error: {e}")

            return neighbors
        except Exception as e:
            messagebox.showerror("Error", f"Error reading neighbors file: {e}")
            return {}

    def read_radii_file(self, file_path):
        # NEW function to read radii file
        radii = {}
        try:
            with open(file_path, 'r') as f:
                lines = f.readlines()

                # Skip the first two lines as they contain other parameters
                if len(lines) < 3:
                    messagebox.showerror("Error", "Radii file format invalid: too few lines")
                    return {}

                # Parse radii starting from the third line (index 2)
                for i, line in enumerate(lines[2:], 1):
                    try:
                        radius = float(line.strip())
                        radii[i] = radius
                    except ValueError:
                        print(f"Warning: Could not parse radius on line {i+2}: {line}")

            return radii
        except Exception as e:
            messagebox.showerror("Error", f"Error reading radii file: {e}")
            return {}

    def generate_sample(self):
        L = self.L_var.get()
        m = self.m_var.get()
        num_particles = m*m

        # Generate sample positions
        pos_file = "sample_positions.txt"
        with open(pos_file, "w") as f:
            f.write("x y\n")  # Header
            for i in range(1, num_particles + 1):
                # Generate some positions within an LxL grid
                x = np.random.uniform(0.1, L - 0.1)
                y = np.random.uniform(0.1, L - 0.1)
                f.write(f"{x:.2f} {y:.2f}\n")

        # Generate sample neighbors
        nei_file = "sample_neighbors.txt"
        with open(nei_file, "w") as f:
            for i in range(1, num_particles + 1):
                # Format it using the required "ID: neighbor1, neighbor2, ..." format
                neighbors = np.random.choice([j for j in range(1, num_particles + 1) if j != i],
                                          size=np.random.randint(0, 3),
                                          replace=False)
                f.write(f"{i}: {', '.join(map(str, neighbors))}\n")

        # Generate sample radii file - NEW
        radii_file = "sample_radii.txt"
        with open(radii_file, "w") as f:
            # Write the two parameter lines (can be arbitrary for sample)
            f.write("100\n")
            f.write("100\n")
            # Write a radius value for each particle (between 0.2 and 0.5)
            for i in range(1, num_particles + 1):
                f.write(f"{np.random.uniform(0.2, 0.5):.4f}\n")

        # Set the file paths
        self.positions_path.set(os.path.abspath(pos_file))
        self.neighbors_path.set(os.path.abspath(nei_file))
        self.radii_path.set(os.path.abspath(radii_file))  # NEW

        messagebox.showinfo("Success", f"Sample data generated:\n- {pos_file}\n- {nei_file}\n- {radii_file}")

    def load_and_visualize(self):
        pos_path = self.positions_path.get()
        nei_path = self.neighbors_path.get()
        radii_path = self.radii_path.get()  # NEW

        if not pos_path or not nei_path:
            messagebox.showerror("Error", "Please select both positions and neighbors files")
            return

        # Update parameters
        self.L = self.L_var.get()
        self.m = self.m_var.get()
        self.rc = self.rc_var.get()  # Get the search radius

        # Read files
        # Read files
        self.positions = self.read_positions_file(pos_path)
        self.neighbors = self.read_neighbors_file(nei_path)

        # Read radii file if provided
        self.radii = {}
        if radii_path:
            self.radii = self.read_radii_file(radii_path)

        # Verify we have particles
        if not self.positions:
            return  # Error messages already shown

        # If radii file was not provided or couldn't be read, set default radii
        if not self.radii:
            # Set default radius (cell_size * 0.15) for all particles
            cell_size = self.L / self.m
            default_radius = cell_size * 0.15
            for particle_id in self.positions.keys():
                self.radii[particle_id] = default_radius

            if radii_path:  # Only show warning if user tried to provide a radii file
                messagebox.showwarning("Warning", "Could not read radii file or file is empty. Using default radii.")

        # Make sure we have radii for all particles
        for particle_id in self.positions.keys():
            if particle_id not in self.radii:
                # Use default radius for particles without specified radius
                cell_size = self.L / self.m
                self.radii[particle_id] = cell_size * 0.15

        self.particles_loaded = True
        self.selected_particle = None
        self.selected_var.set("None")
        self.neighbors_var.set("None")
        self.radius_var.set("N/A")  # Reset radius display

        # Draw grid without selection
        self.visualize()

    def on_click(self, event):
        if not self.particles_loaded or event.xdata is None or event.ydata is None:
            return

        # Find closest particle
        min_dist = float('inf')
        closest = None

        for particle_id, (x, y) in self.positions.items():
            dist = np.sqrt((event.xdata - x)**2 + (event.ydata - y)**2)
            if dist < min_dist:
                min_dist = dist
                closest = particle_id

        # Check if click is close enough to a particle
        # Use the particle's actual radius if available, otherwise use cell_size * 0.25
        cell_size = self.L / self.m
        particle_radius = self.radii.get(closest, cell_size * 0.15)

        # Make the clickable area slightly larger than the particle
        click_threshold = max(particle_radius * 1.5, cell_size * 0.25)

        if min_dist <= click_threshold:
            self.selected_particle = closest
            neighbors_list = self.neighbors.get(closest, [])

            # Update display
            self.selected_var.set(str(closest))
            if neighbors_list:
                self.neighbors_var.set(", ".join(map(str, neighbors_list)))
            else:
                self.neighbors_var.set("None")

            # Update radius display - NEW
            self.radius_var.set(f"{self.radii.get(closest, 'N/A'):.4f}")

            # Redraw
            self.visualize()

    def visualize(self):
        if not self.particles_loaded:
            return

        # Clear the plot
        self.ax.clear()

        # Calculate cell size
        cell_size = self.L / self.m

        # Set up the grid
        self.ax.set_xlim(0, self.L)
        self.ax.set_ylim(0, self.L)
        self.ax.set_aspect('equal')

        # Draw grid lines
        for i in range(self.m + 1):
            position = i * cell_size
            self.ax.axhline(y=position, color='gray', linestyle='-', alpha=0.3)
            self.ax.axvline(x=position, color='gray', linestyle='-', alpha=0.3)

        # Get selected particle's neighbors
        selected_neighbors = self.neighbors.get(self.selected_particle, []) if self.selected_particle else []

        # Draw search radius circle if a particle is selected
        if self.selected_particle and self.selected_particle in self.positions:
            x, y = self.positions[self.selected_particle]
            search_circle = plt.Circle((x, y), self.rc, color='red', fill=False, linestyle='--', linewidth=1.5, alpha=0.7, zorder=5)
            self.ax.add_patch(search_circle)

        # Draw all particles with their respective radii
        for particle_id, (x, y) in self.positions.items():
            if particle_id == self.selected_particle:
                color = 'yellow'
                zorder = 3  # Make sure selected particle is on top
            elif particle_id in selected_neighbors:
                color = 'green'
                zorder = 2  # Neighbors above regular particles
            else:
                color = 'blue'
                zorder = 1

            # Get particle radius (use loaded radius if available, otherwise default)
            circle_radius = self.radii.get(particle_id, cell_size * 0.15)

            # Draw particle
            circle = plt.Circle((x, y), circle_radius, color=color, alpha=0.7, zorder=zorder)
            self.ax.add_patch(circle)

            # Add particle ID
            self.ax.text(x, y, str(particle_id), ha='center', va='center', color='black',
                         fontsize=9, fontweight='bold', zorder=4)

        # Add cell boundaries
        for i in range(self.m):
            for j in range(self.m):
                x_pos = i * cell_size
                y_pos = j * cell_size
                rect = patches.Rectangle((x_pos, y_pos), cell_size, cell_size,
                                         linewidth=1, edgecolor='gray', facecolor='none', alpha=0.5)
                self.ax.add_patch(rect)

        # Add legend
        legend_elements = [
            patches.Patch(color='blue', label='Particles'),
        ]

        if self.selected_particle:
            selected_radius = self.radii.get(self.selected_particle, "N/A")
            if selected_radius != "N/A":
                selected_radius_text = f"{selected_radius:.4f}"
            else:
                selected_radius_text = "Default"

            legend_elements.extend([
                patches.Patch(color='yellow', label=f'Selected Particle ({self.selected_particle}, r={selected_radius_text})'),
                patches.Patch(color='green', label='Neighbors'),
                patches.Patch(edgecolor='red', facecolor='none', label=f'Search Radius (rc={self.rc})')
            ])

        self.ax.legend(handles=legend_elements, loc='upper right')

        # Set title and labels
        if self.selected_particle:
            title = f'Particle Grid (L={self.L}, m={self.m}×{self.m}) - Selected: {self.selected_particle}'
        else:
            title = f'Particle Grid (L={self.L}, m={self.m}×{self.m}) - Click on a particle to select'

        self.ax.set_title(title)
        self.ax.set_xlabel('X')
        self.ax.set_ylabel('Y')

        # Add tick marks at cell boundaries
        self.ax.set_xticks(np.arange(0, self.L + 0.1, cell_size))
        self.ax.set_yticks(np.arange(0, self.L + 0.1, cell_size))

        # Redraw the canvas
        self.fig.tight_layout()
        self.canvas.draw()


if __name__ == "__main__":
    root = tk.Tk()
    app = ParticleGridApp(root)
    root.mainloop()
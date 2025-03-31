import matplotlib.pyplot as plt
import numpy as np
import sys

def read_monte_carlo_file(filename):
    """
    Read Monte Carlo simulation data from a file.

    Parameters:
    filename (str): Path to the input file

    Returns:
    tuple: (header line, data array)
    """
    try:
        # Read the file contents
        with open(filename, 'r') as file:
            # Read the first line as header
            header = file.readline().strip()

            # Read the rest of the data
            data = np.loadtxt(filename, skiprows=1)

        return header, data
    except Exception as e:
        print(f"Error reading file: {e}")
        return None, None

def main():
    # Fixed file path
    filename = sys.argv[1]

    # Read the data
    header, data = read_monte_carlo_file(filename)

    if data is None:
        return

    # Plot the magnetization
    plt.figure(figsize=(10, 6))
    plt.plot(data[:, 0], data[:, 1], marker='o')
    plt.title('Magnetization vs Monte Carlo Steps')
    plt.xlabel('Monte Carlo Steps')
    plt.ylabel('Magnetization (M)')
    plt.grid(True)

    # Show the plot
    plt.show()

    # Ask for stationary step via command line
    while True:
        try:
            stationary_step = int(input(f"Enter the stationary step (0-{len(data)-1}): "))

            # Validate input
            if 0 <= stationary_step < len(data):
                break
            else:
                print(f"Please enter a number between 0 and {len(data)-1}")
        except ValueError:
            print("Please enter a valid integer.")

    # Open the file for writing
    with open(filename, 'w') as file:
        # Write the header
        file.write(f"{header}\n")

        # Write the stationary step
        file.write(f"{stationary_step}\n")

        # Write the rest of the data
        np.savetxt(file, data, fmt='%g')

    print(f"File {filename} has been modified.")

if __name__ == '__main__':
    main()
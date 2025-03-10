import numpy as np
import sys


if len(sys.argv) < 3 or not sys.argv[1].isdigit() or not sys.argv[2].replace('.', '').isdigit():
    print("Please provide the number of particles to generate (int) and the grid length L (decimal)")
    sys.exit(1)

num = sys.argv[1]
l = sys.argv[2]

fileName = "Dynamic"+sys.argv[1]+".txt"
print("Generating file "+fileName)
f = open("./"+fileName, "w")
f.write("0\n") 
for i in range(0, int(num)):
    x = np.random.uniform(0, float(l))
    y = np.random.uniform(0, float(l))
    f.write(str(x)+" "+str(y)+"\n")
f.close()
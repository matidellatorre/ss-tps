#!/bin/bash

# Script para ejecutar el proceso completo de simulación y análisis

echo "===================================================="
echo "Iniciando proceso de simulación y análisis"
echo "===================================================="

# Compilar los programas Java
echo "Compilando programas Java..."
javac MetropolisMonteCarloC.java
javac MetropolisSimulationRunner.java

# Verificar si la compilación fue exitosa
if [ $? -ne 0 ]; then
    echo "Error en la compilación. Abortando."
    exit 1
fi

# Crear directorio de resultados si no existe
mkdir -p resultados

# Ejecutar las simulaciones
echo "Ejecutando simulaciones para diferentes valores de p..."
java MetropolisSimulationRunner

# Verificar si las simulaciones fueron exitosas
if [ $? -ne 0 ]; then
    echo "Error en las simulaciones. Abortando."
    exit 1
fi

# Preguntar por el estado estacionario para cada p
for file in "./resultados"/magnetizacion_*; do
    # Check if the file exists (glob expansion)
    [ -e "$file" ] || continue

    # Print the current file being processed
    echo "Processing file: $file"

    # Run the Python script with the current file path as an argument
    python3 set_stationary.py "$file"
done

mkdir -p resultados_finales

# Ejecutar el análisis de resultados
echo "Analizando resultados y generando gráficas..."
python3 analyze_results.py

# Verificar si el análisis fue exitoso
if [ $? -ne 0 ]; then
    echo "Error en el análisis. Abortando."
    exit 1
fi

echo "===================================================="
echo "Proceso completado exitosamente"
echo "Los resultados y gráficas están disponibles en:"
echo "  - resultados_finales.txt"
echo "  - evolucion_magnetizacion.png"
echo "  - resultados_observables.png"
echo "===================================================="

rm -rf ./*.class
#rm -rf ./configs
#rm -rf ./resultados
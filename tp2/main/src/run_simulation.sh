#!/bin/bash

# Script para ejecutar el proceso completo de simulación y análisis

echo "===================================================="
echo "Iniciando proceso de simulación y análisis"
echo "===================================================="

# Compilar los programas Java
echo "Compilando programas Java..."
javac MetropolisMonteCarlo.java
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
echo "  - resultados_finales.csv"
echo "  - evolucion_magnetizacion.png"
echo "  - resultados_observables.png"
echo "  - resultados_region_critica.png (si hay suficientes datos en la región crítica)"
echo "===================================================="
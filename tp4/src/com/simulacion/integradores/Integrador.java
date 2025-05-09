package com.simulacion.integradores;

import java.io.PrintWriter;
import java.util.List;

public interface Integrador {
    String getNombre();
    void simular(double dt, double tiempoTotal, PrintWriter writer, List<Double> tiempos, List<Double> posicionesNumericas);
}
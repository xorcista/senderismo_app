package com.example.senderismo;

public class Ruta {
    private String nombre;
    private String dificultad;
    private String descripcion;

    public Ruta(String nombre, String dificultad, String descripcion) {
        this.nombre = nombre;
        this.dificultad = dificultad;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDificultad() {
        return dificultad;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

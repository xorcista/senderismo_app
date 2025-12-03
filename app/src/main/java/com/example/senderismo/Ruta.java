package com.example.senderismo;

public class Ruta {
    private String nombre;
    private String dificultad;
    private String descripcion;
    private int imagenResId;


    public Ruta(String nombre, String dificultad, String descripcion, int imagenResId) {
        this.nombre = nombre;
        this.dificultad = dificultad;
        this.descripcion = descripcion;
        this.imagenResId = imagenResId;
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
    public int getImagenResId() { return imagenResId; }
}

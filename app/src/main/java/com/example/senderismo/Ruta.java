package com.example.senderismo;
import com.google.firebase.firestore.Exclude;

public class Ruta {
    private String nombre;
    private String dificultad;
    private String descripcion;
    private int imagenResId;
    @Exclude
    private String id;

    public Ruta(){
    }

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
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}

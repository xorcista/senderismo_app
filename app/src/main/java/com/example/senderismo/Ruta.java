package com.example.senderismo;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Ruta implements Serializable {

    private String userId;
    private String nombreRuta;
    private String descripcion;
    private String dificultad;
    private String tipoDeRuta;
    private double origenLat;
    private double origenLng;
    private double destinoLat;
    private double destinoLng;
    private String polyline;
    private boolean favorita = false;
    @Exclude
    private String documentId;
    public Ruta() {
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNombreRuta() { return nombreRuta; }
    public void setNombreRuta(String nombreRuta) { this.nombreRuta = nombreRuta; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public String getTipoDeRuta() {
        return tipoDeRuta;
    }

    public void setTipoDeRuta(String tipoDeRuta) {
        this.tipoDeRuta = tipoDeRuta;
    }

    public boolean isFavorita() {
        return favorita;
    }

    public void setFavorita(boolean favorita) {
        this.favorita = favorita;
    }
    public double getOrigenLat() { return origenLat; }
    public void setOrigenLat(double origenLat) { this.origenLat = origenLat; }

    public double getOrigenLng() { return origenLng; }
    public void setOrigenLng(double origenLng) { this.origenLng = origenLng; }

    public double getDestinoLat() { return destinoLat; }
    public void setDestinoLat(double destinoLat) { this.destinoLat = destinoLat; }

    public double getDestinoLng() { return destinoLng; }
    public void setDestinoLng(double destinoLng) { this.destinoLng = destinoLng; }

    public String getPolyline() { return polyline; }
    public void setPolyline(String polyline) { this.polyline = polyline; }
}

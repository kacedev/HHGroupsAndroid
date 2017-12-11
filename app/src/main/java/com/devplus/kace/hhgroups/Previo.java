package com.devplus.kace.hhgroups;

import java.io.Serializable;


public class Previo implements Serializable {

    private String album, artista, nombre, id, url, audioUrl, imagenUrl;
    private int numero;

    String getAlbum() {
        return album;
    }

    void setAlbum(String album) {
        this.album = album;
    }

    String getArtista() {
        return artista;
    }

    void setArtista(String artista) {
        this.artista = artista;
    }

    void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    String getNombre() {
        return nombre;
    }

    void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    int getNumero() {
        return numero;
    }

    void setNumero(int numero) {
        this.numero = numero;
    }

    String getImagenUrl() {
        return imagenUrl;
    }

    String getArtistaYAlbum() {
        if(album != null && !album.isEmpty())
            return String.format("%s - %s", artista, album);
        else
            return artista;
    }
}

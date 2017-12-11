package com.devplus.kace.hhgroups;


import java.io.Serializable;

public class Favorito implements Serializable {

    public Favorito(Previo p) {
        nombre = p.getNombre();
        artista = p.getArtista();
        album = p.getAlbum();
        url = p.getUrl();
        imagenUrl = p.getImagenUrl();
        tracklist = null;
    }

    public Favorito(Album album) {
        this.album = album.getTitulo();
        artista = album.getArtista();
        url = null;
        nombre = null;
        tracklist = album.getTracklist();
        imagenUrl = album.getImagenUrl();
    }

    Favorito() {};

    Previo[] tracklist;
    String nombre;
    String artista;
    String album;
    String url;
    String imagenUrl;
    String trackPath;

    boolean isAlbum() { return  album != null; }

    public Previo[] getTracklist() {
        return tracklist;
    }

    public void setTracklist(Previo[] tracklist) {
        this.tracklist = tracklist;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public void setTrackPath(String trackPath) {
        this.trackPath = trackPath;
    }

    public String getTrackPath(){
        return trackPath;
    }
}

package com.devplus.kace.hhgroups;

import java.io.Serializable;

class Album implements Serializable {

    private Previo[] tracklist;
    private String titulo, artista, imagenUrl;

    Album(Previo[] tracklist, String titulo, String artista, String imagenUrl) {
        this.tracklist = tracklist;
        this.titulo = titulo;
        this.artista = artista;
        this.imagenUrl = imagenUrl;
    }

    Previo[] getTracklist() {
        return tracklist;
    }

    String getTitulo() {
        return titulo;
    }

    String getArtista() {
        return artista;
    }

    String getImagenUrl() { return imagenUrl; }

    @Override
    public String toString() {
        return artista + " - " + titulo + " [ " + tracklist.length + " canciones]";
    }
}

package com.devplus.kace.hhgroups;


import android.content.Intent;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

class AlbumLoader extends Thread implements InterruptedThread {

    private String mUrl;
    private ServiceSender mSender;
    private boolean mStop;

    AlbumLoader(String url) {
        mUrl = url;
    }

    AlbumLoader setSender(ServiceSender serviceSender) {
        mSender = serviceSender;
        return this;
    }

    @Override
    public void run() {
        Previo[] tracklist;
        Log.v("SERVICIO-THREAD-ALBUM", "Iniciando AlbumLoader: " + mUrl);
        Document doc = null;
        try {
            doc = Jsoup.connect(mUrl).timeout(5000).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO: Gestionar errores de conexión:
        if(doc == null) {
            Log.v("SERVICIO-THREAD-ALBUM", "No se ha podido cargar el album especificado.");
            mSender.send(new Intent().setAction(Constants.BROADCAST_FILTER).putExtra(Constants.EXTRAS.SUCCESS, false), true);
            return;
        }
        String[] split = doc.select(".content_left h1").text().split(" - ");
        String artista = split[0];
        String album = split[1];
        String imagenURL = doc.select("#album_cover_front").attr("href");
        //Permisos [Exclusivo de los álbumes de instrumentales]
        /*Elements info = doc.select("div#album_permision_tooltip");
            if(info.size() != 0) {
                String tipoInstrumentales = info.first().text();
                //Log.v("ALBUM", "Tipo: " + tipoInstrumentales);
            }*/
        //Descripcion del album:
        //String descripcion = doc.select("p#long_desc").first().text();
        Elements info = doc.select(".album_tracklist li");
        tracklist = new Previo[info.size()];
        for(int i = 0; i < info.size(); i++) {
            tracklist[i] = new Previo();
            Element track = info.get(i);
            String id = track.attr("id").substring(9);
            Log.v("ALBUM-ID", id);
            String nombreTrack = track.select(".track_title").text();
            tracklist[i].setAlbum(album);
            tracklist[i].setArtista(artista);
            tracklist[i].setId(id);
            tracklist[i].setNombre(nombreTrack);
            tracklist[i].setNumero(i + 1);
        }

        if(!mStop) {
            Album a = new Album(tracklist, album, artista, imagenURL);
            mSender.sendAlbumAndCache(new Intent().setAction(Constants.BROADCAST_FILTER)
                                     .putExtra(Constants.EXTRAS.SUCCESS, true)
                                     .putExtra(Constants.EXTRAS.ALBUM, a));
            Log.v("SERVICIO-THREAD-ALBUM", "Terminando AlbumLoader");
        }
    }

    @Override
    public void shouldStop() {
        mStop = true;
    }

    @Override
    public String getTaskName() {
        return "AlbumLoader";
    }
}

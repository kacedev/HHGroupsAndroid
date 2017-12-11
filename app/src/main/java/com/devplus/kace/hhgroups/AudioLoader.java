package com.devplus.kace.hhgroups;

import android.content.Intent;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

class AudioLoader extends Thread {

    private Previo previo;
    private boolean download;
    private ServiceSender mSender;

    private final String ERROR = "ERROR";

    //download == true  -> try to download song
    //download == false -> try to play song
    AudioLoader (Previo previo, boolean download) {
        this.previo = previo;
        this.download = download;
    }

    AudioLoader setSender(ServiceSender sender) {
        mSender = sender;
        return this;
    }

    @Override
    public void run() {
        //Comprobamos si el ID existe en el Previo y si no, lo obtenemos de la web.
        if(previo.getId() == null || previo.getId().isEmpty()) {
            previo.setId(getSongId());
        }
        Log.v("TRACK-ID", previo.getId());
        //Comprobamos que no se hayan producido errores al obtener el ID.
        //TODO: Gestionar errores de conexión.
        if(previo.getId().equals(ERROR)) {
            Log.v("SERVICIO-THREAD", "No se ha podido recuperar la ID.");
            mSender.send(new Intent().setAction(Constants.BROADCAST_FILTER)
                    .putExtra(Constants.EXTRAS.SUCCESS, false), true);
            return;
        }
        //Obtenemos la URL de descarga/streaming y le pedimos al servicio que la envíe.
        try {
            Log.v("SERVICIO-THREAD", "Iniciando AudioLoader");
            final String URL = "http://www.hhgroups.com/play-%s.mp3";
            String audioUrl =
                    Jsoup.connect(String.format(URL, previo.getId()))
                         .get().body().text().replace(" ", "%20");
            Log.v("TRACK-PLAY", audioUrl);
            if(audioUrl == null || audioUrl.isEmpty()) {
                mSender.send(new Intent().setAction(Constants.BROADCAST_FILTER).putExtra(Constants.EXTRAS.SUCCESS, false), true);
            }
            previo.setAudioUrl(audioUrl);
            if(!isInterrupted()) {
                if (download)
                    mSender.send(new Intent().setAction(Constants.BROADCAST_FILTER)
                            .putExtra(Constants.EXTRAS.DOWNLOAD_PREVIO, previo)
                            .putExtra(Constants.EXTRAS.SUCCESS, true), true);
                else
                    mSender.send(new Intent().setAction(Constants.BROADCAST_FILTER)
                            .putExtra(Constants.EXTRAS.PLAY_PREVIO, previo)
                            .putExtra(Constants.EXTRAS.SUCCESS, true), true);
                Log.v("SERVICIO-THREAD", "Terminando AudioLoader");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getSongId () {
        Document doc;
        try {
            doc = Jsoup.connect(previo.getUrl()).get();
            String id = doc.select(".work_options > div").attr("class").split(" ")[0];
            Log.v("TRACK-ID", id);
            return id;
        } catch (IOException | IndexOutOfBoundsException e) {
            return ERROR;
        }
    }
}

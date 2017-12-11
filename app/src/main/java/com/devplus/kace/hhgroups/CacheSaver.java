package com.devplus.kace.hhgroups;


import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class CacheSaver extends Thread {

    private ObjectOutputStream mOutput;
    private Previo[] mFiles;
    private int mMaxPage;
    private Album mAlbum;

    public CacheSaver(FileOutputStream output, Previo[] previos, int maxPage){
        try {
            mOutput = new ObjectOutputStream(output);
        } catch (IOException e) {
            Log.v("SERVICIO-CACHE", "IOException");
            e.printStackTrace();
        }
        mFiles = previos;
        mMaxPage = maxPage;
    }

    public CacheSaver(OutputStream output, Album album) {
        try {
            mOutput = new ObjectOutputStream(output);
            mOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAlbum = album;
    }

    @Override
    public void run() {
        Log.v("SERVICIO-CACHE", "Empezando");

        if(mFiles != null) {
            Log.v("SERVICIO-CACHE", "Catálogo");
            try {
                mOutput.writeObject(mFiles);
                mOutput.writeInt(mMaxPage);
                mOutput.close();
            } catch (IOException e) {
                Log.v("SERVICIO-CACHE", "IOE");
                e.printStackTrace();
            }
        } else {
            Log.v("SERVICIO-CACHE", "Álbum");
            try {
                mOutput.writeObject(mAlbum);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.v("SERVICIO-CACHE", "Finalizado");
    }

}

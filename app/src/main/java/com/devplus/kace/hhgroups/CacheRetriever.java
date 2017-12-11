package com.devplus.kace.hhgroups;

import android.content.Intent;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class CacheRetriever extends Thread implements InterruptedThread {

    ObjectInputStream mInput;
    ServiceSender mSender;
    boolean mIsAlbum;
    boolean mStop;

    CacheRetriever(FileInputStream input, ServiceSender sender, boolean isAlbum) {
        mSender = sender;
        mIsAlbum = isAlbum;
        try {
            mInput = new ObjectInputStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if(!mIsAlbum) {
            Log.v("SERVICIO-CACHE", "Leyendo catálogo");
            Previo[] files = null;
            int maxPage = 0;
            try {
                files = (Previo[]) mInput.readObject();
                maxPage = mInput.readInt();
                mInput.close();
            } catch (IOException | ClassNotFoundException e) {
                Log.v("SERVICIO-CACHE", "Exception " + e.getMessage());
                e.printStackTrace();
            }
            if(!mStop) {
                mSender.send(HHGService.getServiceIntent(true)
                        .putExtra(Constants.EXTRAS.CATALOG, files)
                        .putExtra(Constants.EXTRAS.MAX_PAGE, maxPage), true);
            }
        } else {
            Log.v("SERVICIO-CACHE", "Leyendo album");
            Album album = null;
            try {
                album = (Album) mInput.readObject();
                mInput.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            if(!mStop) {
                mSender.send(HHGService.getServiceIntent(true)
                        .putExtra(Constants.EXTRAS.ALBUM, album), true);
            }
        }

    }

    @Override
    public void shouldStop() {
        mStop = true;
    }

    @Override
    public String getTaskName() {
        return "CacheRetriever";
    }
}

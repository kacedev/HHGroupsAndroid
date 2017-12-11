package com.devplus.kace.hhgroups;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

class FavGetter extends Thread implements InterruptedThread {

    private boolean mStop = false;
    private ServiceSender mSender;
    private File mDir;

    FavGetter(File dir) {
        mDir = dir;
    }

    FavGetter setSender(ServiceSender sender) {
        mSender = sender;
        return this;
    }

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        SQLiteDatabase db = manager.openDatabase();
        Favorito[] favs = getFavs(db);
        db.close();
        if(!mStop) {
            mSender.send(HHGService.getServiceIntent(true).putExtra(Constants.EXTRAS.FAVS, favs), true);
        }
    }

    @Override
    public void shouldStop() {
        mStop = true;
    }

    @Override
    public String getTaskName() {
        return this.getClass().getName();
    }

    private Favorito[] getFavs(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM FAVS", null);
        Favorito[] favs = new Favorito[cursor.getCount()];
        int i = 0;
        while(cursor.moveToNext()) {
            Favorito f = new Favorito();
            //ARTISTA, NOMBRE, URL, IMAGEN, TRACKPATH
            f.setArtista(cursor.getString(0));
            f.setNombre(cursor.getString(1));
            f.setUrl(cursor.getString(2));
            if(cursor.isNull(3))
                f.setImagenUrl(null);
            else
                f.setImagenUrl(cursor.getString(3));

            if(cursor.isNull(4))
                f.setTrackPath(null);
            else {
                f.setTrackPath(cursor.getString(4));
                f.setTracklist(getTracklist(f.getTrackPath()));
            }
            favs [i] = f;
            i++;
        }
        cursor.close();
        db.close();
        return favs;
    }

    private Previo[] getTracklist(String name) {
        Previo[] tracklist = null;
        ObjectInputStream objInput = null;
        File tracksFile = new File(mDir, name);
        try {
            if(!tracksFile.exists()) {
                Log.v("DATABASE", "No ha sido encontrado el fichero tracklist");
                return null;
            }
            objInput = new ObjectInputStream(new FileInputStream(tracksFile));
            tracklist = (Previo[]) objInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(objInput != null)
                    objInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tracklist;
    }


}

package com.devplus.kace.hhgroups;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

class FavSaver extends Thread {

    private Object mFile;
    private File mDir;

    FavSaver (Album a) {
        mFile = a;
    }

    FavSaver (Previo p) {
        mFile = p;
    }

    FavSaver setDir(File dir) {
        mDir = dir;
        return this;
    }

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        SQLiteDatabase db = manager.openDatabase();
        if(mFile instanceof Previo) {
            Log.v("DATABASE", "Previo");
            savePrevio(db);
        } else {
            Log.v("DATABASE", "Álbum");
            saveAlbum(db);
        }
        manager.closeDatabase();
        Log.v("DATABASE", "Fin de la inserción del favorito");
    }

    private boolean savePrevio(SQLiteDatabase db) {
        Previo p = (Previo) mFile;
        String insert;
        if(p.getImagenUrl() != null)
            insert = String.format("INSERT INTO FAVS (ARTISTA, NOMBRE, URL, IMAGEN) VALUES (%s, %s, %s, %s)",
                    p.getArtista(), p.getNombre(), p.getUrl(), p.getImagenUrl());
        else
            insert = String.format("INSERT INTO FAVS (ARTISTA, NOMBRE, URL) VALUES (%s, %s, %s)",
                    p.getArtista(), p.getNombre(), p.getUrl());
        Cursor cursorExist = db.rawQuery("SELECT * FROM FAVS WHERE ARTISTA = ? AND NOMBRE = ?",
                new String[]{p.getArtista(), p.getNombre()});
        if(cursorExist.moveToFirst()) {
            cursorExist.close();
            db.close();
            Log.v("DATABASE", "Ya existe PREVIO " + p.getArtista() + " - " + p.getNombre());
            return false;
        } else {
            Log.v("DATABASE", "Insertando PREVIO " + p.getArtista() + " - " + p.getNombre());
            db.execSQL(insert);
            cursorExist.close();
            db.close();
            return true;
        }
    }

    private boolean saveAlbum(SQLiteDatabase db) {
        Album album = (Album) mFile;
        String insert = "INSERT INTO FAVS (ARTISTA, NOMBRE, IMAGEN, TRACKPATH) VALUES (%s, %s, %s, %s)";
        Cursor cursorExist = db.rawQuery("SELECT * FROM FAVS WHERE ARTISTA = ? AND NOMBRE = ?",
                new String[]{album.getArtista(), album.getTitulo()});
        if(cursorExist.moveToFirst()) {
            Log.v("DATABASE", "Ya existe ALBUM " + album.getArtista() + " - " + album.getTitulo());
            cursorExist.close();
            db.close();
            return false;
        } else {
            String trackPath = saveTracklist(album);
            if(trackPath == null) {
                Log.v("DATABASE", "Error creando el fichero de tracklist");
                return false;
            }
            Log.v("DATABASE", "Insertando ALBUM " + album.getArtista() + " - " + album.getTitulo());
            cursorExist.close();
            db.execSQL(String.format(insert, album.getArtista(), album.getTitulo(), album.getImagenUrl(), trackPath));
            db.close();
            return true;
        }
    }

    private String saveTracklist(Album a) {
        String fileName = String.format("%s-%s.hhg", a.getArtista(), a.getTitulo());
        ObjectOutputStream objOutput = null;
        File tracksFile = new File(mDir, fileName);
        try {
            if(tracksFile.createNewFile()) {
                Log.v("DATABASE", "Ya existe el tracklist ??");
                return null;
//                tracksFile.delete();
//                tracksFile.createNewFile();
            } else {
                Log.v("DATABASE", "No existe el tracklist");
            }
            objOutput = new ObjectOutputStream(new FileOutputStream(tracksFile));
            objOutput.writeObject(a.getTracklist());
            objOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if(objOutput != null)
                    objOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }
}

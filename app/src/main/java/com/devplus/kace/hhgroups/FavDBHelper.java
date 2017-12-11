package com.devplus.kace.hhgroups;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class FavDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Favs.db";
    private static final int DB_VERSION = 1;

    public FavDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE FAVS" +
                "(_ID INTEGER PRIMARY KEY AUTOINCREMENT, ARTISTA TEXT, NOMBRE TEXT, URL TEXT, IMAGEN TEXT, TRACKPATH TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}


}

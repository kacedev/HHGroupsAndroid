package com.devplus.kace.hhgroups;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

final class Utilities {

    private static String[] albumCategories = new String[] {"Destacados", "Instrumentales", "Recientes", "Puntos"};
    private static String[] basesCategories = new String[] {"Destacados", "Uso libre", "Recientes", "Puntos"};
    private static String[] temasCategories = new String[] {"Destacados", "Recientes", "Puntos"};
    static String[] searchSections = new String[] {"Álbumes", "Bases", "Temas"};

    static String[] getCategories(int section) {
        switch(section) {
            case 0: return albumCategories;
            case 1: return basesCategories;
            case 2: return temasCategories;
            default: return new String[]{};
        }
    }

    static String getCatalogCacheName(int section, int category, int page) {
        return String.format("%d-%d-%d.hhg", section, category, page);
    }

    static String[] getContextOptions(int section) {
        if(section == 0) //Álbumes
            return new String[]{};//{"Añadir a favoritos"};
        else
            return new String[]{"Descargar"};//, "Añadir a favoritos"};
    }

    static AlertDialog.Builder getErrorDialog
            (Context context, String error, DialogInterface.OnClickListener retryClickListener,
             DialogInterface.OnClickListener cancelClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(R.string.retry, retryClickListener);
        builder.setNegativeButton(R.string.cancel, cancelClickListener);
        builder.setTitle(error);
        return builder;
    }
}

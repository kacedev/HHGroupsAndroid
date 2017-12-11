package com.devplus.kace.hhgroups;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class HHGService extends Service implements ServiceSender {

    private final IBinder mBinder = new HHGBinder();
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private Thread mTask;

    //TODO: Implementar este método en los Loaders.
    static Intent getServiceIntent(boolean success) {
        return new Intent().setAction(Constants.BROADCAST_FILTER).putExtra(Constants.EXTRAS.SUCCESS, success);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(mHandlerThread == null) {
            mHandlerThread = new HandlerThread("HHGService", Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();
        }
        if(mHandler == null && mHandlerThread != null)
            mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mTask != null && mTask instanceof InterruptedThread) {
            mHandler.removeCallbacks(mTask);
            Log.v("SERVICIO", "Interrumpiendo tarea: " + ((InterruptedThread)mTask).getTaskName());
            ((InterruptedThread)mTask).shouldStop();
        }
        switch (intent.getAction()) {
            case Constants.ACTIONS.DOWNLOAD:
                Log.v("SERVICIO", "ACTION_DOWLOAD");
                Previo downloadPrevio = (Previo) intent.getSerializableExtra(Constants.EXTRAS.PREVIO);
                mTask = new AudioLoader(downloadPrevio, true).setSender(this);
                break;
            case Constants.ACTIONS.GET_ALBUM:
                Log.v("SERVICIO", "ACTION_ALBUM");
//                String name = intent.getStringExtra(Constants.EXTRAS.ALBUM_NAME);
//                if(albumIsCached(name)) {
//                    Log.v("SERVICIO", "Obteniendo album de caché + " + name);
//                    try {
//                        mTask = new CacheRetriever(openFileInput("a-"+name), this, true);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                } else
                    mTask = new AlbumLoader(intent.getStringExtra(Constants.EXTRAS.URL)).setSender(this);
                break;
            case Constants.ACTIONS.GET_CATALOG:
                Log.v("SERVICIO", "ACTION_CATALOG");
                int section = intent.getExtras().getInt(Constants.EXTRAS.SECTION);
                int category = intent.getExtras().getInt(Constants.EXTRAS.CATEGORY);
                int page = intent.getExtras().getInt(Constants.EXTRAS.PAGE);
                String search = intent.getExtras().getString(Constants.EXTRAS.SEARCH);
                String tempName = Utilities.getCatalogCacheName(section, category, page);
                if(search.isEmpty()) {
                    if(catalogIsCached(section, category, page)) {
                        Log.v("SERVICIO", "Obteniendo catálogo de caché");
                        try {
                            mTask = new CacheRetriever(new FileInputStream(
                                    getMyCacheDir() + File.separator + tempName), this, false);
                        } catch (IOException e) {
                            Log.v("SERVICIO", "IOException: " + e.getMessage() + "\n borrando caché.");
                            File f = new File(getMyCacheDir(),
                                    Utilities.getCatalogCacheName(section, category, page));
                            f.delete();
                            mTask = new CatalogLoader(section, category, page).setSender(this);
                        }
                    } else
                        mTask = new CatalogLoader(section, category, page).setSender(this);
                } else
                    mTask = new CatalogLoader(section, page, search).setSender(this);
                break;
            case Constants.ACTIONS.PLAY:
                Log.v("SERVICIO", "ACTION_PLAY_ONE");
                Previo playPrevio = (Previo) intent.getSerializableExtra(Constants.EXTRAS.PREVIO);
                mTask = new AudioLoader(playPrevio, false).setSender(this);
                break;
            case Constants.ACTIONS.SAVE_TO_FAVS:
                Log.v("SERVICIO", "SAVE_TO_FAVS");
                Previo p = (Previo) intent.getSerializableExtra(Constants.EXTRAS.PREVIO);
                DatabaseManager.initializeInstance(new FavDBHelper(getApplicationContext()));
                if(p != null) {
                    mTask = new FavSaver(p).setDir(getFilesDir());
                    break;
                } else {
                    Album a = (Album) intent.getSerializableExtra(Constants.EXTRAS.ALBUM);
                    mTask = new FavSaver(a).setDir(getFilesDir());
                    break;
                }
            case Constants.ACTIONS.GET_FAVS:
                Log.v("SERVICIO", "GET_FAVS");
                DatabaseManager.initializeInstance(new FavDBHelper(getApplicationContext()));
                mTask = new FavGetter(getFilesDir()).setSender(this);
                break;

        }
        mHandler.post(mTask);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void send(Intent intent, boolean finish) {
        sendBroadcast(intent);
        if(finish) {
            mHandlerThread.quit();
            stopSelf();
        }
    }

    boolean catalogIsCached(int section, int category, int page) {
        return new File(getMyCacheDir(), Utilities.getCatalogCacheName(section, category, page)).exists();
    }

    boolean albumIsCached(String name) {
        return new File(getMyCacheDir(), "a-" + name).exists();
    }

    @Override
    public void sendCatalogAndCache(Intent intent) {
        send(intent, false);
        if(intent.getExtras().getBoolean(Constants.EXTRAS.SUCCESS)) {
            int section = intent.getExtras().getInt(Constants.EXTRAS.SECTION);
            int category = intent.getExtras().getInt(Constants.EXTRAS.CATEGORY);
            int page = intent.getExtras().getInt(Constants.EXTRAS.PAGE);
            String tempName = Utilities.getCatalogCacheName(section, category, page);
            try {
                File tempCache = new File(getMyCacheDir(), tempName);
                tempCache.createNewFile();
                Log.v("SERVICIO", "Creado caché -> " + tempCache.getPath());
                Previo[] catalog = (Previo[]) intent.getSerializableExtra(Constants.EXTRAS.CATALOG);
                int maxPage = intent.getExtras().getInt(Constants.EXTRAS.MAX_PAGE);
                FileOutputStream fos = new FileOutputStream(tempCache);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(catalog);
                oos.writeInt(maxPage);
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mHandlerThread.quit();
        stopSelf();
    }

    @Override
    public void sendAlbumAndCache(Intent intent) {
        send(intent, true);
//        mHandlerThread.quit();
//        stopSelf();
    }


//    @Override
//    public void cacheAndSend(Previo[] catalog, int section, int category, int page, int maxPage) {
//        try {
//            new File(Utilities.getCatalogCacheName(section, category, page)).mkdir();
//            mHandler.post(new CacheSaver(openFileOutput(Utilities.getCatalogCacheName(section, category, page), MODE_PRIVATE), catalog, maxPage));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandlerThread.quit();
    }

    File getMyCacheDir() {
        File dir = new File(getFilesDir(), "cache");
        dir.mkdir();
        return dir;
    }

    private class HHGBinder extends Binder {}
}

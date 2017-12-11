package com.devplus.kace.hhgroups;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class HHGActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ActivityListener, ServiceConnection, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Fragment mFragment;
    private Fragment mLastFragment;

    private MenuItem mItemChecked;
    private BroadcastReceiver mReceiver;
    private MediaPlayer mPlayer;
    private ProgressBar mProgress;

    private Button mPlayBtn;
    private SeekBar mSeekbar;
    private TextView mArtistText, mTitleText;
    private Handler mHandler;

    private Previo mSong;
    private Previo mDownloadSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hhg);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        //drawer.setDrawerListener(toggle);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mHandler = new Handler();

        mProgress = (ProgressBar) findViewById(R.id.playerProgress);

        findViewById(R.id.playerOptionsBtn).setOnClickListener(this);
        mPlayBtn = (Button) findViewById(R.id.playerPlayButton);
        mPlayBtn.setOnClickListener(this);

        mSeekbar = (SeekBar) findViewById(R.id.playerSeekbar);
        mSeekbar.setOnSeekBarChangeListener(this);

        mArtistText = (TextView) findViewById(R.id.playerArtistText);
        mTitleText = (TextView) findViewById(R.id.playerTitleText);

        mReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) { onBroadcast(intent); }
        };

        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnPreparedListener(this);

        mFragment = CatalogFragment.newInstance(0, "Álbumes");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameFragment, mFragment).commit();

        file = new File(getFilesDir(), "cache");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(mLastFragment != null)
                restoreLastFragment();
            else
                super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.hhg, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        //TODO: Añadir la flecha para mostrar/esconder el reproductor.
//        return false;
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Comprobamos qué botón del drawer ha sido pulsado y sustituimos el fragmento.
        changeFragment(item);
        //Cerrar el drawer:
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void restoreLastFragment() {
        mFragment = mLastFragment;
        mLastFragment = null;
        //Creamos una transición para aplicar el nuevo fragmento:
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameFragment, mFragment).commit();
        //Marcamos como seleccionado el botón del Drawer que ha sido pulsado.
        if(mItemChecked != null)
            mItemChecked.setChecked(false);
    }


    void changeFragment(MenuItem item) {
        //En función del botón que hayamos pulsado sustituimos el fragmento:
        Class fragmentClass;

        int section = 0;
        switch(item.getItemId()) {
            case R.id.nav_albumes:
                fragmentClass = CatalogFragment.class;
                break;
            case R.id.nav_bases:
                section = 1;
                fragmentClass = CatalogFragment.class;
                break;
            case R.id.nav_temas:
                section = 2;
                fragmentClass = CatalogFragment.class;
                break;
            case R.id.nav_buscar:
                fragmentClass = SearchFragment.class;
                break;
            default:
                fragmentClass = CatalogFragment.class;
        }

        if(mFragment != null)
            mLastFragment = mFragment;

        try {
            if(fragmentClass == CatalogFragment.class)
                mFragment = CatalogFragment.newInstance(section, item.getTitle().toString());
            else if(fragmentClass == SearchFragment.class) {
                //TODO: Gestionar otros posibles fragments.
                mFragment = SearchFragment.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Creamos una transición para aplicar el nuevo fragmento:
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frameFragment, mFragment).commit();
        //Marcamos como seleccionado el botón del Drawer que ha sido pulsado.
        mItemChecked = item;
        mItemChecked.setChecked(true);
        //Cambiamos el título de la toolbar.
        //setTitle(item.getTitle());
    }

    @Override
    public void requestPlay(Previo previo) {
        Intent playIntent = new Intent(getApplicationContext(), HHGService.class);
        playIntent.setAction(Constants.ACTIONS.PLAY)
                  .putExtra (Constants.EXTRAS.PREVIO, previo);
        startService(playIntent);
    }

    @Override
    public void requestDownload(Previo previo) {
        Intent downloadIntent = new Intent(getApplicationContext(), HHGService.class);
        downloadIntent.setAction(Constants.ACTIONS.DOWNLOAD)
                      .putExtra (Constants.EXTRAS.PREVIO, previo);
        startService(downloadIntent);
    }

    @Override
    public void requestCatalog(int page, int section, int category, String search) {
        Intent catalogIntent = new Intent(getApplicationContext(), HHGService.class);
        catalogIntent.setAction(Constants.ACTIONS.GET_CATALOG)
                     .putExtra(Constants.EXTRAS.SECTION, section)
                     .putExtra(Constants.EXTRAS.CATEGORY, category)
                     .putExtra(Constants.EXTRAS.PAGE, page)
                     .putExtra(Constants.EXTRAS.SEARCH, search);
        startService(catalogIntent);
    }

    @Override
    public void requestAlbum(Previo albumPrevio) {
        if(!(mFragment instanceof AlbumFragment)) {
            //Cambiamos el fragmento y le pedimos al servicio que nos entregue el álbum:
            mLastFragment = mFragment;
            mFragment = AlbumFragment.newInstance(albumPrevio);
            if (mItemChecked != null)
                mItemChecked.setChecked(false);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frameFragment, mFragment).commit();
        }
        Intent albumIntent = new Intent(getApplicationContext(), HHGService.class);
        albumIntent.setAction(Constants.ACTIONS.GET_ALBUM)
                   .putExtra(Constants.EXTRAS.URL, albumPrevio.getUrl());
        startService(albumIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_FILTER);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.v("SERVICIO", "Conectado a la actividad");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.v("SERVICIO", "Desconectado de la actividad");
    }

    void onBroadcast(Intent intent) {
        //Comprobamos si es una descarga o una reproducción:
        Previo p = (Previo) intent.getSerializableExtra(Constants.EXTRAS.DOWNLOAD_PREVIO);
        if(p != null) { //Gestionar descarga
            Log.v("SERVICIO", "Recibido previo de descarga");
            mDownloadSong = p;
            if(Build.VERSION.SDK_INT >= 23 ) {
                int permissionCheck = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    downloadSong();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
            return;
        }
        p = (Previo) intent.getSerializableExtra(Constants.EXTRAS.PLAY_PREVIO);
        if(p != null) { //Gestionar la reproducción
            Log.v("SERVICIO", "Recibido previo de reproducción");
            mPlayer.stop();
            mPlayer.reset();
            mProgress.setVisibility(View.VISIBLE);
            mPlayBtn.setEnabled(false);
            mArtistText.setText(p.getArtistaYAlbum());
            mTitleText.setText(p.getNombre());
            mSong = p;
            try {
                mPlayer.setDataSource(p.getAudioUrl());
                mPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if(mFragment instanceof AlbumFragment) {
            Log.v("SERVICIO", "Recibido álbum");
            Album album = (Album) intent.getSerializableExtra(Constants.EXTRAS.ALBUM);
            if(album != null){
                Log.v("SERVICIO", "CORRECTO");
                ((AlbumFragment) mFragment).setAlbum(album);
            } else {
                ((OnErrorListener) mFragment).onError("Error accediendo a HHGroups.");
            }
            return;
        } else if (mFragment instanceof CatalogFragment) {
            Log.v("SERVICIO", "Recibido catálogo");
            Previo[] catalog = (Previo[]) intent.getSerializableExtra(Constants.EXTRAS.CATALOG);
            if(catalog != null) {
                //Conexión correcta con resultados:
                Log.v("SERVICIO", "CORRECTO");
                ((CatalogFragment)mFragment).setCatalog(catalog, intent.getIntExtra(Constants.EXTRAS.MAX_PAGE, 0));
            } else {
                Log.v("SERVICIO", "FALLIDO");
                //Error en la conexión con la web:
                ((OnErrorListener) mFragment).onError("Error accediendo a HHGroups.");
            }
            return;
        } else if(mFragment instanceof SearchFragment) {
            Log.v("SERVICIO", "Recibidos resultados de búsqueda.");
            Previo[] catalog = (Previo[]) intent.getSerializableExtra(Constants.EXTRAS.CATALOG);
            if(catalog != null) {
                //Conexión correcta con resultados:
                Log.v("SERVICIO", "CORRECTO");
                ((SearchFragment)mFragment).setCatalog(catalog, intent.getIntExtra(Constants.EXTRAS.MAX_PAGE, 0));
            } else {
                if(intent.getBooleanExtra(Constants.EXTRAS.SUCCESS, false)) {
                    //Hemos recuperado información de la búsqueda y no hay resultados:
                    Log.v("SERVICIO", "SIN_RESULTADOS");
                    ((OnErrorListener) mFragment).onError("No se han encontrado resultados.");
                } else {
                    //Ha ocurrido un problema conectando con la web:
                    Log.v("SERVICIO", "FALLIDO");
                    ((OnErrorListener) mFragment).onError("Error accediendo a HHGroups.");
                }
            }
            return;
        } else if(mFragment instanceof FavoritesFragment) {
            Log.v("SERVICIO", "Recibdos favoritos");
            Favorito[] favs = (Favorito[]) intent.getSerializableExtra(Constants.EXTRAS.FAVS);
            if(favs != null) {
                Log.v("SERVICIO", "Correcto");
            }
        }
    }

    File file;

    @Override
    public void onStop() {
        Log.v("SERVICIO-CACHE", "onStop");
        for(File f : file.listFiles()) {
            Log.v("SERVICIO-CACHE", "Eliminado uno: " + f.getName());
            f.delete();
        }
        super.onStop();
    }

    void downloadSong() {
        String fileName = String.format("%s - %s.mp3", mDownloadSong.getArtistaYAlbum(), mDownloadSong.getNombre());
        String downloadDir = Environment.getExternalStorageDirectory().getPath() + File.separator + "HHGroups";
        Log.v("DESCARGA", downloadDir);
        File dir = new File(downloadDir);
        if(!dir.exists())
            dir.mkdir();
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mDownloadSong.getAudioUrl()));
        request.setTitle(getResources().getString(R.string.app_name));
        request.setDescription(fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(getResources().getString(R.string.app_name) , fileName);
        downloadManager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            downloadSong();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.reset();
        if (Build.VERSION.SDK_INT >= 21) {
            mPlayBtn.setBackground(getDrawable(R.drawable.play_icon));
        } else {
            mPlayBtn.setBackgroundResource(R.drawable.play_icon);
        }
        mHandler.removeCallbacks(mSeekbarUpdater);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mProgress.setVisibility(View.INVISIBLE);
        mPlayBtn.setEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            mPlayBtn.setBackground(getDrawable(R.drawable.pause_icon));
        } else {
            mPlayBtn.setBackgroundResource(R.drawable.pause_icon);
        }
        mSeekbar.setMax(mediaPlayer.getDuration());
        mHandler.post(mSeekbarUpdater);
        mediaPlayer.start();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.playerPlayButton:
                if(mPlayer.isPlaying()) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        mPlayBtn.setBackground(getDrawable(R.drawable.play_icon));
                    } else {
                        mPlayBtn.setBackgroundResource(R.drawable.play_icon);
                    }
                    mPlayer.pause();
                } else {
                    if (Build.VERSION.SDK_INT >= 21) {
                        mPlayBtn.setBackground(getDrawable(R.drawable.pause_icon));
                    } else {
                        mPlayBtn.setBackgroundResource(R.drawable.pause_icon);
                    }
                    mPlayer.start();
                }
                break;
            case R.id.playerOptionsBtn:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(new String[]{"Descargar"},//, "Añadir a favoritos"},
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if(mSong != null) {
                            if(which == 0) {
                                requestDownload(mSong);
                                Log.v("PLAYER", "Descargar " + mSong.getNombre());
                            } else {
                                Log.v("PLAYER", "Añadir a favoritos." + mSong.getArtistaYAlbum());
                            }
                        }
                    }
                });
                builder.show();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            mPlayer.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private SeekbarUpdater mSeekbarUpdater = new SeekbarUpdater();

    private class SeekbarUpdater implements Runnable {
        @Override
        public void run() {
            mSeekbar.setProgress(mPlayer.getCurrentPosition());
            Log.v("SEEKBAR_UPDATER", "Se ha actualizado otra vez.");
            mHandler.postDelayed(this, 1000);
        }
    }
}

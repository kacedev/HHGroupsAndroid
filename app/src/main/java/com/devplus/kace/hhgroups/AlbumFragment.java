package com.devplus.kace.hhgroups;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import static android.view.View.GONE;
import static android.view.View.inflate;

public class AlbumFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener,
                                                        OnErrorListener {

    private static final String PARAM_ALBUM_PREVIO = "com.devplus.kace.hhgroups.PARAM_ALBUM_PREVIO";

    private Previo mPrevio;

    private RecyclerView mRecycler;
    private AlbumAdapter mAdapter;
    private ImageView mImage;
    private TextView mArtistText, mTitleText;
    private ProgressBar mProgress;

    private ActivityListener mActivity;

    public AlbumFragment() {}

    public static AlbumFragment newInstance(Previo albumPrevio) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_ALBUM_PREVIO, albumPrevio);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ActivityListener)
            mActivity = (ActivityListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPrevio = (Previo) getArguments().getSerializable(PARAM_ALBUM_PREVIO);
        }
    }

    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album, null);
        mRecycler = root.findViewById(R.id.albumTracklist);
        mImage = root.findViewById(R.id.albumImagen);
        if(mPrevio != null) {
            Glide.with(getContext()).load(mPrevio.getImagenUrl()).into(mImage);
        }
        mTitleText = root.findViewById(R.id.albumTituloText);
        mArtistText = root.findViewById(R.id.albumArtistaText);
        mProgress = root.findViewById(R.id.albumProgress);
        return root;
    }

    @Override
    public void onClick(View view) {
        int position = mRecycler.getChildAdapterPosition(view);
        mActivity.requestPlay(mAdapter.getPrevio(position));
    }


    public void setAlbum(Album album) {
        //mAlbum = album;
        mProgress.setVisibility(GONE);
        //Cargamos la imagen:
        Glide.with(getContext()).load(album.getImagenUrl()).placeholder(mImage.getDrawable()).into(mImage);
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View imageDialog = inflate(getContext(), R.layout.image_dialog, null);
                ((ImageView) imageDialog.findViewById(R.id.imageDialog)).setImageDrawable(mImage.getDrawable());
                builder.setView(imageDialog);
                builder.show();
            }
        });
        //Recyler y Adapter:
        mAdapter = new AlbumAdapter(getContext(), album.getTracklist());
        mAdapter.setClickListener(this);
        mAdapter.setLongClickListener(this);
        mRecycler.addItemDecoration(new RecyclerSeparator().setSeparation(5));
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        //InfoBox:
        mArtistText.setText(album.getArtista());
        mTitleText.setText(album.getTitulo());
    }

    @Override
    public boolean onLongClick(View view) {
        final int position = mRecycler.getChildAdapterPosition(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(new String[]{"Descargar"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mActivity.requestDownload(mAdapter.getPrevio(position));
            }
        });
        builder.show();
        return true;
    }

    @Override
    public void onError(String error) {
        Utilities.getErrorDialog(getContext(), error, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mActivity.requestAlbum(mPrevio);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProgress.setVisibility(View.INVISIBLE);
            }
        }).show();
    }
}

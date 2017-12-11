package com.devplus.kace.hhgroups;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Previo[] mPrevios;
    private View.OnClickListener mListener;
    private View.OnLongClickListener mLongListener;

    AlbumAdapter(Context context, Previo[] previos) {
        mInflater = LayoutInflater.from(context);
        mPrevios = previos;
    }

    @Override
    @SuppressLint("InflateParams")
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = mInflater.inflate(R.layout.album_track, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mPrevios[position]);
    }

    @Override
    public int getItemCount() {
        if(mPrevios == null)
            return 0;
        return mPrevios.length;
    }

    void setClickListener(View.OnClickListener newListener) {
        mListener = newListener;
    }

    void setLongClickListener(View.OnLongClickListener newLongListener) { mLongListener = newLongListener; }

    Previo getPrevio(int position) { return mPrevios[position]; }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView postText, titleText;

        ViewHolder(View itemView) {
            super(itemView);
            postText = itemView.findViewById(R.id.albumItemPosText);
            titleText = itemView.findViewById(R.id.albumItemTitulo);
            if(mListener != null)
                itemView.setOnClickListener(mListener);
            if(mLongListener != null)
                itemView.setOnLongClickListener(mLongListener);
        }

        void bind(Previo previo) {
            postText.setText(String.valueOf(previo.getNumero()));
            titleText.setText(previo.getNombre());
        }
    }
}

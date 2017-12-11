package com.devplus.kace.hhgroups;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Previo[] mPrevios;
    private View.OnClickListener mListener;
    private View.OnLongClickListener mLongListener;

    CatalogAdapter(Context context, Previo[] previos) {
        mInflater = LayoutInflater.from(context);
        mPrevios = previos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = mInflater.inflate(R.layout.catalog_item, parent, false);
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

    Previo getPrevio(int position) { return mPrevios[position]; }

    void setClickListener(View.OnClickListener newListener) {
        mListener = newListener;
    }

    void setLongClickListener(View.OnLongClickListener newLongListener) {
        mLongListener = newLongListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView topText, artistText, titleText;
        ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(mListener);
            itemView.setOnLongClickListener(mLongListener);
            image = itemView.findViewById(R.id.catalogItemImagen);
            topText = itemView.findViewById(R.id.catalogItemTopText);
            artistText = itemView.findViewById(R.id.catalogItemArtistaText);
            titleText = itemView.findViewById(R.id.catalogItemTituloText);
        }

        void bind(Previo previo) {
            String imagenUrl = previo.getImagenUrl();
            if(imagenUrl != null && !imagenUrl.isEmpty() && imagenUrl.length() < 300)
                Glide.with(mInflater.getContext()).load(previo.getImagenUrl()).into(image);
            else {
                if (Build.VERSION.SDK_INT >= 21) {
                    image.setImageDrawable(mInflater.getContext().getResources().getDrawable(R.drawable.music_icon, null));
                } else {
                    image.setImageResource(R.drawable.music_icon);
                }
            }
            if(previo.getNumero() > 0) {
                topText.setVisibility(View.VISIBLE);
                topText.setText(String.valueOf(previo.getNumero()));
            } else
                topText.setVisibility(View.GONE);
            artistText.setText(previo.getArtista());
            titleText.setText(previo.getNombre());
        }
    }

    void updateElements(Previo[] newPrevios) {
        mPrevios = newPrevios;
        notifyDataSetChanged();
    }
}

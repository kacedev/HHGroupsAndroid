package com.devplus.kace.hhgroups;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FavoritesFragment extends Fragment {

    ActivityListener mActivity;

    public FavoritesFragment() {}

    public static FavoritesFragment newInstance(ActivityListener ativity) {
        return new FavoritesFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ActivityListener) {
            mActivity = (ActivityListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: mActivity.requestFavs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.fragment_favs, null);
        return root;

    }

}

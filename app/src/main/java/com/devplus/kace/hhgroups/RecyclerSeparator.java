package com.devplus.kace.hhgroups;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

class RecyclerSeparator extends RecyclerView.ItemDecoration {

    private int mSpace;

    RecyclerSeparator() {}

    RecyclerSeparator setSeparation(int space) {
        mSpace = space;
        return this;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //outRect.left = mSpace;
        //outRect.right = mSpace;
        outRect.bottom = mSpace;
//        if(parent.getChildAdapterPosition(view) == 0)
//            outRect.top = mSpace;
    }
}

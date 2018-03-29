package com.example.uploadservice.adapter.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Description：网格话题布局分隔线
 * Created by kang on 2018/3/29.
 */

public class ThreeGridDecoration extends RecyclerView.ItemDecoration {
    private int halfHOffset;
    private int bottomOffset;

    public ThreeGridDecoration(int halfOffset, int bottomOffset) {
        this.halfHOffset = halfOffset;
        this.bottomOffset = bottomOffset;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position % 3 == 0) {
            outRect.right = halfHOffset;
        } else if (position % 3 == 2) {
            outRect.left = halfHOffset;
        }

        outRect.bottom = bottomOffset;
    }
}

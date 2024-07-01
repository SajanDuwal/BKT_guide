package com.sajan.bktguide.rvAdapters

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RvCustomIconDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = 1
        } else {
            outRect.top = 3
        }

        if (parent.getChildLayoutPosition(view) == parent.adapter!!.itemCount - 1) {
            outRect.bottom = 2
        } else {
            outRect.bottom = 3
        }
        outRect.right = 2
        outRect.left = 2
    }
}
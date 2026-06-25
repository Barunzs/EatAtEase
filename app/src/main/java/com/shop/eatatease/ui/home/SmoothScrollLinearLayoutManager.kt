package com.shop.eatatease.ui.home

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class SmoothScrollLinearLayoutManager(context: Context?,
                                      orientation: Int,
                                      reverseLayout: Boolean): LinearLayoutManager(context, orientation, reverseLayout){

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State?,
        position: Int
    ) {
        val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {

            // Customize the speed here.
            // Higher number = slower scroll speed.
            // The default is usually 25f. Try 100f or 150f for a slower glide.
            private val MILLISECONDS_PER_INCH = 100f

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }

        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }
}
package com.skyd.imomoe.view.component

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import tv.danmaku.ijk.media.player.pragma.DebugLog


class WrapLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            DebugLog.d("WrapLinearLayoutManager", "捕获异常：" + e.message)
        }
    }
}
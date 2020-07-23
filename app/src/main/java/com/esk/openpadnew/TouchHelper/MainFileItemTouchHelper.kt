package com.esk.openpadnew.TouchHelper

import android.content.SharedPreferences
import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.esk.openpadnew.Adapter.MainFileViewHolder
import com.esk.openpadnew.PREF_SWIPE_DELETE

/**
 * Main 액티비티의 RecyclerView 의 아이템을 Swipe 하여 삭제하기 위한 리스너 인터페이스
 */
interface RecyclerItemTouchHelperListener {
    fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int)
}


/**
 * Main 액티비티의 RecyclerView 의 아이템을 Swipe 하여 삭제하기 위한 터치 헬퍼
 * @param dragFlags 드래그 플래그
 * @param swipeFlags 스와잎 플래그
 * @param listener 스와잎 리스너
 * @param sharedPref SharedPreferences
 */
class MainFileItemTouchHelper(dragFlags: Int, swipeFlags: Int, listener: RecyclerItemTouchHelperListener, sharedPref: SharedPreferences)
    : ItemTouchHelper.SimpleCallback(dragFlags, swipeFlags) {
    private var mSharedPref: SharedPreferences = sharedPref
    private var mListener: RecyclerItemTouchHelperListener = listener
    private var mDragFlags: Int = dragFlags
    private var mSwipeFlags: Int = swipeFlags

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (mSharedPref.getBoolean(PREF_SWIPE_DELETE, true)) {
            makeMovementFlags(mDragFlags, mSwipeFlags)
        } else {
            makeMovementFlags(0, 0)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mListener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val foregroundView: View? = (viewHolder as MainFileViewHolder).foregroundView
        if (foregroundView != null) {
            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val foregroundView: View? = (viewHolder as MainFileViewHolder).foregroundView
        if (foregroundView != null) {
            getDefaultUIUtil().clearView(foregroundView)
        }
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val foregroundView: View? = (viewHolder as MainFileViewHolder).foregroundView
        if (foregroundView != null) {
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val foregroundView: View? = (viewHolder as MainFileViewHolder).foregroundView
            if (foregroundView != null) {
                getDefaultUIUtil().onSelected(foregroundView)
            }
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }
}
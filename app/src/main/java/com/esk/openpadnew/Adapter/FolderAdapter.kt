package com.esk.openpadnew.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.esk.openpadnew.DataType.FolderObject
import com.esk.openpadnew.R
import kotlinx.android.synthetic.main.item_browser_big.view.*
import kotlinx.android.synthetic.main.item_browser_small.view.*

/**
 * Folder 액티비티에서 RecyclerView 에 사용하는 뷰홀더
 * @param view View
 * @param isBig 크게보기 유무
 */
class FolderViewHolder(val view: View, isBig: Boolean) : RecyclerView.ViewHolder(view) {
    val folderIcon: ImageView
    val folderName: TextView
    lateinit var fileCounts: TextView

    init {
        if (isBig) {
            folderName = view.item_browser_name_big
            folderIcon = view.item_browser_icon_big
            fileCounts = view.item_browser_files_big
        } else {
            folderName = view.item_browser_name_small
            folderIcon = view.item_browser_icon_small
        }
    }
}


/**
 * Folder 액티비티에서 RecyclerView 에 사용하는 어댑터
 * @param mContext 컨텍스트
 * @param mFolders 폴더 목록
 * @param mIsBig 크게보기 유무
 * @param action 클릭 액션
 */
class FolderAdapter(private val mContext: Context, private val mFolders: ArrayList<FolderObject>, private val mIsBig: Boolean, action: ClickAction)
    : RecyclerView.Adapter<FolderViewHolder>() {
    private val mAction: ClickAction = action   // 클릭 액션

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view: View = when (mIsBig) {
            true -> LayoutInflater.from(mContext).inflate(R.layout.item_browser_big, null)
            false -> LayoutInflater.from(mContext).inflate(R.layout.item_browser_small, null)
        }
        return FolderViewHolder(view, mIsBig)
    }

    override fun getItemCount(): Int {
        return mFolders.size
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        if (mIsBig) {
            val counts: String = mFolders[position].fileCounts.toString() + mContext.getString(R.string.item_folder_files)
            holder.fileCounts.text = counts
        }

        val folderType: FolderObject.FolderType = mFolders[position].folderType
        when (folderType) {
            FolderObject.FolderType.Primitive -> {
                holder.folderName.text = mContext.getString(R.string.folder_default)
                holder.folderIcon.setImageResource(R.drawable.baseline_folder_shared_black_24)
            }
            FolderObject.FolderType.Widget -> {
                holder.folderName.text = mContext.getString(R.string.folder_widget)
                holder.folderIcon.setImageResource(R.drawable.baseline_folder_shared_black_24)
            }
            FolderObject.FolderType.Normal -> {
                holder.folderName.text = mFolders[position].fileName
                holder.folderIcon.setImageResource(R.drawable.baseline_folder_black_24)
            }
        }

        holder.view.setOnClickListener { v: View? ->
            if (v != null) {
                mAction.onClick(v, holder.adapterPosition)
            }
        }

        holder.view.setOnLongClickListener { v: View? ->
            if (v != null) {
                mAction.onLongClick(v, holder.adapterPosition)
                return@setOnLongClickListener true
            }
            return@setOnLongClickListener false
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mFolders[position].folderType.value
    }
}
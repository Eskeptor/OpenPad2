package com.esk.openpadnew.Adapter

import android.content.SharedPreferences
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.esk.openpadnew.DataType.MainFileObject
import com.esk.openpadnew.MainActivity
import com.esk.openpadnew.*
import com.esk.openpadnew.R
import kotlinx.android.synthetic.main.item_main_big.view.*
import kotlinx.android.synthetic.main.item_main_small.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 리스트의 아이템을 단일 클릭, 또는 롱 클릭했을 때 사용할 클릭 액션 이벤트용 인터페이스
 */
interface ClickAction {
    fun onClick(view: View, position: Int)
    fun onLongClick(view: View, position: Int)
}


/**
 * Main 액티비티에서 사용할 RecyclerView 의 뷰홀더
 * @param view 뷰
 * @param isBig 크게보기 유무
 */
class MainFileViewHolder(val view: View, isBig: Boolean) : RecyclerView.ViewHolder(view) {
    val image: ImageView
    val title: TextView
    val contents: TextView
    val date: TextView
    val foregroundView: RelativeLayout
    val backgroundView: RelativeLayout

    init {
        if (isBig) {
            image = view.item_main_image_big
            title = view.item_main_title_big
            contents = view.item_main_context_big
            date = view.item_main_date_big
            foregroundView = view.item_main_foreground_big
            backgroundView = view.item_main_background_big
        } else {
            image = view.item_main_image_small
            title = view.item_main_title_small
            contents = view.item_main_context_small
            date = view.item_main_date_small
            foregroundView = view.item_main_foreground_small
            backgroundView = view.item_main_background_small
        }
    }
}


/**
 * Main 액티비티에서 사용할 RecyclerView 의 패딩(ItemDecoration)
 * @param bottom Rect 의 bottom
 * @param left Rect 의 left
 * @param right Rect 의 right
 * @param top Rect 의 top
 */
class RecyclerViewPadding(private var bottom: Int, private var right: Int, private var left: Int, private var top: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = bottom
        outRect.right = right
        outRect.left = left
        outRect.top = top
    }
}


/**
 * Main 액티비티에서 사용할 RecyclerView 의 어댑터
 * @param mIsBig 크게보기 유무
 * @param action 클릭 액션
 * @param activity 메인 액티비티
 * @param mainFiles 해당 폴더의 메모 파일 리스트
 * @param sharedPref SharedPreferences
 */
class MainFileAdapter(mainFiles: ArrayList<MainFileObject>,
                      sharedPref: SharedPreferences,
                      private val activity: MainActivity,
                      private val mIsBig: Boolean,
                      action: ClickAction)
    : RecyclerView.Adapter<MainFileViewHolder>() {
    private var mMainFiles: ArrayList<MainFileObject> = mainFiles
    private var mSharedPref: SharedPreferences = sharedPref
    private var mAction: ClickAction = action

    // TODO( 이미지 캐시 어떻게 할지 생각하기)
    /*private val mMyGlideOptions: RequestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainFileViewHolder {
        val view: View = when (mIsBig) {
            true -> LayoutInflater.from(parent.context).inflate(R.layout.item_main_big, null)
            false -> LayoutInflater.from(parent.context).inflate(R.layout.item_main_small, null)
        }
        return MainFileViewHolder(view, mIsBig)
    }

    override fun getItemViewType(position: Int): Int {
        return mMainFiles[position].fileType.value
    }

    override fun onBindViewHolder(holder: MainFileViewHolder, position: Int) {
        if (getItemViewType(position) == MainFileObject.FileType.Image.value) {
            if (mSharedPref.getBoolean(PREF_VIEW_IMAGE, true)) {
                // TODO 이미지 갱신 처리하기
                val myGlideOptions: RequestOptions = RequestOptions()
                    .signature(ObjectKey(UUID.randomUUID().toString()))
                Glide.with(activity).clear(holder.image)
                Glide.with(activity).load(mMainFiles[position].filePath).apply(myGlideOptions).into(holder.image)
                holder.image.visibility = View.VISIBLE
            } else {
                holder.image.visibility = View.GONE
            }
            holder.contents.text = mMainFiles[position].oneLinePreview
            holder.title.text = mMainFiles[position].fileTitle
            holder.date.text = mMainFiles[position].modifiedDate
        } else {
            holder.title.text = mMainFiles[position].fileTitle
            holder.contents.text = mMainFiles[position].oneLinePreview
            holder.date.text = mMainFiles[position].modifiedDate
            holder.image.visibility = View.GONE
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

    override fun getItemCount(): Int {
        return mMainFiles.size
    }
}
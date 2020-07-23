package com.esk.openpadnew

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.esk.openpadnew.Adapter.ClickAction
import com.esk.openpadnew.Adapter.FolderAdapter
import com.esk.openpadnew.Adapter.RecyclerViewPadding
import com.esk.openpadnew.DataType.FolderObject
import com.esk.openpadnew.Util.CreateIntent
import com.esk.openpadnew.Util.LogBot
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_folder.*
import kotlinx.android.synthetic.main.dialog_folder_new.view.*
import java.io.File
import java.lang.ref.WeakReference
import java.util.regex.Pattern

private const val HANDLER_CREATE_LIST = 100
private const val HANDLER_REFRESH = 101

private const val FOLDER_NAME_REGEX: String = "^[a-zA-Z0-9가-힣\\s]*$"


class FolderActivity : AppCompatActivity() {
    private lateinit var mContextThis: Context
    private lateinit var mFolderHandler: FolderHandler
    private lateinit var mFolderAdapter: FolderAdapter
    private lateinit var mSharedPref: SharedPreferences
    private lateinit var mCurSortType: SortType
    private lateinit var mCurViewStyleMenu: MenuItem
    private lateinit var mItemDecoration: RecyclerView.ItemDecoration
    private var mIsCurSortASC: Boolean = true
    private var mIsCurViewStyleList: Boolean = true
    private val mCurFolderList: ArrayList<FolderObject> = ArrayList()
    private var mPasswordFlag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder)

        mContextThis = applicationContext

        // 액티비티 이름 변경
        setTitle(R.string.folder_title)

        // 상단 그림자 제거
        supportActionBar?.elevation = 0.0f

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            folder_layout_sort.setBackgroundColor(resources.getColor(R.color.white, theme))
        }

        // 폴더 핸들러 생성
        mFolderHandler = FolderHandler(this)

        // Pref 연결
        mSharedPref = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)

        // 패스워드창 플래그 설정
        mPasswordFlag = false

        // List 의 View Style 확인
        mIsCurViewStyleList = mSharedPref.getBoolean(PREF_FOLDER_VIEW_STYLE, true)

        // List 에 사용할 데코레이션
        mItemDecoration = RecyclerViewPadding(10, 5, 5, 5)

        // 정렬 타입
        mCurSortType = when (mSharedPref.getInt(PREF_FOLDER_SORT_TYPE, SortType.Name.value)) {
            SortType.Name.value -> {
                folder_btn_sort_type.setText(R.string.menu_sort_by_name)
                SortType.Name
            }
            SortType.Time.value -> {
                folder_btn_sort_type.setText(R.string.menu_sort_by_time)
                SortType.Time
            }
            SortType.Files.value-> {
                folder_btn_sort_type.setText(R.string.menu_sort_by_files)
                SortType.Files
            }
            else                -> {
                folder_btn_sort_type.setText(R.string.menu_sort_by_name)
                SortType.Name
            }
        }

        // Sort 리스트 팝업 메뉴 생성
        folder_btn_sort_type.setOnClickListener { v: View? ->
            val popup = PopupMenu(mContextThis, v)
            menuInflater.inflate(R.menu.menu_folder_sort, popup.menu)
            popup.setOnMenuItemClickListener { item: MenuItem? ->
                if (item != null) {
                    val id = item.itemId
                    when (id) {
                        R.id.menu_folder_sort_by_name -> {
                            sortFolders(SortType.Name)
                            folder_btn_sort_type.setText(R.string.menu_sort_by_name)
                        }
                        R.id.menu_folder_sort_by_time -> {
                            sortFolders(SortType.Time)
                            folder_btn_sort_type.setText(R.string.menu_sort_by_time)
                        }
                        R.id.menu_folder_sort_by_files -> {
                            sortFolders(SortType.Files)
                            folder_btn_sort_type.setText(R.string.menu_sort_by_files)
                        }
                    }
                }
                return@setOnMenuItemClickListener false
            }
            popup.show()
        }

        // 정렬 순서(오름차, 내림차) 버튼 연결
        mIsCurSortASC = mSharedPref.getBoolean(PREF_FOLDER_SORT_IS_ASC, true)
        if (mIsCurSortASC) {
            folder_btn_sort.setImageResource(R.drawable.baseline_up_black_24)
        } else {
            folder_btn_sort.setImageResource(R.drawable.baseline_down_black_24)
        }
        folder_btn_sort.setOnClickListener { _: View? ->
            if (mIsCurSortASC) {
                mIsCurSortASC = false
                folder_btn_sort.setImageResource(R.drawable.baseline_down_black_24)
                sortFolders(mCurSortType)
            } else {
                mIsCurSortASC = true
                folder_btn_sort.setImageResource(R.drawable.baseline_up_black_24)
                sortFolders(mCurSortType)
            }
        }

        // 폴더 리스트 갱신
        mFolderHandler.sendEmptyMessage(HANDLER_CREATE_LIST)
    }

    override fun onPause() {
        super.onPause()
        mPasswordFlag = true
    }

    override fun onResume() {
        super.onResume()

        if (mPasswordFlag) {
            if (::mSharedPref.isInitialized) {
                if (mSharedPref.getBoolean(PREF_SET_PASSWORD, false)) {
                    val intent = CreateIntent.createIntent(applicationContext, PasswordActivity::class.java)
                    intent.putExtra(EXTRA_PASSWORD_INTENT_TYPE, PasswordIntentType.MainExecute.value)
                    startActivityForResult(intent, REQUEST_CODE_PASSWORD_FLAG)
                }
            }
        }
        mPasswordFlag = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                when (requestCode) {
                    REQUEST_CODE_PASSWORD_FLAG -> {
                        mPasswordFlag = false
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val prefEditor: SharedPreferences.Editor = mSharedPref.edit()
        prefEditor.putBoolean(PREF_FOLDER_SORT_IS_ASC, mIsCurSortASC)
        prefEditor.putInt(PREF_FOLDER_SORT_TYPE, mCurSortType.value)
        prefEditor.putBoolean(PREF_FOLDER_VIEW_STYLE, mIsCurViewStyleList)
        prefEditor.apply()

        mCurFolderList.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_folder, menu)
        mCurViewStyleMenu = menu!!.findItem(R.id.menu_folder_view_type)
        if (mIsCurViewStyleList) {
            mCurViewStyleMenu.setIcon(R.drawable.baseline_view_module_black_24)
        } else {
            mCurViewStyleMenu.setIcon(R.drawable.baseline_view_list_black_24)
        }
        return true
    }

    /**
     * 액션바의 메뉴 아이템 선택시 처리하는 함수
     *  @param item 선택 된 메뉴 아이템
     *      R.id.menu_folder_new: 새로운 폴더 생성
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int? = item?.itemId
        when (id) {
            R.id.menu_folder_new -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                val layout: View = LayoutInflater.from(mContextThis).inflate(R.layout.dialog_folder_new, null)
                val inputEditText: EditText = layout.dialog_folder_input
                val warningTextView: TextView = layout.dialog_folder_warning
                warningTextView.visibility = View.GONE
                builder.setTitle(R.string.folder_dialog_new_title)
                builder.setView(layout)
                builder.setPositiveButton(R.string.dialog_ok, null)
                builder.setNegativeButton(R.string.dialog_cancel, null)

                val dialog: AlertDialog = builder.create()
                if (dialog.window != null) {
                    dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                }
                dialog.setOnShowListener { _: DialogInterface? ->
                    val positiveBtn: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveBtn.setOnClickListener { _: View? ->
                        var closeEnable = false
                        val folderName: String = inputEditText.text.toString()
                        if (folderName == "") {
                            warningTextView.visibility = View.VISIBLE
                            warningTextView.setText(R.string.folder_dialog_new_warning2)
                        } else {
                            val isValidName: Boolean = Pattern.matches(FOLDER_NAME_REGEX, folderName)
                            if (isValidName) {
                                val file = File(APP_INTERNAL_PATH + File.separator + folderName)
                                if (file.exists()) {
                                    warningTextView.visibility = View.VISIBLE
                                    warningTextView.setText(R.string.folder_dialog_new_warning3)
                                } else {
                                    if (file.mkdir()) {
                                        Snackbar.make(folder_layout, R.string.folder_dialog_new_success, Snackbar.LENGTH_SHORT).show()
                                        mFolderHandler.sendEmptyMessage(HANDLER_REFRESH)
                                    } else {
                                        Snackbar.make(folder_layout, R.string.folder_dialog_new_fail, Snackbar.LENGTH_SHORT).show()
                                    }
                                    closeEnable = true
                                }
                            } else {
                                warningTextView.visibility = View.VISIBLE
                                warningTextView.setText(R.string.folder_dialog_new_warning1)
                            }
                        }

                        if (closeEnable) {
                            dialog.dismiss()
                        }
                    }
                }
                dialog.show()
            }
            R.id.menu_folder_view_type -> {
                if(mIsCurViewStyleList) {
                    mIsCurViewStyleList = false
                    mCurViewStyleMenu.setIcon(R.drawable.baseline_view_list_black_24)
                } else {
                    mIsCurViewStyleList = true
                    mCurViewStyleMenu.setIcon(R.drawable.baseline_view_module_black_24)
                }
                mFolderHandler.sendEmptyMessage(HANDLER_CREATE_LIST)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sortFolders(type: SortType) {
        mCurSortType = type
        mFolderHandler.sendEmptyMessage(HANDLER_REFRESH)
    }

    /**
     * 폴더 새로고침 수행
     * 폴더의 ArrayList 만 갱신함
     *           true : 크게 보기
     *           false: 작게 보기
     */
    private fun refreshList() {
        if (mCurFolderList.isNotEmpty()) {
            mCurFolderList.clear()
        }
        val file = File(APP_INTERNAL_PATH)
        val files: Array<File>? = file.listFiles { pathname: File? ->
            if (pathname != null) {
                return@listFiles pathname.isDirectory &&
                        (pathname.name != APP_DEFAULT_FOLDER_NAME) &&
                        (pathname.name != APP_WIDGET_FOLDER_NAME)
            }
            return@listFiles false
        }

        if (files != null) {
            // 폴더들을 ArrayList 에 추가
            for (folder in files) {
                mCurFolderList.add(FolderObject(folder.path, mIsCurViewStyleList))
            }

            // 정렬하기
            when (mCurSortType) {
                SortType.Name -> {
                    if (mCurFolderList.isNotEmpty()) {
                        if (mIsCurSortASC) {
                            mCurFolderList.sortBy { obj: FolderObject -> obj.fileName }
                        } else {
                            mCurFolderList.sortByDescending { obj: FolderObject -> obj.fileName }
                        }
                    }
                }
                SortType.Time -> {
                    if (mCurFolderList.isNotEmpty()) {
                        if (mIsCurSortASC) {
                            mCurFolderList.sortBy { obj: FolderObject -> obj.folderLastModified }
                        } else {
                            mCurFolderList.sortByDescending { obj: FolderObject -> obj.folderLastModified }
                        }
                    }
                }
                SortType.Files -> {
                    if (mCurFolderList.isNotEmpty()) {
                        if (mIsCurSortASC) {
                            mCurFolderList.sortBy { obj: FolderObject -> obj.fileCounts }
                        } else {
                            mCurFolderList.sortByDescending { obj: FolderObject -> obj.fileCounts }
                        }
                    }
                }
            }

            // 기본 폴더를 ArrayList 에 추가
            mCurFolderList.add(FolderObject(APP_INTERNAL_DEFAULT_FOLDER_PATH, mIsCurViewStyleList))
            // 위젯 폴더를 ArrayList 에 추가
            mCurFolderList.add(FolderObject(APP_INTERNAL_WIDGET_FOLDER_PATH, mIsCurViewStyleList))

            // 위의 두 폴더를 따로 추가하는 이유는 두 폴더를 리스트의 맨 아래로 내리기 위함
        }
    }


    /**
     * 폴더 제거를 수행
     * position 의 위치에 있는 ArrayList의 폴더를 제거함
     * @param position 제거할 위치
     */
    private fun deleteFolder(position: Int) {
        val type: FolderObject.FolderType = mCurFolderList[position].folderType

        when (type) {
            FolderObject.FolderType.Primitive, FolderObject.FolderType.Widget -> {
                Snackbar.make(folder_layout, R.string.folder_delete_warning1, Snackbar.LENGTH_SHORT).show()
            }
            FolderObject.FolderType.Normal -> {
                val file = File(mCurFolderList[position].path)
                val files = file.listFiles()
                if (files != null) {
                    for (inFile in files) {
                        if (!inFile.delete()) {
                            Snackbar.make(
                                folder_layout,
                                R.string.folder_delete_fail,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                    if (file.delete()) {
                        Snackbar.make(
                            folder_layout,
                            R.string.folder_delete_success,
                            Snackbar.LENGTH_SHORT
                        ).show()
                        mFolderHandler.sendEmptyMessage(HANDLER_REFRESH)
                    } else {
                        Snackbar.make(
                            folder_layout,
                            R.string.folder_delete_fail,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    /**
     * FolderHandler.class 에서 사용할 handleMessage 함수
     * @param message 메시지
     *          HANDLER_CREATE_LIST: 처음 실행시 리스트 생성 작업
     *          HANDLER_REFRESH    : 리스트에서 데이터로 작용하는 ArrayList 만 갱신하는 작업
     */
    private fun handleMessage(message: Message) {
        val what: Int = message.what
        when (what) {
            HANDLER_CREATE_LIST -> {
                // 폴더 리스트 갱신
                refreshList()

                // 폴더 어댑터 연결
                mFolderAdapter = FolderAdapter(mContextThis, mCurFolderList, mIsCurViewStyleList, object :
                    ClickAction {
                    override fun onClick(view: View, position: Int) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_MEMO_OPEN_FOLDER_URL, mCurFolderList[position].path)
                        setResult(RESULT_OK, intent)
                        finish()
                    }

                    override fun onLongClick(view: View, position: Int) {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this@FolderActivity)
                        val layout: View = LayoutInflater.from(mContextThis).inflate(R.layout.dialog_folder_delete, null)
                        builder.setTitle(R.string.folder_dialog_delete_title)
                        builder.setView(layout)
                        builder.setPositiveButton(R.string.dialog_ok) { dialog: DialogInterface?, _: Int ->
                            deleteFolder(position)
                            dialog?.dismiss()
                        }
                        builder.setNegativeButton(R.string.dialog_cancel, null)
                        builder.show()
                    }
                })

                // 레이아웃 매니저 연결
                val layoutManager: StaggeredGridLayoutManager = when (mIsCurViewStyleList) {
                    true -> StaggeredGridLayoutManager(1, 1)
                    false -> StaggeredGridLayoutManager(2, 1)
                }
                layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                layoutManager.invalidateSpanAssignments()

                folder_list.setHasFixedSize(true)
                folder_list.layoutManager = layoutManager
                folder_list.adapter = mFolderAdapter
                if (!mIsCurViewStyleList) {
                    if (folder_list.itemDecorationCount > 0) {
                        folder_list.removeItemDecoration(mItemDecoration)
                    }
                    folder_list.addItemDecoration(mItemDecoration)
                }
            }
            HANDLER_REFRESH -> {
                refreshList()

                mFolderAdapter.notifyDataSetChanged()
                LogBot.logName("Test").log("Refresh")
            }
        }
    }

    /**
     * 폴더 액티비티에서 UI 작업을 수행하는 핸들러 클래스
     */
    private class FolderHandler(activity: FolderActivity) : Handler() {
        private val mActivity: WeakReference<FolderActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity:FolderActivity? = mActivity.get()
            activity?.handleMessage(msg)
        }
    }
}
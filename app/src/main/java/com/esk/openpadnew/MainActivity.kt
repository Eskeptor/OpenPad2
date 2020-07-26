package com.esk.openpadnew

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.esk.openpadnew.Adapter.ClickAction
import com.esk.openpadnew.Adapter.MainFileAdapter
import com.esk.openpadnew.DataType.MainFileObject
import com.esk.openpadnew.TouchHelper.MainFileItemTouchHelper
import com.esk.openpadnew.TouchHelper.RecyclerItemTouchHelperListener
import com.esk.openpadnew.Util.CreateIntent
import com.esk.openpadnew.Util.LogBot
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

private const val REQUEST_CODE_FIRST_BOOT = 1
private const val REQUEST_CODE_IS_MEMO_CHANGED = 2
private const val REQUEST_CODE_IS_MEMO_CREATED = 3
private const val REQUEST_CODE_GET_FOLDER = 4
private const val REQUEST_CODE_SETTINGS = 5

private const val WAIT_FOR_SECOND: Long = 1000L

private const val HANDLER_CREATE_LIST = 100
private const val HANDLER_REFRESH = 101

class MainActivity : AppCompatActivity(), RecyclerItemTouchHelperListener {
    // 오브젝트 필드 모음
    private val mToolbar = findViewById<Toolbar>(R.id.toolbar)
    private val mLayoutSort = findViewById<LinearLayout>(R.id.main_layout_sort)

    private lateinit var mContextThis: Context
    private lateinit var mMainHandler: MainHandler
    private lateinit var mCurFileAdapter: MainFileAdapter
    private lateinit var mSharedPref: SharedPreferences
    private lateinit var mCurSortType: SortType
    private lateinit var mCurViewStyleMenu: MenuItem
    private lateinit var mItemDecoration: RecyclerView.ItemDecoration

    private var mBackPressedTime: Long = 0L
    private var mCurFolderPath: String? = ""
    private val mCurFolderFileList: ArrayList<MainFileObject> = ArrayList()
    private var mIsCurViewStyleList: Boolean = false
    private var mIsCurSortASC: Boolean = true
    private var mDeleteFile: ArrayList<File> = ArrayList()
    private var mDeleteFileSMY: ArrayList<File> = ArrayList()
    private var mPrevViewImage: Boolean = true
    private var mLayoutTypeList: StaggeredGridLayoutManager = StaggeredGridLayoutManager(1, 1)
    private var mLayoutTypeGrid: StaggeredGridLayoutManager = StaggeredGridLayoutManager(2, 1)
    private var mPasswordFlag: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 툴바 생성 밑 폴더 버튼 생성
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportActionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.baseline_folder_black_24, null))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                mLayoutSort.setBackgroundColor(resources.getColor(R.color.white, theme))
            }
        } else {
            supportActionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.baseline_folder_black_24))
        }
        // 상단 그림자 제거
        supportActionBar?.elevation = 0.0f

        mContextThis = applicationContext
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        mCurViewStyleMenu = menu.findItem(R.id.menu_main_change_list)
        if (mIsCurViewStyleList) {
            mCurViewStyleMenu.setIcon(R.drawable.baseline_view_module_black_24)
        } else {
            mCurViewStyleMenu.setIcon(R.drawable.baseline_view_list_black_24)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        when (id) {
            // 리스트 타입 변경 버튼
            R.id.menu_main_change_list -> {
                if (mIsCurViewStyleList) {
                    mIsCurViewStyleList = false
                    mCurViewStyleMenu.setIcon(R.drawable.baseline_view_list_black_24)

                } else {
                    mIsCurViewStyleList = true
                    mCurViewStyleMenu.setIcon(R.drawable.baseline_view_module_black_24)
                }
                mMainHandler.sendEmptyMessage(HANDLER_CREATE_LIST)
            }
            // 설정 버튼
            R.id.menu_main_settings -> {
                val intent: Intent = CreateIntent.createIntent(mContextThis, SettingsActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_SETTINGS)
            }
            // 폴더 버튼
            android.R.id.home -> {
                val intent: Intent = CreateIntent.createIntent(mContextThis, FolderActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_GET_FOLDER)
            }
        }
        return true
    }

    override fun onBackPressed() {
        val tempTime: Long = System.currentTimeMillis()
        val intervalTime: Long = tempTime - mBackPressedTime

        if (main_memo_menu.isExpanded) {
            main_memo_menu.collapse()
        } else {
            if (intervalTime in 0..WAIT_FOR_SECOND) {
                super.onBackPressed()
            } else {
                mBackPressedTime = tempTime
                Snackbar.make(main_layout, R.string.back_pressed, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LogBot.logName("Debugbug").logLevel(LogBot.Level.Error).log("onResume")

        if (mPasswordFlag) {
            if (::mSharedPref.isInitialized) {
                if (mSharedPref.getBoolean(PREF_SET_PASSWORD, false)) {
                    val intent = CreateIntent.createIntent(mContextThis, PasswordActivity::class.java)
                    intent.putExtra(EXTRA_PASSWORD_INTENT_TYPE, PasswordIntentType.MainExecute.value)
                    startActivityForResult(intent, REQUEST_CODE_PASSWORD_FLAG)
                }
            }
        }
        mPasswordFlag = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogBot.logName("Debugbug").logLevel(LogBot.Level.Error).log("onActivityResult")
        mPasswordFlag = false
        when (resultCode) {
            RESULT_OK   -> {
                when (requestCode) {
                    REQUEST_CODE_FIRST_BOOT  ->  {
                        val restart: AlertDialog.Builder = AlertDialog.Builder(this)
                        restart.setTitle(R.string.main_dialog_restart_title)
                        restart.setMessage(R.string.main_dialog_restart_context)
                        restart.setPositiveButton(R.string.dialog_ok) { dialog: DialogInterface?, _: Int ->
                            val startActivity: Intent = CreateIntent.createIntent(baseContext, MainActivity::class.java)
                            val pendingIntentID = 123456
                            val pendingIntent: PendingIntent = PendingIntent.getActivity(baseContext, pendingIntentID, startActivity, PendingIntent.FLAG_CANCEL_CURRENT)
                            val mgr: AlarmManager? = baseContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
                            mgr?.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
                            System.exit(0)
                            dialog?.dismiss()
                        }
                        restart.show()
                        //mMainHandler.sendEmptyMessage(HANDLER_CREATE_LIST)
                    }
                    REQUEST_CODE_IS_MEMO_CHANGED, REQUEST_CODE_IS_MEMO_CREATED -> {
                        mMainHandler.sendEmptyMessage(HANDLER_REFRESH)
                    }
                    REQUEST_CODE_GET_FOLDER -> {
                        if (data != null) {
                            mCurFolderPath = data.getStringExtra(EXTRA_MEMO_OPEN_FOLDER_URL)
                            mMainHandler.sendEmptyMessage(HANDLER_REFRESH)
                        }
                    }
                    REQUEST_CODE_SETTINGS -> {
                        val curViewImage: Boolean = mSharedPref.getBoolean(PREF_VIEW_IMAGE, true)
                        if (mPrevViewImage != curViewImage) {
                            mPrevViewImage = curViewImage
                            mMainHandler.sendEmptyMessage(HANDLER_REFRESH)
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        destroyBackup()
    }

    private fun destroyBackup() {
        if (mDeleteFile.isNotEmpty()) {
            for (file in mDeleteFile) {
                if (file.exists()) {
                    if (file.delete()) {
                        LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("메모 삭제 완료")
                    } else {
                        LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("메모 삭제 실패")
                    }
                }
            }
        }
        if (mDeleteFileSMY.isNotEmpty()) {
            for (file in mDeleteFileSMY) {
                if (file.exists()) {
                    if (file.delete()) {
                        LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("SMY 삭제 완료")
                    } else {
                        LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("SMY 삭제 실패")
                    }
                }
            }
        }

        if (::mSharedPref.isInitialized) {
            val prefEditor: SharedPreferences.Editor = mSharedPref.edit()
            prefEditor.putBoolean(PREF_MAIN_VIEW_STYLE, mIsCurViewStyleList)
            prefEditor.putInt(PREF_MAIN_SORT_TYPE, mCurSortType.value)
            prefEditor.putBoolean(PREF_MAIN_SORT_IS_ASC, mIsCurSortASC)
            prefEditor.apply()
        }
    }

    private fun sortFiles(type: SortType) {
        mCurSortType = type
        mMainHandler.sendEmptyMessage(HANDLER_REFRESH)
    }

    private fun refreshList() {
        if (mCurFolderFileList.isNotEmpty()) {
            mCurFolderFileList.clear()
        }
        val file = File(mCurFolderPath)
        val files: Array<File> = file.listFiles { pathname: File? ->
            if (pathname != null) {
                return@listFiles pathname.name.endsWith(FILE_EXTENSION_TEXT) ||
                        pathname.name.endsWith(FILE_EXTENSION_IMAGE)
            }
            return@listFiles false
        }

        if (files.isNotEmpty()) {
            for (newFile in files) {
                mCurFolderFileList.add(MainFileObject(newFile, Locale.getDefault().displayCountry, resources.getString(R.string.item_main_image_title)))
            }

            // 정렬 하기
            when (mCurSortType) {
                SortType.Name -> {
                    if (mIsCurSortASC) {
                        mCurFolderFileList.sortBy { obj: MainFileObject -> obj.fileTitle }
                    } else {
                        mCurFolderFileList.sortByDescending { obj: MainFileObject -> obj.fileTitle }
                    }
                }
                // Time = else
                else -> {
                    if (mIsCurSortASC) {
                        mCurFolderFileList.sortBy { obj: MainFileObject -> obj.modifiedDate }
                    } else {
                        mCurFolderFileList.sortByDescending { obj: MainFileObject -> obj.modifiedDate }
                    }
                }
            }

            // 메모가 있을 때 Empty 문구 안보이기
            main_txt_empty.visibility = View.GONE
        } else {
            // 메모가 없을 때 Empty 문구 보이기
            main_txt_empty.visibility = View.VISIBLE
        }
    }

    private fun defaultFolderCheck() {
        val appFolder = File(APP_INTERNAL_PATH)
        if (!appFolder.exists()) {
            if (appFolder.mkdir()) {
                LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("앱 폴더 생성")
            } else {
                LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("앱 폴더 생성 실패")
            }
        } else {
            LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("기본 폴더 존재")
        }

        val defaultFolder = File(APP_INTERNAL_DEFAULT_FOLDER_PATH)
        if (!defaultFolder.exists()) {
            if (defaultFolder.mkdir()) {
                LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("기본 폴더 생성")
                mCurFolderPath = APP_INTERNAL_DEFAULT_FOLDER_PATH
            } else {
                LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("기본 폴더 생성 실패")
            }
        } else {
            LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("기본 폴더 존재")
            mCurFolderPath = APP_INTERNAL_DEFAULT_FOLDER_PATH
        }

        val widgetFolder = File(APP_INTERNAL_WIDGET_FOLDER_PATH)
        if (!widgetFolder.exists()) {
            if (widgetFolder.mkdir()) {
                LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("위젯 폴더 생성")
            } else {
                LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("위젯 폴더 생성 실패")
            }
        } else {
            LogBot.logName("defaultFolderCheck").logLevel(LogBot.Level.Debug).log("위젯 폴더 존재")
        }
    }

    private fun firstBootCheck() {
        val sharedPref: SharedPreferences = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)
        if (sharedPref.getBoolean(PREF_FIRST_BOOT, true)) {
            val intent: Intent = CreateIntent.createIntent(mContextThis, TutorialActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_FIRST_BOOT)
        } else {
            mMainHandler.sendEmptyMessage(HANDLER_CREATE_LIST)
        }
    }

    private fun deleteFile(position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.folder_dialog_delete_title)
        builder.setMessage(R.string.main_dialog_delete)
        builder.setPositiveButton(R.string.dialog_ok) {dialog: DialogInterface?, _: Int ->
            when (mCurFolderFileList[position].fileType) {
                MainFileObject.FileType.Text -> {
                    mDeleteFile.add(File(mCurFolderFileList[position].filePath))
                    mCurFolderFileList.removeAt(position)
                    mCurFileAdapter.notifyDataSetChanged()
                    Snackbar.make(main_layout, R.string.main_delete_success, Snackbar.LENGTH_LONG)
                        .setAction(R.string.main_delete_restore) { _: View? ->
                            mDeleteFile.clear()
                            mMainHandler.sendEmptyMessage(HANDLER_REFRESH)
                        }
                        .show()
                }
                MainFileObject.FileType.Image -> {
                    mDeleteFile.add(File(mCurFolderFileList[position].filePath))
                    val idx = mCurFolderFileList[position].filePath.lastIndexOf(".")
                    val smyPath = mCurFolderFileList[position].filePath.substring(0, idx) + FILE_EXTENSION_IMAGE_SUMMARY
                    mDeleteFileSMY.add(File(smyPath))
                    mCurFolderFileList.removeAt(position)
                    mCurFileAdapter.notifyDataSetChanged()
                    Snackbar.make(main_layout, R.string.main_delete_success, Snackbar.LENGTH_LONG)
                        .setAction(R.string.main_delete_restore) { _: View? ->
                            mDeleteFile.clear()
                            mDeleteFileSMY.clear()
                            mMainHandler.sendEmptyMessage(HANDLER_REFRESH)
                        }
                        .show()
                }
            }
            dialog?.dismiss()
        }
        builder.setNegativeButton(R.string.dialog_cancel, null)
        builder.show()
    }


    private fun handleMessage(message: Message) {
        val what: Int = message.what
        when (what) {
            HANDLER_CREATE_LIST -> {
                // 기본 폴더 체크
                defaultFolderCheck()

                // 리스트 새로고침
                refreshList()

                // 어댑터 생성
                mCurFileAdapter = MainFileAdapter(mCurFolderFileList, mSharedPref, this, mIsCurViewStyleList, object : ClickAction {
                    override fun onClick(view: View, position: Int) {
                        val intent: Intent =  if (mCurFolderFileList[position].fileType == MainFileObject.FileType.Text) {
                            CreateIntent.createIntent(mContextThis, TextMemoActivity::class.java)
                        } else {
                            CreateIntent.createIntent(mContextThis, PaintMemoActivity::class.java)
                        }
                        intent.putExtra(EXTRA_MEMO_OPEN_FOLDER_URL, mCurFolderPath)
                        intent.putExtra(EXTRA_MEMO_OPEN_FILE_URL, mCurFolderFileList[position].filePath)
                        startActivityForResult(intent, REQUEST_CODE_IS_MEMO_CHANGED)
                    }

                    override fun onLongClick(view: View, position: Int) {
                        deleteFile(position)
                    }
                })

                // 터치핼퍼 연결
                val itemTouchHelperCallBack = MainFileItemTouchHelper(0, ItemTouchHelper.LEFT, this, mSharedPref)
                ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(main_list)

                // 레이아웃 매니저 연결(좌, 우 분리)
                val layoutManager = when (mIsCurViewStyleList) {
                    true -> mLayoutTypeList
                    false -> mLayoutTypeGrid
                }
                layoutManager.invalidateSpanAssignments()

                // 레이아웃과 매니저 연결
                main_list.setHasFixedSize(true)
                main_list.layoutManager = layoutManager
                main_list.adapter = mCurFileAdapter
                if (main_list.itemDecorationCount < 2) {
                    main_list.addItemDecoration(mItemDecoration)
                    LogBot.logName("LayoutManager").logLevel(LogBot.Level.Debug).log("add item deco")
                }
            }

            HANDLER_REFRESH -> {
                // 리스트 새로고침
                refreshList()
                if (::mCurFileAdapter.isInitialized) {
                    mCurFileAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        val deletedIndex: Int = viewHolder.adapterPosition
        deleteFile(deletedIndex)
        mCurFileAdapter.notifyDataSetChanged()
    }

    private class MainHandler(activity: MainActivity) : Handler() {
        private val mActivity: WeakReference<MainActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity: MainActivity? = mActivity.get()
            activity?.handleMessage(msg)
        }
    }


}
package com.esk.openpadnew

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.esk.openpadnew.TextManager.TextManager
import com.esk.openpadnew.Util.CreateIntent
import com.esk.openpadnew.Util.CreateMD5
import com.esk.openpadnew.Util.LogBot
import io.github.mthli.knife.KnifeText
import kotlinx.android.synthetic.main.activity_text_memo.*
import kotlinx.android.synthetic.main.text_link_dialog.view.*
import java.io.File
import java.lang.ref.WeakReference

private const val HANDLER_GET_MEMO: Int = 200
private const val INDEX_FILE_NAME: String = "index.idx"

class TextMemoActivity : AppCompatActivity() {
    enum class SaveStatus(val value: Int) {
        None(0), New(1), Overwrite(2)
    }
    private var mCurFilePath: String? = null                // 현재 파일의 경로
    private var mCurFolderPath: String? = null              // 현재 폴더의 경로
    private var mCurFileMD5: String? = null                 // 현재 파일의 MD5값 (파일의 내용이 변경되었는지 확인용)
    private lateinit var mSharedPref: SharedPreferences     // 어플의 SharedPreferences
    private var mPasswordFlag: Boolean = false              // 보안 플래그

    private var mIsWidget: Boolean = false                  // 위젯에서 호출되었는지 유무
    private var mWidgetID = 0                               // 위젯의 호출 ID
    private var mWidgetFileID = 0                           // 위젯과 연결된 파일의 ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_memo)

        // 뒤로가기 버튼 활성화
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportActionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.baseline_back_black_24, null))
        } else {
            supportActionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.baseline_back_black_24))
        }
        // 상단 그림자 제거
        supportActionBar?.elevation = 0.0f

        // Preference 연결
        mSharedPref = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)

        // 패스워드창 플래그 설정
        mPasswordFlag = false

        // 하단의 유틸리티 버튼 초기화
        initButton()

        // 위젯 관련 값
        mIsWidget = intent.getBooleanExtra(INTENT_EXTRA_MEMO_ISWIDGET, false)
        mWidgetID = intent.getIntExtra(INTENT_EXTRA_WIDGET_ID, 999)
        mWidgetFileID = intent.getIntExtra(INTENT_EXTRA_WIDGET_FILE_ID, 999)

        // 위젯으로부터 호출되었다면
        if (mIsWidget) {
            val tmpFile = File(APP_INTERNAL_WIDGET_FOLDER_PATH + File.separator + mWidgetFileID + FILE_EXTENSION_TEXT)

            // 이미 존재하는 파일이면 -> 위젯 메모 수정
            if (tmpFile.exists()) {
                setTitle(R.string.text_widget_title)
                mCurFilePath = tmpFile.path
                TextHandler(this).sendEmptyMessage(HANDLER_GET_MEMO)
            } else {    // 존재하지 않는 파일이면 -> 새로운 위젯 메모
                setTitle(R.string.widget_title_new)
            }
        } else {    // 위젯으로부터 호출되지 않고 메인 액티비티에서 호출되었다면
            // 메인 액티비티로부터 현재 열린 파일, 폴더의 경로를 받아옴
            // 새로운 파일을 생성했다면 mCurFilePath 는 null
            mCurFilePath = intent.getStringExtra(EXTRA_MEMO_OPEN_FILE_URL)
            mCurFolderPath = intent.getStringExtra(EXTRA_MEMO_OPEN_FOLDER_URL)
            if (mCurFilePath == null) {
                setTitle(R.string.text_title_new)
            } else {
                setTitle(R.string.text_title)
                TextHandler(this).sendEmptyMessage(HANDLER_GET_MEMO)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int? = item.itemId
        if (id != null) {
            when (id) {
                android.R.id.home -> onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val curTextMD5 = CreateMD5.create(text_field.toHtml())
        if (curTextMD5 == mCurFileMD5) {
            super.onBackPressed()
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.paint_dialog_save_title)
            builder.setMessage(R.string.paint_dialog_save)
            val clickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    AlertDialog.BUTTON_POSITIVE -> {
                        if (mIsWidget) {    // 위젯에서 호출 되었다면 (위젯용 메모파일 저장)
                            if (mCurFilePath == null) {
                                mCurFilePath = APP_INTERNAL_WIDGET_FOLDER_PATH + File.separator + mWidgetFileID + FILE_EXTENSION_TEXT
                                var tmpFile = File(mCurFilePath!!)
                                while (tmpFile.exists()) {
                                    mWidgetFileID++
                                    mCurFilePath = APP_INTERNAL_WIDGET_FOLDER_PATH + File.separator + mWidgetFileID + FILE_EXTENSION_TEXT
                                    tmpFile = File(mCurFilePath!!)
                                }
                            }

                            if (TextManager.saveText(text_field.toHtml(), mCurFilePath!!)) {
                                LogBot.logName("TextMemoActivity - onBackPressed").logLevel(LogBot.Level.Error).log("위젯 내용 저장")
                            } else {
                                LogBot.logName("TextMemoActivity - onBackPressed").logLevel(LogBot.Level.Error).log("위젯 내용 저장 실패")
                            }

                            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                            MemoWidget.updateAppWidget(applicationContext, appWidgetManager, mWidgetID)
                            val resultValue = Intent()
                            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetID)
                            resultValue.putExtra(INTENT_EXTRA_WIDGET_FILE_ID, mWidgetFileID)
                            setResult(Activity.RESULT_OK, resultValue)

                        } else {            // 위젯에서 호출되지 않았다면 (일반 메모파일 저장)
                            saveMemo(text_field.toHtml())
                            setResult(RESULT_OK)
                        }
                        dialog.dismiss()
                        finish()
                    }
                    AlertDialog.BUTTON_NEGATIVE -> {
                        finish()
                    }
                }
                dialog.dismiss()
            }
            builder.setPositiveButton(R.string.dialog_save, clickListener)
            builder.setNeutralButton(R.string.dialog_cancel, clickListener)
            builder.setNegativeButton(R.string.dialog_no, clickListener)
            builder.show()
        }
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

    override fun onDestroy() {
        super.onDestroy()
        mCurFileMD5 = null
        mCurFilePath = null
        mCurFolderPath = null
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

    private fun saveMemo(memo: String) {
        if (memo == "") {
            return
        }

        if (mCurFilePath == null) {
            var memoIndex = checkMemoTitle()

            mCurFilePath = mCurFolderPath + File.separator + memoIndex + FILE_EXTENSION_TEXT

            if (mCurFilePath != null) {
                var saveFile = File(mCurFilePath!!)

                while (saveFile.exists()) {
                    memoIndex += 1
                    mCurFilePath = mCurFolderPath + File.separator + memoIndex + FILE_EXTENSION_TEXT
                    saveFile = File(mCurFilePath!!)
                }
            }
        }

        if (TextManager.saveText(memo, mCurFilePath!!)) {
            LogBot.logName("saveMemo").logLevel(LogBot.Level.Debug).log("메모 저장 완료")
        } else {
            LogBot.logName("saveMemo").logLevel(LogBot.Level.Debug).log("메모 저장 실패")
        }
    }

    private fun checkMemoTitle(): Int {
        var memoIndex = "0"
        val memoIndexFile = File(mCurFolderPath + File.separator + INDEX_FILE_NAME)

        if (!memoIndexFile.exists()) {
            if (memoIndexFile.createNewFile()) {
                if (TextManager.saveText(memoIndex, memoIndexFile.path)) {
                    LogBot.logName("checkMemoTitle").logLevel(LogBot.Level.Debug).log("인덱스 저장 완료")
                } else {
                    LogBot.logName("checkMemoTitle").logLevel(LogBot.Level.Debug).log("인덱스 저장 실패")
                }
            }
        } else {
            memoIndex = TextManager.openText(memoIndexFile.path)
        }

        return memoIndex.toInt()
    }

    private fun initButton() {
        setupClear()
        setupUndo()
        setupRedo()
        setupBold()
        setupItalic()
        setupUnderline()
        setupBullet()
        setupStrikethrough()
        setupQuote()
        setupLink()
    }

    private fun setupClear() {
        text_tools_clear.setOnClickListener { _: View? ->
            text_field.clearFormats()
        }
    }

    private fun setupUndo() {
        text_tools_undo.setOnClickListener { _: View? ->
            text_field.undo()
        }
    }

    private fun setupRedo() {
        text_tools_redo.setOnClickListener { _: View? ->
            text_field.redo()
        }
    }

    private fun setupBold() {
        text_tools_bold.setOnClickListener { _: View? ->
            text_field.bold(!text_field.contains(KnifeText.FORMAT_BOLD))
        }
    }

    private fun setupItalic() {
        text_tools_italic.setOnClickListener { _: View? ->
            text_field.italic(!text_field.contains(KnifeText.FORMAT_ITALIC))
        }
    }

    private fun setupUnderline() {
        text_tools_underline.setOnClickListener { _: View? ->
            text_field.underline(!text_field.contains(KnifeText.FORMAT_UNDERLINED))
        }
    }

    private fun setupBullet() {
        text_tools_bullet.setOnClickListener { _: View? ->
            text_field.bullet(!text_field.contains(KnifeText.FORMAT_BULLET))
        }
    }

    private fun setupStrikethrough() {
        text_tools_strikethrough.setOnClickListener { _: View? ->
            text_field.strikethrough(!text_field.contains(KnifeText.FORMAT_STRIKETHROUGH))
        }
    }

    private fun setupQuote() {
        text_tools_quote.setOnClickListener { _: View? ->
            text_field.quote(!text_field.contains(KnifeText.FORMAT_QUOTE))
        }
    }

    private fun setupLink() {
        text_tools_link.setOnClickListener { _: View? ->
            showLinkDialog()
        }
    }

    private fun showLinkDialog() {
        val start: Int = text_field.selectionStart
        val end: Int = text_field.selectionEnd

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(false)

        val view: View = layoutInflater.inflate(R.layout.text_link_dialog, null, false)
        builder.setView(view)
        builder.setTitle(R.string.text_link_dialog_title)
        builder.setPositiveButton(R.string.dialog_ok) { dialog: DialogInterface?, _: Int ->
            val link: String = view.link_url.text.toString().trim()
            if (TextUtils.isEmpty(link)) {
                dialog?.dismiss()
                return@setPositiveButton
            }

            text_field.link(link, start, end)
            dialog?.dismiss()
        }
        builder.setNegativeButton(R.string.dialog_cancel, null)
        builder.create().show()
    }

    private fun handleMessage(message: Message) {
        when (message.what) {
            HANDLER_GET_MEMO    -> {
                val memoData: String = TextManager.openText(mCurFilePath!!)
                text_field.fromHtml(memoData)
                mCurFileMD5 = CreateMD5.create(text_field.toHtml())
            }
        }
    }

    private class TextHandler(activity: TextMemoActivity) : Handler() {
        private val mActivity: WeakReference<TextMemoActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity: TextMemoActivity? = mActivity.get()
            activity?.handleMessage(msg)
        }
    }
}
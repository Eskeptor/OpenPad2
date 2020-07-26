package com.esk.openpadnew

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
    private var mCurFilePath:String? = null
    private var mCurFolderPath:String? = null
    private var mCurFileMD5: String? = null
    private lateinit var mSharedPref: SharedPreferences
    private var mPasswordFlag: Boolean = false

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

        // 메인 액티비티로부터 현재 열린 파일, 폴더의 경로를 받아옴
        // 새로운 파일을 생성했다면 mCurFilePath 는 null
        mCurFilePath = intent.getStringExtra(EXTRA_MEMO_OPEN_FILE_URL)
        mCurFolderPath = intent.getStringExtra(EXTRA_MEMO_OPEN_FOLDER_URL)

        if (mCurFilePath == null) {
            setTitle(R.string.text_title_new)
        } else {
            setTitle(R.string.text_title)
        }

        if (mCurFilePath != null) {
            TextHandler(this).sendEmptyMessage(HANDLER_GET_MEMO)
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
                        saveMemo(text_field.toHtml())
                        dialog.dismiss()
                        setResult(RESULT_OK)
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
        val what: Int = message.what
        when (what) {
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
package com.esk.openpadnew

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.esk.openpadnew.DataType.BrushObject
import com.esk.openpadnew.DataType.CircleObject
import com.esk.openpadnew.TextManager.TextManager
import com.esk.openpadnew.Util.CreateIntent
import com.esk.openpadnew.Util.FragmentType
import com.esk.openpadnew.Util.LogBot
import com.esk.openpadnew.Util.SimpleFragment
import com.flyco.tablayout.listener.CustomTabEntity
import com.flyco.tablayout.listener.OnTabSelectListener
import kotlinx.android.synthetic.main.activity_paint_memo.*
import kotlinx.android.synthetic.main.dialog_paint_description.view.*
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

private const val PAINT_COLOR_MAX: Int = 255
private const val PAINT_MINIMUM_LINE_LENGTH_PIXEL: Float = 0.0f

const val HANDLER_LOAD: Int = 100
const val HANDLER_CHANGE_COLOR: Int = 101
const val HANDLER_CHANGE_BRUSH_SIZE: Int = 102
const val HANDLER_CHANGE_BRUSH_TYPE: Int = 103
const val HANDLER_CHANGE_ERASER_SIZE: Int = 104

enum class PaintType {
    Brush, Eraser, Shape
}

class TabEntity(val title: String) : CustomTabEntity {
    override fun getTabUnselectedIcon() = 0

    override fun getTabSelectedIcon() = 0

    override fun getTabTitle() = title
}

class PaintMemoActivity : AppCompatActivity() {
    companion object {
        private lateinit var mShapeType: BrushObject.ShapeType
        private var mMenuItemUndo: MenuItem? = null
        private var mMenuItemReset: MenuItem? = null
    }

    lateinit var paintHandler: PaintHandler

    private val mToolsEntity: ArrayList<CustomTabEntity> = ArrayList()
    private val mToolsFragments: ArrayList<Fragment> = ArrayList()
    private var mIsToolUp: Boolean = false
    private var mOpenFolderPath: String? = null
    private var mOpenFilePath: String? = null
    private var mOpenFileSMYPath: String? = null
    private lateinit var mSharedPref: SharedPreferences
    private lateinit var mContextThis: Context
    private lateinit var mPaintClass: PaintClass
    private lateinit var mSaveFile: File
    private var mPasswordFlag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint_memo)
        mContextThis = applicationContext

        // 액티비티 이름 변경
        setTitle(R.string.text_title_new)

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

        // 핸들러 생성
        paintHandler = PaintHandler(this)

        // 초기 모양값은 None(일반 브러쉬)
        mShapeType = BrushObject.ShapeType.None

        // 페인트툴의 프래그먼트 생성 및 연결
        val brushFragment = SimpleFragment.getInstance(FragmentType.Brush, this)
        val eraserFragment = SimpleFragment.getInstance(FragmentType.Eraser, this)
        val shapeFragment = SimpleFragment.getInstance(FragmentType.Shape, this)
        mToolsFragments.add(brushFragment)
        mToolsFragments.add(eraserFragment)
        mToolsFragments.add(shapeFragment)
        val toolsTitle: Array<String> =
            arrayOf(getString(R.string.paint_tools_brush), getString(R.string.paint_tools_eraser), getString(R.string.paint_tools_shape))
        for (element in toolsTitle) {
            mToolsEntity.add(TabEntity(element))
        }
        paint_tab.setTabData(mToolsEntity, this, R.id.paint_tab_pager, mToolsFragments)
        paint_tab.currentTab = 0
        paint_tab.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabReselect(position: Int) {}

            override fun onTabSelect(position: Int) {
                mShapeType = when (position) {
                    0 -> {
                        mPaintClass.changePaintType(PaintType.Brush)
                        mPaintClass.setBrushColor(Color.rgb(brushFragment.mColorRed, brushFragment.mColorGreen, brushFragment.mColorBlue))
                        mPaintClass.setBrushSize(SimpleFragment.convertPixelsToDpFloat(brushFragment.mCurBrushSize.toFloat(), mContextThis))
                        BrushObject.ShapeType.None
                    }
                    1 -> {
                        mPaintClass.changePaintType(PaintType.Eraser)
                        mPaintClass.setBrushSize(SimpleFragment.convertPixelsToDpFloat(eraserFragment.mCurEraserSize.toFloat(), mContextThis))
                        BrushObject.ShapeType.None
                    }
                    else -> {
                        mPaintClass.changePaintType(PaintType.Shape)
                        mPaintClass.setBrushColor(Color.rgb(brushFragment.mColorRed, brushFragment.mColorGreen, brushFragment.mColorBlue))
                        mPaintClass.setBrushSize(SimpleFragment.convertPixelsToDpFloat(brushFragment.mCurBrushSize.toFloat(), mContextThis))
                        BrushObject.ShapeType.Circle
                    }
                }
            }
        })

        // 페인트툴 최소화
        paint_tab_pager.visibility = View.GONE

        // 페인트툴 인디케이터 연결
        paint_tools_indicator.setOnClickListener {
            if (mIsToolUp) {
                paint_tab_pager.visibility = View.GONE
                paint_tools_indicator.setImageResource(R.drawable.baseline_up_white_24)
                mIsToolUp = false
            } else {
                paint_tab_pager.visibility = View.VISIBLE
                paint_tools_indicator.setImageResource(R.drawable.baseline_down_white_24)
                mIsToolUp = true
            }
        }

        // 폴더의 경로와 파일 경로를 메인 액티비티로부터 받아옴
        // 이때 새로운 파일을 생성하는 경우에는 mOpenFilePath 는 공백이다.
        mOpenFolderPath = intent.getStringExtra(EXTRA_MEMO_OPEN_FOLDER_URL)
        mOpenFilePath = intent.getStringExtra(EXTRA_MEMO_OPEN_FILE_URL)
        if (mOpenFilePath != null) {
            val idx = mOpenFilePath!!.lastIndexOf(".")
            mOpenFileSMYPath = mOpenFilePath!!.substring(0, idx) + FILE_EXTENSION_IMAGE_SUMMARY
        }

        // 이미지 메모의 요약파일이 없을 경우 요약파일 생성
        if (mOpenFilePath != null) {
            val idx = mOpenFilePath!!.lastIndexOf(".")
            mOpenFileSMYPath = mOpenFilePath!!.substring(0, idx) + FILE_EXTENSION_IMAGE_SUMMARY
            if (mOpenFileSMYPath != null) {
                val imageSummary = File(mOpenFileSMYPath!!)
                if (!imageSummary.exists()) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.paint_dialog_description_title)
                    builder.setMessage(R.string.paint_dialog_description)
                    val clickListener: DialogInterface.OnClickListener =
                        DialogInterface.OnClickListener { dialog, which ->
                            if (which == AlertDialog.BUTTON_POSITIVE) {
                                createImageDescription()
                            }
                            dialog.dismiss()
                        }
                    builder.setPositiveButton(R.string.dialog_yes, clickListener)
                    builder.setNegativeButton(R.string.dialog_no, clickListener)
                    builder.show()
                }
            }
        }

        paintHandler.sendEmptyMessage(HANDLER_LOAD)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_paint, menu)
        mMenuItemUndo = menu?.findItem(R.id.menu_paint_undo)
        mMenuItemUndo?.isVisible = false
        mMenuItemReset = menu?.findItem(R.id.menu_paint_reset)
        mMenuItemReset?.isVisible = false

        if (mOpenFilePath == null) {
            menu?.findItem(R.id.menu_paint_description)?.isVisible = false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int? = item.itemId
        if (id != null) {
            when (id) {
                android.R.id.home -> {
                    onBackPressed()
                }
                R.id.menu_paint_undo -> {
                    mPaintClass.undoCanvas()
//                    mPaintClass.setBrushSize(SimpleFragment.convertPixelsToDpInt((mToolsFragments[0] as SimpleFragment).mCurBrushSize.toFloat(), this).toFloat())
                }
                R.id.menu_paint_description -> {
                    createImageDescription()
                }
                R.id.menu_paint_reset -> {
                    mPaintClass.resetPaint(mOpenFilePath != null, mOpenFilePath)
                    mMenuItemUndo?.isVisible = false
                    mMenuItemReset?.isVisible = false
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (mPaintClass.mIsModified) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.paint_dialog_save_title)
            builder.setMessage(R.string.paint_dialog_save)
            val clickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    AlertDialog.BUTTON_POSITIVE -> {
                        if (mOpenFilePath != null) {
                            if (mPaintClass.savePaint(mOpenFilePath!!)) {
                                LogBot.logName("onBackPressed").logLevel(LogBot.Level.Debug).log("이미지 메모 저장 완료")
                            } else {
                                LogBot.logName("onBackPressed").logLevel(LogBot.Level.Debug).log("이미지 메모 저장 불가")
                            }

                        } else {
                            var index = checkMemoTitle()
                            var path = mOpenFolderPath + File.separator + index + FILE_EXTENSION_IMAGE
                            var file = File(path)
                            while (file.exists()) {
                                index++
                                path = mOpenFolderPath + File.separator + index + FILE_EXTENSION_IMAGE
                                file = File(path)
                            }
                            if (mPaintClass.savePaint(path)) {
                                LogBot.logName("onBackPressed").logLevel(LogBot.Level.Debug).log("이미지 메모 저장 완료")
                            } else {
                                LogBot.logName("onBackPressed").logLevel(LogBot.Level.Debug).log("이미지 메모 저장 불가")
                            }
                        }
                        dialog.dismiss()
                        setResult(RESULT_OK)
                        finish()
                    }
                    AlertDialog.BUTTON_NEGATIVE -> {
                        if (mOpenFilePath == null) {
                            if (mOpenFileSMYPath != null) {
                                val file = File(mOpenFileSMYPath!!)
                                if (file.exists()) {
                                    if (file.delete()) {
                                        LogBot.logName("onBackPressed").logLevel(LogBot.Level.Debug).log("SMY 제거 완료")
                                    } else {
                                        LogBot.logName("onBackPressed").logLevel(LogBot.Level.Debug).log("SMY 제거 불가")
                                    }
                                }
                            }
                        }
                        dialog.dismiss()
                        setResult(RESULT_OK)
                        finish()
                    }
                    else -> {
                        dialog.dismiss()
                    }
                }
            }
            builder.setPositiveButton(R.string.dialog_save, clickListener)
            builder.setNeutralButton(R.string.dialog_cancel, clickListener)
            builder.setNegativeButton(R.string.dialog_no, clickListener)
            builder.show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenFilePath = null
        mOpenFileSMYPath = null
        mOpenFolderPath = null

        mPaintClass.destroyPaint()

        mMenuItemUndo = null
        mMenuItemReset = null

        mToolsEntity.clear()
        mToolsFragments.clear()
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
            RESULT_OK -> {
                when (requestCode) {
                    REQUEST_CODE_PASSWORD_FLAG -> {
                        mPasswordFlag = false
                    }
                }
            }
        }
    }

    fun handleMessage(message: Message) {
        when (message.what) {
            HANDLER_LOAD -> {
                // 페인트 클래스 생성
                mPaintClass = PaintClass(mContextThis)
                paint_draw.addView(mPaintClass)
                initPaint()
            }
            HANDLER_CHANGE_COLOR -> {
                val color = message.obj as Int
                mPaintClass.setBrushColor(color)
            }
            HANDLER_CHANGE_BRUSH_SIZE -> {
                val size = message.obj as Int
                mPaintClass.setBrushSize(size.toFloat())
            }
            HANDLER_CHANGE_BRUSH_TYPE -> {
                val type = message.obj as BrushObject.ShapeType
                mShapeType = type
            }
        }
    }

    private fun createImageDescription() {
        val imageSummary: File = when (mOpenFileSMYPath == null) {
            true -> {
                val idx = mOpenFilePath!!.lastIndexOf(".")
                mOpenFileSMYPath = mOpenFilePath!!.substring(0, idx) + FILE_EXTENSION_IMAGE_SUMMARY
                File(mOpenFileSMYPath!!)
            }
            false -> {
                File(mOpenFileSMYPath!!)
            }
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.paint_dialog_description_title)
        val view: View = LayoutInflater.from(mContextThis).inflate(R.layout.dialog_paint_description, null)
        val descriptionTxt: EditText = view.dialog_image_description_input

        if (imageSummary.exists()) {
            descriptionTxt.setText(TextManager.getLines(imageSummary, 1)!![0])
        }

        builder.setView(view)
        val clickListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                AlertDialog.BUTTON_POSITIVE -> {
                    TextManager.saveText(descriptionTxt.text.toString(), imageSummary.path)
                    setResult(RESULT_OK)
                }
            }
            dialog.dismiss()
        }
        builder.setPositiveButton(R.string.dialog_save, clickListener)
        builder.setNegativeButton(R.string.dialog_cancel, clickListener)

        val dialog: AlertDialog = builder.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }

    private fun initPaint() {
        if (mOpenFilePath == null) {
            setTitle(R.string.text_title_new)
            paint_draw.setBackgroundColor(Color.WHITE)
        } else {
            setTitle(R.string.text_title)
        }
        mPaintClass.resetPaint(mOpenFilePath != null, mOpenFilePath)
        mPaintClass.setBrushSize(SimpleFragment.convertPixelsToDpInt(10f, mContextThis).toFloat())
        mPaintClass.setBrushColor(Color.rgb(0, 0, 0))
    }

    private fun checkMemoTitle(): Int {
        val INDEX_FILE_NAME = "index.idx"
        var memoIndex = "0"
        val memoIndexFile = File(mOpenFolderPath + File.separator + INDEX_FILE_NAME)

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

    class PaintHandler(activity: PaintMemoActivity): Handler() {
        private val mActivity: WeakReference<PaintMemoActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity: PaintMemoActivity? = mActivity.get()
            activity?.handleMessage(msg)
        }
    }

    private class PaintClass(context: Context) : View(context) {
        private var mScreenWidth: Int = 0
        private var mScreenHeight: Int = 0
        private var mCurX: Float = 0.0f
        private var mCurY: Float = 0.0f
        private var mPrevX: Float = 0.0f
        private var mPrevY: Float = 0.0f
        private var mRadius: Float = 0.0f
        var mCurColor: Int = 0
            private set
        private var mCurBrushSize: Float = 0.0f
        private var mFilename:String = ""
        private var mFileOpened: Boolean = false
        var mIsModified: Boolean = false
            private set
        private var mIsCircleDrawing: Boolean = false
        private var mIsRectangleDrawing: Boolean = false
        private lateinit var mCanvas: Canvas
        private lateinit var mPath: Path
        private var mCanvasPaint: Paint? = null
        private var mBrushPaint: Paint? = null
        private var mBrushObject: BrushObject? = null
        private var mBitmap: Bitmap? = null
        private var mRect = Rect()


        init {
            val displayMetrics: DisplayMetrics = context.applicationContext.resources.displayMetrics
            mScreenWidth = displayMetrics.widthPixels
            mScreenHeight = displayMetrics.heightPixels
        }

        override fun onDraw(canvas: Canvas?) {
            if (mBitmap != null) {
                canvas?.drawBitmap(mBitmap!!, 0f, 0f, mCanvasPaint)
            }

            if (mIsCircleDrawing) {
                if (mBrushPaint != null) {
                    canvas?.drawCircle(
                        mPrevX + (mCurX - mPrevX) / 2,
                        mPrevY + (mCurY - mPrevY) / 2,
                        mRadius,
                        mBrushPaint!!
                    )
                }
            }

            if (mIsRectangleDrawing) {
                if (mBrushPaint != null) {
                    canvas?.drawRect(mRect, mBrushPaint!!)
                }
            }
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            destroyPaint()
            mBrushObject?.init()
        }

        /**
         * 페인트 제거
         */
        fun destroyPaint() {
            if (mBitmap != null) {
                mBitmap?.recycle()
                mBitmap = null
            }

            if (mBrushObject != null) {
                mBrushObject!!.init()
                mBrushObject = null
            }

            if (mCanvasPaint != null) {
                mCanvasPaint!!.reset()
                mCanvasPaint = null
            }

            if (mBrushPaint != null) {
                mBrushPaint!!.reset()
                mBrushPaint = null
            }

            mCanvas.setBitmap(null)
        }

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            mCurX = event!!.x
            mCurY = event.y

            val action = event.action

            when (mShapeType) {
                BrushObject.ShapeType.None -> {
                    when (action) {
                        MotionEvent.ACTION_MOVE -> {
                            if (abs(mCurX - mPrevX) >= PAINT_MINIMUM_LINE_LENGTH_PIXEL ||
                                abs(mCurY - mPrevY) >= PAINT_MINIMUM_LINE_LENGTH_PIXEL) {
                                mPath.quadTo(mPrevX, mPrevY, mCurX, mCurY)
                                mPrevX = mCurX
                                mPrevY = mCurY
                            }
                            if (mBrushPaint != null) {
                                mCanvas.drawPath(mPath, mBrushPaint!!)
                            }
                            invalidate()
                            return true
                        }
                        MotionEvent.ACTION_DOWN -> {
                            mPath.reset()
                            mPath.moveTo(mCurX, mCurY)
                            mPrevX = mCurX
                            mPrevY = mCurY
                            invalidate()
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            mBrushObject?.brushTypes?.add(BrushObject.ShapeType.None)
                            mBrushObject?.brushPaths?.add(Path(mPath))
                            mBrushObject?.brushSizes?.add(mBrushPaint!!.strokeWidth)
                            mBrushObject?.brushColors?.add(mCurColor)
                            mMenuItemUndo?.isVisible = true
                            mMenuItemReset?.isVisible = true
                            drawLine()
                            invalidate()
                            mIsModified = true
                            mPath.reset()
                            return false
                        }
                    }
                }
                BrushObject.ShapeType.Circle -> {
                    when (action) {
                        MotionEvent.ACTION_DOWN -> {
                            mPrevX = mCurX
                            mPrevY = mCurY
                            mRadius = 1f
                            mIsCircleDrawing = true
                            invalidate()
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            mRadius = (sqrt((mCurX - mPrevX).toDouble().pow(2.0) + (mCurY - mPrevY).toDouble()
                                .pow(2.0)
                            ) / 2).toFloat()
                            invalidate()
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            mBrushObject?.brushTypes?.add(BrushObject.ShapeType.Circle)
                            mBrushObject?.brushPaths?.add(CircleObject(mPrevX + (mCurX - mPrevX) / 2, mPrevY + (mCurY - mPrevY) / 2, mRadius))
                            mBrushObject?.brushSizes?.add(mBrushPaint!!.strokeWidth)
                            mBrushObject?.brushColors?.add(mCurColor)
                            mMenuItemUndo?.isVisible = true
                            mMenuItemReset?.isVisible = true
                            drawLine()
                            invalidate()
                            mIsModified = true
                            mIsCircleDrawing = false
                            return false
                        }
                    }
                }
                BrushObject.ShapeType.Rectangle -> {
                    when (action) {
                        MotionEvent.ACTION_DOWN -> {
                            mPrevX = mCurX
                            mPrevY = mCurY
                            mIsRectangleDrawing = true
                            invalidate()
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            mRect.set(mPrevX.toInt(), mPrevY.toInt(), mCurX.toInt(), mCurY.toInt())
                            invalidate()
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            mBrushObject?.brushTypes?.add(BrushObject.ShapeType.Rectangle)
                            mBrushObject?.brushPaths?.add(Rect(mPrevX.toInt(), mPrevY.toInt(), mCurX.toInt(), mCurY.toInt()))
                            mBrushObject?.brushSizes?.add(mBrushPaint!!.strokeWidth)
                            mBrushObject?.brushColors?.add(mCurColor)
                            mMenuItemUndo?.isVisible = true
                            mMenuItemReset?.isVisible = true
                            drawLine()
                            invalidate()
                            mIsModified = true
                            mIsRectangleDrawing = false
                            return false
                        }
                    }
                }
            }
            return false
        }

        /**
         * 초기화 수행
         */
        fun resetPaint(isFileOpened: Boolean?, path: String?) {
            if (isFileOpened != null && path != null) {
                mFileOpened = when (isFileOpened) {
                    true -> {
                        mFilename = path
                        true
                    }
                    false -> false
                }
                init()
            } else {
                init()
                setBrushColor(mCurColor)
            }
            invalidate()
            mIsModified = false
        }


        /**
         * 브러쉬의 색 설정
         * @param color 브러쉬 색
         */
        fun setBrushColor(color: Int) {
            mCurColor = color
            mBrushPaint?.color = mCurColor
        }


        /**
         * 브러쉬의 사이즈 설정
         * @param size 브러쉬 사이즈
         */
        fun setBrushSize(size: Float) {
            mCurBrushSize = size
            mBrushPaint?.strokeWidth = mCurBrushSize
        }


        /**
         * 현재 페인트의 타입(브러쉬, 지우개, 도형)을 변경
         * @param type 변경할 타입
         */
        fun changePaintType(type: PaintType) {
            when (type) {
                PaintType.Brush, PaintType.Shape -> {
                    mBrushPaint!!.color = mCurColor
                }
                PaintType.Eraser -> {
                    mCurColor = Color.WHITE
                    mBrushPaint!!.color = mCurColor
                }
            }
        }


        /**
         * 그리기의 Undo 기능
         */
        fun undoCanvas() {
            if (mBrushObject != null) {
                if (mBrushObject!!.brushPaths.isNotEmpty()) {
                    if (mFileOpened) {          // 현재 파일이 열려있다면
                        if (mBitmap != null) {
                            mBitmap?.recycle()  // 비트맵이 있다면 리사이클
                            mBitmap = null
                        }
                        // 비트맵을 불러온다.
                        mBitmap =
                            BitmapFactory.decodeFile(mFilename).copy(Bitmap.Config.ARGB_8888, true)
                    } else {                    // 현재 파일이 열려있지 않다면
                        if (mBitmap != null) {
                            mBitmap?.recycle()  // 비트맵이 있다면 리사이클
                            mBitmap = null
                        }
                        // 비트맵을 새로 생성한다.
                        mBitmap = Bitmap.createBitmap(
                            mScreenWidth,
                            mScreenHeight,
                            Bitmap.Config.ARGB_8888
                        )
                    }

                    // 캔버스 생성
                    if (mBitmap != null) {
                        mCanvas = Canvas(mBitmap!!)
                        if (!mFileOpened) {
                            mCanvas.drawARGB(
                                PAINT_COLOR_MAX,
                                PAINT_COLOR_MAX,
                                PAINT_COLOR_MAX,
                                PAINT_COLOR_MAX
                            )
                        }
                    }

                    mBrushObject!!.brushTypes.removeLast()
                    mBrushObject!!.brushPaths.removeLast()
                    mBrushObject!!.brushSizes.removeLast()
                    mBrushObject!!.brushColors.removeLast()
                    mPath.reset()
                    drawLine()
                }

                if (mBrushObject!!.brushPaths.isEmpty()) {
                    mMenuItemUndo?.isVisible = false
                    mMenuItemReset?.isVisible = false
                    mIsModified = false
                }
            }
            invalidate()
        }


        /**
         * 이미지 메모 세이브 기능
         * @param path 세이브 경로
         */
        fun savePaint(path: String): Boolean {
            var stream: FileOutputStream? = null
            this.draw(mCanvas)

            try {
                stream = FileOutputStream(File(path))
                mBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                return true
            } catch (e: Exception) {
                LogBot.logName("savePaint").log(e.toString())
            } finally {
                stream?.close()
            }
            return false
        }


        /**
         * 초기화 수행
         */
        private fun init() {
            if (mFileOpened) {          // 현재 파일이 열려있다면
                if (mBitmap != null) {
                    mBitmap?.recycle()  // 비트맵이 있다면 리사이클
                    mBitmap = null
                }
                // 비트맵을 불러온다.
                mBitmap = BitmapFactory.decodeFile(mFilename).copy(Bitmap.Config.ARGB_8888, true)
            } else {                    // 현재 파일이 열려있지 않다면
                if (mBitmap != null) {
                    mBitmap?.recycle()  // 비트맵이 있다면 리사이클
                    mBitmap = null
                }
                // 비트맵을 새로 생성한다.
                mBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888)
            }

            // 캔버스 생성
            if (mBitmap != null) {
                mCanvas = Canvas(mBitmap!!)
                if (!mFileOpened) {
                    mCanvas.drawARGB(
                        PAINT_COLOR_MAX,
                        PAINT_COLOR_MAX,
                        PAINT_COLOR_MAX,
                        PAINT_COLOR_MAX
                    )
                }
            }

            mPath = Path()

            // 브러쉬 오브젝트 생성
            if (mBrushObject != null) {
                mBrushObject?.init()
            } else {
                mBrushObject = BrushObject()
            }

            // 캔버스용 페인트 생성
            if (mCanvasPaint != null) {
                mCanvasPaint?.reset()
                mCanvasPaint = null
            }
            mCanvasPaint = Paint(Paint.DITHER_FLAG)
            mCanvasPaint?.isAntiAlias = true

            // 브러쉬용 페인트 생성
            if (mBrushPaint != null) {
                mBrushPaint?.reset()
                mBrushPaint = null
            }
            mBrushPaint = Paint(Paint.DITHER_FLAG)
            mBrushPaint?.alpha = PAINT_COLOR_MAX
            mBrushPaint?.strokeJoin = Paint.Join.ROUND
            mBrushPaint?.style = Paint.Style.STROKE
            mBrushPaint?.strokeCap = Paint.Cap.ROUND
            mBrushPaint?.isAntiAlias = true

            mRadius = 0.0f
            setBrushColor(Color.BLACK)
            setBrushSize(10f)
        }


        /**
         * 선을 그리는 함수(지우개 포함)
         */
        private fun drawLine() {
            //mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            if (mBrushPaint != null) {
                val itBrushSize: Iterator<Float> = mBrushObject!!.brushSizes.iterator()
                val itBrushPath: Iterator<Any> = mBrushObject!!.brushPaths.iterator()
                val itBrushColor: Iterator<Int> = mBrushObject!!.brushColors.iterator()
                val itBrushType: Iterator<BrushObject.ShapeType> = mBrushObject!!.brushTypes.iterator()

                while (itBrushSize.hasNext()) {
                    mBrushPaint!!.strokeWidth = itBrushSize.next()
                    mBrushPaint!!.color = itBrushColor.next()
                    when (itBrushType.next()) {
                        BrushObject.ShapeType.None -> {
                            mCanvas.drawPath(itBrushPath.next() as Path, mBrushPaint!!)
                        }
                        BrushObject.ShapeType.Circle -> {
                            val circle: CircleObject = itBrushPath.next() as CircleObject
                            mCanvas.drawCircle(circle.x, circle.y, circle.r, mBrushPaint!!)
                        }
                        BrushObject.ShapeType.Rectangle -> {
                            val rect: Rect = itBrushPath.next() as Rect
                            mCanvas.drawRect(rect, mBrushPaint!!)
                        }
                    }
                }

                mBrushPaint!!.strokeWidth = mCurBrushSize
                mBrushPaint!!.color = mCurColor
            }
        }
    }
}
package com.esk.openpadnew

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*


// TODO (위젯 만들기)
/**
 * The configuration screen for the [MemoWidget] AppWidget.
 */
class MemoWidgetConfigureActivity : Activity() {
    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var mSharedPref: SharedPreferences

    private var mTab1CurBackRed: Int = 0                        // Tab1 - Background color - Red
    private var mTab1CurBackRed_Backup: Int = 0                 // Tab1 - Background color(Backup) - Red
    private var mTab1CurBackGreen: Int = 0                      // Tab1 - Background color - Green
    private var mTab1CurBackGreen_Backup: Int = 0               // Tab1 - Background color(Backup) - Green
    private var mTab1CurBackBlue: Int = 0                       // Tab1 - Background color - Blue
    private var mTab1CurBackBlue_Backup: Int = 0                // Tab1 - Background color(Backup) - Blue

    private var mTab1CurFontRed: Int = 0                        // Tab1 - Font color - Red
    private var mTab1CurFontRed_Backup: Int = 0                 // Tab1 - Font color(Backup) - Red
    private var mTab1CurFontGreen: Int = 0                      // Tab1 - Font color - Green
    private var mTab1CurFontGreen_Backup: Int = 0               // Tab1 - Font color(Backup) - Green
    private var mTab1CurFontBlue: Int = 0                       // Tab1 - Font color - Blue
    private var mTab1CurFontBlue_Backup: Int = 0                // Tab1 - Font color(Backup) - Blue

    private var mTab2CurBackRed: Int = 0                        // Tab2 - Background color - Red
    private var mTab2CurBackRed_Backup: Int = 0                 // Tab2 - Background color(Backup) - Red
    private var mTab2CurBackGreen: Int = 0                      // Tab2 - Background color - Green
    private var mTab2CurBackGreen_Backup: Int = 0               // Tab2 - Background color(Backup) - Green
    private var mTab2CurBackBlue: Int = 0                       // Tab2 - Background color - Blue
    private var mTab2CurBackBlue_Backup: Int = 0                // Tab2 - Background color(Backup) - Blue

    private var mTab2CurFontRed: Int = 0                        // Tab2 - Font color - Red
    private var mTab2CurFontRed_Backup: Int = 0                 // Tab2 - Font color(Backup) - Red
    private var mTab2CurFontGreen: Int = 0                      // Tab2 - Font color - Green
    private var mTab2CurFontGreen_Backup: Int = 0               // Tab2 - Font color(Backup) - Green
    private var mTab2CurFontBlue: Int = 0                       // Tab2 - Font color - Blue
    private var mTab2CurFontBlue_Backup: Int = 0                // Tab2 - Font color(Backup) - Blue

    private var mSeekBarChangeListener: SeekBar.OnSeekBarChangeListener? = null

    private var mPreviewMainLayout: LinearLayout? = null
    private var mPreviewTitleLayout: FrameLayout? = null
    private var mPreviewContextLayout: FrameLayout? = null
    private var mPreviewTxtTitle: TextView? = null
    private var mPreviewTxtContext: TextView? = null

    private var mTab1BackTxtRed: TextView? = null
    private var mTab1BackTxtGreen: TextView? = null
    private var mTab1BackTxtBlue: TextView? = null
    private var mTab1FontTxtRed: TextView? = null
    private var mTab1FontTxtGreen: TextView? = null
    private var mTab1FontTxtBlue: TextView? = null
    private var mTab2BackTxtRed: TextView? = null
    private var mTab2BackTxtGreen: TextView? = null
    private var mTab2BackTxtBlue: TextView? = null
    private var mTab2FontTxtRed: TextView? = null
    private var mTab2FontTxtGreen: TextView? = null
    private var mTab2FontTxtBlue: TextView? = null

    private var mTab1BackSeekRed: SeekBar? = null
    private var mTab1BackSeekGreen: SeekBar? = null
    private var mTab1BackSeekBlue: SeekBar? = null
    private var mTab1FontSeekRed: SeekBar? = null
    private var mTab1FontSeekGreen: SeekBar? = null
    private var mTab1FontSeekBlue: SeekBar? = null
    private var mTab2BackSeekRed: SeekBar? = null
    private var mTab2BackSeekGreen: SeekBar? = null
    private var mTab2BackSeekBlue: SeekBar? = null
    private var mTab2FontSeekRed: SeekBar? = null
    private var mTab2FontSeekGreen: SeekBar? = null
    private var mTab2FontSeekBlue: SeekBar? = null

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // 위젯을 나가거나 뒤로가기를 눌렀을때 캔슬 이벤트 발생
        setResult(Activity.RESULT_CANCELED)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Preference 연결
        mSharedPref = getSharedPreferences(APP_WIDGET_PREFERENCE + mAppWidgetId, Context.MODE_PRIVATE)

        setContentView(R.layout.memo_widget_configure)


        // Tab 설정 ------------------------------------------------------------------------------------
        val tabHost: TabHost = findViewById(R.id.widget_config_tabhost)
        tabHost.setup()

        val tab1: TabHost.TabSpec = tabHost.newTabSpec("Tab1")
            .setContent(R.id.tab1)
            .setIndicator(getString(R.string.widget_config_customize_tap1_title))
        tabHost.addTab(tab1)
        val tab2: TabHost.TabSpec = tabHost.newTabSpec("Tab2")
            .setContent(R.id.tab2)
            .setIndicator(getString(R.string.widget_config_customize_tap2_title))
        tabHost.addTab(tab2)
        // --------------------------------------------------------------------------------------------


        // 변수 연결 ------------------------------------------------------------------------------------
        mTab1CurBackRed = mSharedPref.getInt(MemoWidget.WIDGET_TITLE_BACK_COLOR_RED, MemoWidget.WIDGET_TITLE_BACK_COLOR_RED_DEFAULT)
        mTab1CurBackRed_Backup = mTab1CurBackRed
        mTab1CurBackGreen = mSharedPref.getInt(MemoWidget.WIDGET_TITLE_BACK_COLOR_GREEN, MemoWidget.WIDGET_TITLE_BACK_COLOR_GREEN_DEFAULT)
        mTab1CurBackGreen_Backup = mTab1CurBackGreen
        mTab1CurBackBlue = mSharedPref.getInt(MemoWidget.WIDGET_TITLE_BACK_COLOR_BLUE, MemoWidget.WIDGET_TITLE_BACK_COLOR_BLUE_DEFAULT)
        mTab1CurBackBlue_Backup = mTab1CurBackBlue
        mTab1CurFontRed = mSharedPref.getInt(MemoWidget.WIDGET_TITLE_FONT_COLOR_RED, MemoWidget.WIDGET_TITLE_FONT_COLOR_RED_DEFAULT)
        mTab1CurFontRed_Backup = mTab1CurFontRed
        mTab1CurFontGreen = mSharedPref.getInt(MemoWidget.WIDGET_TITLE_FONT_COLOR_GREEN, MemoWidget.WIDGET_TITLE_FONT_COLOR_GREEN_DEFAULT)
        mTab1CurFontGreen_Backup = mTab1CurFontGreen
        mTab1CurFontBlue = mSharedPref.getInt(MemoWidget.WIDGET_TITLE_FONT_COLOR_BLUE, MemoWidget.WIDGET_TITLE_FONT_COLOR_BLUE_DEFAULT)
        mTab1CurFontBlue_Backup = mTab1CurFontBlue

        mTab2CurBackRed = mSharedPref.getInt(MemoWidget.WIDGET_CONTEXT_BACK_COLOR_RED, MemoWidget.WIDGET_CONTEXT_BACK_COLOR_RED_DEFAULT)
        mTab2CurBackRed_Backup = mTab2CurBackRed
        mTab2CurBackGreen = mSharedPref.getInt(MemoWidget.WIDGET_CONTEXT_BACK_COLOR_GREEN, MemoWidget.WIDGET_CONTEXT_BACK_COLOR_GREEN_DEFAULT)
        mTab2CurBackGreen_Backup = mTab2CurBackGreen
        mTab2CurBackBlue = mSharedPref.getInt(MemoWidget.WIDGET_CONTEXT_BACK_COLOR_BLUE, MemoWidget.WIDGET_CONTEXT_BACK_COLOR_BLUE_DEFAULT)
        mTab2CurBackBlue_Backup = mTab2CurBackBlue
        mTab2CurFontRed = mSharedPref.getInt(MemoWidget.WIDGET_CONTEXT_FONT_COLOR_RED, MemoWidget.WIDGET_CONTEXT_FONT_COLOR_RED_DEFAULT)
        mTab2CurFontRed_Backup = mTab2CurFontRed
        mTab2CurFontGreen = mSharedPref.getInt(MemoWidget.WIDGET_CONTEXT_FONT_COLOR_GREEN, MemoWidget.WIDGET_CONTEXT_FONT_COLOR_GREEN_DEFAULT)
        mTab2CurFontGreen_Backup = mTab2CurFontGreen
        mTab2CurFontBlue = mSharedPref.getInt(MemoWidget.WIDGET_CONTEXT_FONT_COLOR_BLUE, MemoWidget.WIDGET_CONTEXT_FONT_COLOR_BLUE_DEFAULT)
        mTab2CurFontBlue_Backup = mTab2CurFontBlue
        // --------------------------------------------------------------------------------------------


        // 컨트롤 연결 -----------------------------------------------------------------------------------
        mPreviewMainLayout = findViewById(R.id.widget_config_preview_mainlayout)
        mPreviewTitleLayout = findViewById(R.id.widget_config_preview_titlelayout)
        mPreviewContextLayout = findViewById(R.id.widget_config_preview_contextlayout)
        mPreviewTxtTitle = findViewById(R.id.widget_config_preview_memodate)
        mPreviewTxtContext = findViewById(R.id.widget_config_preview_memocontext)

        mTab1BackTxtRed = findViewById(R.id.widget_config_tab1_back_txtRed)
        mTab1BackTxtGreen = findViewById(R.id.widget_config_tab1_back_txtGreen)
        mTab1BackTxtBlue = findViewById(R.id.widget_config_tab1_back_txtBlue)
        mTab1FontTxtRed = findViewById(R.id.widget_config_tab1_font_txtRed)
        mTab1FontTxtGreen = findViewById(R.id.widget_config_tab1_font_txtGreen)
        mTab1FontTxtBlue = findViewById(R.id.widget_config_tab1_font_txtBlue)

        mTab2BackTxtRed = findViewById(R.id.widget_config_tab2_back_txtRed)
        mTab2BackTxtGreen = findViewById(R.id.widget_config_tab2_back_txtGreen)
        mTab2BackTxtBlue = findViewById(R.id.widget_config_tab2_back_txtBlue)
        mTab2FontTxtRed = findViewById(R.id.widget_config_tab2_font_txtRed)
        mTab2FontTxtGreen = findViewById(R.id.widget_config_tab2_font_txtGreen)
        mTab2FontTxtBlue = findViewById(R.id.widget_config_tab2_font_txtBlue)

        mTab1BackSeekRed = findViewById(R.id.widget_config_tab1_back_seekRed)
        mTab1BackSeekGreen = findViewById(R.id.widget_config_tab1_back_seekGreen)
        mTab1BackSeekBlue = findViewById(R.id.widget_config_tab1_back_seekBlue)
        mTab1FontSeekRed = findViewById(R.id.widget_config_tab1_font_seekRed)
        mTab1FontSeekGreen = findViewById(R.id.widget_config_tab1_font_seekGreen)
        mTab1FontSeekBlue = findViewById(R.id.widget_config_tab1_font_seekBlue)

        mTab2BackSeekRed = findViewById(R.id.widget_config_tab2_back_seekRed)
        mTab2BackSeekGreen = findViewById(R.id.widget_config_tab2_back_seekGreen)
        mTab2BackSeekBlue = findViewById(R.id.widget_config_tab2_back_seekBlue)
        mTab2FontSeekRed = findViewById(R.id.widget_config_tab2_font_seekRed)
        mTab2FontSeekGreen = findViewById(R.id.widget_config_tab2_font_seekGreen)
        mTab2FontSeekBlue = findViewById(R.id.widget_config_tab2_font_seekBlue)
        // --------------------------------------------------------------------------------------------


        // 배경색 설정 -----------------------------------------------------------------------------------
        mPreviewMainLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
        mPreviewTitleLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
        mPreviewContextLayout?.setBackgroundColor(Color.rgb(mTab2CurBackRed, mTab2CurBackGreen, mTab2CurBackBlue))
        mPreviewTxtTitle?.setTextColor(Color.rgb(mTab1CurFontRed, mTab1CurFontGreen, mTab1CurFontBlue))
        mPreviewTxtContext?.setTextColor(Color.rgb(mTab2CurFontRed, mTab2CurFontGreen, mTab2CurFontBlue))
        // --------------------------------------------------------------------------------------------


        // SeekBar ChangeListener 생성 -----------------------------------------------------------------
        mSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val id = seekBar.id
                when (id) {
                    R.id.widget_config_tab1_back_seekRed -> mTab1BackTxtRed?.text = String.format(resources.getString(R.string.paint_txtBrushRed), progress)
                    R.id.widget_config_tab1_back_seekGreen -> mTab1BackTxtGreen?.text = String.format(resources.getString(R.string.paint_txtBrushGreen), progress)
                    R.id.widget_config_tab1_back_seekBlue -> mTab1BackTxtBlue?.text = String.format(resources.getString(R.string.paint_txtBrushBlue), progress)
                    R.id.widget_config_tab1_font_seekRed -> mTab1FontTxtRed?.text = String.format(resources.getString(R.string.paint_txtBrushRed), progress)
                    R.id.widget_config_tab1_font_seekGreen -> mTab1FontTxtGreen?.text = String.format(resources.getString(R.string.paint_txtBrushGreen), progress)
                    R.id.widget_config_tab1_font_seekBlue -> mTab1FontTxtBlue?.text = String.format(resources.getString(R.string.paint_txtBrushBlue), progress)
                    R.id.widget_config_tab2_back_seekRed -> mTab2BackTxtRed?.text = String.format(resources.getString(R.string.paint_txtBrushRed), progress)
                    R.id.widget_config_tab2_back_seekGreen -> mTab2BackTxtGreen?.text = String.format(resources.getString(R.string.paint_txtBrushGreen), progress)
                    R.id.widget_config_tab2_back_seekBlue -> mTab2BackTxtBlue?.text = String.format(resources.getString(R.string.paint_txtBrushBlue), progress)
                    R.id.widget_config_tab2_font_seekRed -> mTab2FontTxtRed?.text = String.format(resources.getString(R.string.paint_txtBrushRed), progress)
                    R.id.widget_config_tab2_font_seekGreen -> mTab2FontTxtGreen?.text = String.format(resources.getString(R.string.paint_txtBrushGreen), progress)
                    R.id.widget_config_tab2_font_seekBlue -> mTab2FontTxtBlue?.text = String.format(resources.getString(R.string.paint_txtBrushBlue), progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val id = seekBar.id
                when (id) {
                    R.id.widget_config_tab1_back_seekRed -> {
                        mTab1CurBackRed = seekBar.progress
                        mPreviewMainLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                        mPreviewTitleLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                    }
                    R.id.widget_config_tab1_back_seekGreen -> {
                        mTab1CurBackGreen = seekBar.progress
                        mPreviewMainLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                        mPreviewTitleLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                    }
                    R.id.widget_config_tab1_back_seekBlue -> {
                        mTab1CurBackBlue = seekBar.progress
                        mPreviewMainLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                        mPreviewTitleLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                    }
                    R.id.widget_config_tab1_font_seekRed -> {
                        mTab1CurFontRed = seekBar.progress
                        mPreviewTxtTitle?.setTextColor(Color.rgb(mTab1CurFontRed, mTab1CurFontGreen, mTab1CurFontBlue))
                    }
                    R.id.widget_config_tab1_font_seekGreen -> {
                        mTab1CurFontGreen = seekBar.progress
                        mPreviewTxtTitle?.setTextColor(Color.rgb(mTab1CurFontRed, mTab1CurFontGreen, mTab1CurFontBlue))
                    }
                    R.id.widget_config_tab1_font_seekBlue -> {
                        mTab1CurFontBlue = seekBar.progress
                        mPreviewTxtTitle?.setTextColor(Color.rgb(mTab1CurFontRed, mTab1CurFontGreen, mTab1CurFontBlue))
                    }
                    R.id.widget_config_tab2_back_seekRed -> {
                        mTab2CurBackRed = seekBar.progress
                        mPreviewContextLayout?.setBackgroundColor(Color.rgb(mTab2CurBackRed, mTab2CurBackGreen, mTab2CurBackBlue))
                    }
                    R.id.widget_config_tab2_back_seekGreen -> {
                        mTab2CurBackGreen = seekBar.progress
                        mPreviewContextLayout?.setBackgroundColor(Color.rgb(mTab2CurBackRed, mTab2CurBackGreen, mTab2CurBackBlue))
                    }
                    R.id.widget_config_tab2_back_seekBlue -> {
                        mTab2CurBackBlue = seekBar.progress
                        mPreviewContextLayout?.setBackgroundColor(Color.rgb(mTab2CurBackRed, mTab2CurBackGreen, mTab2CurBackBlue))
                    }
                    R.id.widget_config_tab2_font_seekRed -> {
                        mTab2CurFontRed = seekBar.progress
                        mPreviewTxtContext?.setTextColor(Color.rgb(mTab2CurFontRed, mTab2CurFontGreen, mTab2CurFontBlue))
                    }
                    R.id.widget_config_tab2_font_seekGreen -> {
                        mTab2CurFontGreen = seekBar.progress
                        mPreviewTxtContext?.setTextColor(Color.rgb(mTab2CurFontRed, mTab2CurFontGreen, mTab2CurFontBlue))
                    }
                    R.id.widget_config_tab2_font_seekBlue -> {
                        mTab2CurFontBlue = seekBar.progress
                        mPreviewTxtContext?.setTextColor(Color.rgb(mTab2CurFontRed, mTab2CurFontGreen, mTab2CurFontBlue))
                    }
                }
            }
        }
        // --------------------------------------------------------------------------------------------


        // SeekBar 설정 --------------------------------------------------------------------------------
        mTab1BackSeekRed?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab1BackSeekGreen?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab1BackSeekBlue?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab1FontSeekRed?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab1FontSeekGreen?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab1FontSeekBlue?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab2BackSeekRed?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab2BackSeekGreen?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab2BackSeekBlue?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab2FontSeekRed?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab2FontSeekGreen?.setOnSeekBarChangeListener(mSeekBarChangeListener)
        mTab2FontSeekBlue?.setOnSeekBarChangeListener(mSeekBarChangeListener)

        mTab1BackSeekRed?.progress = mTab1CurBackRed
        mTab1BackSeekGreen?.progress = mTab1CurBackGreen
        mTab1BackSeekBlue?.progress = mTab1CurBackBlue
        mTab1FontSeekRed?.progress = mTab1CurFontRed
        mTab1FontSeekGreen?.progress = mTab1CurFontGreen
        mTab1FontSeekBlue?.progress = mTab1CurFontBlue
        mTab2BackSeekRed?.progress = mTab2CurBackRed
        mTab2BackSeekGreen?.progress = mTab2CurBackGreen
        mTab2BackSeekBlue?.progress = mTab2CurBackBlue
        mTab2FontSeekRed?.progress = mTab2CurFontRed
        mTab2FontSeekGreen?.progress = mTab2CurFontGreen
        mTab2FontSeekBlue?.progress = mTab2CurFontBlue
        // --------------------------------------------------------------------------------------------

    }

    // TODO (OnClick 생성하기)
    fun onClick(view: View) {
        when (view.id) {
            R.id.widget_config_btnReset -> {
                mTab1CurBackRed = MemoWidget.WIDGET_TITLE_BACK_COLOR_RED_DEFAULT
                mTab1CurBackGreen = MemoWidget.WIDGET_TITLE_BACK_COLOR_GREEN_DEFAULT
                mTab1CurBackBlue = MemoWidget.WIDGET_TITLE_BACK_COLOR_BLUE_DEFAULT
                mTab1CurFontRed = MemoWidget.WIDGET_TITLE_FONT_COLOR_RED_DEFAULT
                mTab1CurFontGreen = MemoWidget.WIDGET_TITLE_FONT_COLOR_GREEN_DEFAULT
                mTab1CurFontBlue = MemoWidget.WIDGET_TITLE_FONT_COLOR_BLUE_DEFAULT
                mTab2CurBackRed = MemoWidget.WIDGET_CONTEXT_BACK_COLOR_RED_DEFAULT
                mTab2CurBackGreen = MemoWidget.WIDGET_CONTEXT_BACK_COLOR_GREEN_DEFAULT
                mTab2CurBackBlue = MemoWidget.WIDGET_CONTEXT_BACK_COLOR_BLUE_DEFAULT
                mTab2CurFontRed = MemoWidget.WIDGET_CONTEXT_FONT_COLOR_RED_DEFAULT
                mTab2CurFontGreen = MemoWidget.WIDGET_CONTEXT_FONT_COLOR_GREEN_DEFAULT
                mTab2CurFontBlue = MemoWidget.WIDGET_CONTEXT_FONT_COLOR_BLUE_DEFAULT
                mPreviewMainLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                mPreviewTitleLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                mPreviewTxtTitle?.setTextColor(Color.rgb(mTab1CurFontRed, mTab1CurFontGreen, mTab1CurFontBlue))
                mPreviewContextLayout?.setBackgroundColor(Color.rgb(mTab2CurBackRed, mTab2CurBackGreen, mTab2CurBackBlue))
                mPreviewTxtContext?.setTextColor(Color.rgb(mTab2CurFontRed, mTab2CurFontGreen, mTab2CurFontBlue))
                mTab1BackSeekRed?.progress = mTab1CurBackRed
                mTab1BackSeekGreen?.progress = mTab1CurBackGreen
                mTab1BackSeekBlue?.progress = mTab1CurBackBlue
                mTab1FontSeekRed?.progress = mTab1CurFontRed
                mTab1FontSeekGreen?.progress = mTab1CurFontGreen
                mTab1FontSeekBlue?.progress = mTab1CurFontBlue
                mTab2BackSeekRed?.progress = mTab2CurBackRed
                mTab2BackSeekGreen?.progress = mTab2CurBackGreen
                mTab2BackSeekBlue?.progress = mTab2CurBackBlue
                mTab2FontSeekRed?.progress = mTab2CurFontRed
                mTab2FontSeekGreen?.progress = mTab2CurFontGreen
                mTab2FontSeekBlue?.progress = mTab2CurFontBlue
            }
            R.id.widget_config_btnAdd -> {
                val mSharedPrefEditor = mSharedPref.edit()
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_TITLE_BACK_COLOR_RED, mTab1CurBackRed)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_TITLE_BACK_COLOR_GREEN, mTab1CurBackGreen)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_TITLE_BACK_COLOR_BLUE, mTab1CurBackBlue)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_TITLE_FONT_COLOR_RED, mTab1CurFontRed)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_TITLE_FONT_COLOR_GREEN, mTab1CurFontGreen)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_TITLE_FONT_COLOR_BLUE, mTab1CurFontBlue)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_CONTEXT_BACK_COLOR_RED, mTab2CurBackRed)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_CONTEXT_BACK_COLOR_GREEN, mTab2CurBackGreen)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_CONTEXT_BACK_COLOR_BLUE, mTab2CurBackBlue)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_CONTEXT_FONT_COLOR_RED, mTab2CurFontRed)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_CONTEXT_FONT_COLOR_GREEN, mTab2CurFontGreen)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_CONTEXT_FONT_COLOR_BLUE, mTab2CurFontBlue)
                mSharedPrefEditor.putInt(MemoWidget.WIDGET_ID, mAppWidgetId)
                mSharedPrefEditor.apply()
                val context: Context = this@MemoWidgetConfigureActivity
                val appWidgetManager = AppWidgetManager.getInstance(context)
                MemoWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId)
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                setResult(RESULT_OK, resultValue)
                finish()
            }
            R.id.widget_config_btnLast -> {
                mTab1CurBackRed = mTab1CurBackRed_Backup
                mTab1CurBackGreen = mTab1CurBackGreen_Backup
                mTab1CurBackBlue = mTab1CurBackBlue_Backup
                mTab1CurFontRed = mTab1CurFontRed_Backup
                mTab1CurFontGreen = mTab1CurFontGreen_Backup
                mTab1CurFontBlue = mTab1CurFontBlue_Backup
                mTab2CurBackRed = mTab2CurBackRed_Backup
                mTab2CurBackGreen = mTab2CurBackGreen_Backup
                mTab2CurBackBlue = mTab2CurBackBlue_Backup
                mTab2CurFontRed = mTab2CurFontRed_Backup
                mTab2CurFontGreen = mTab2CurFontGreen_Backup
                mTab2CurFontBlue = mTab2CurFontBlue_Backup
                mPreviewMainLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                mPreviewTitleLayout?.setBackgroundColor(Color.rgb(mTab1CurBackRed, mTab1CurBackGreen, mTab1CurBackBlue))
                mPreviewTxtTitle?.setTextColor(Color.rgb(mTab1CurFontRed, mTab1CurFontGreen, mTab1CurFontBlue))
                mPreviewContextLayout?.setBackgroundColor(Color.rgb(mTab2CurBackRed, mTab2CurBackGreen, mTab2CurBackBlue))
                mPreviewTxtContext?.setTextColor(Color.rgb(mTab2CurFontRed, mTab2CurFontGreen, mTab2CurFontBlue))
                mTab1BackSeekRed?.progress = mTab1CurBackRed
                mTab1BackSeekGreen?.progress = mTab1CurBackGreen
                mTab1BackSeekBlue?.progress = mTab1CurBackBlue
                mTab1FontSeekRed?.progress = mTab1CurFontRed
                mTab1FontSeekGreen?.progress = mTab1CurFontGreen
                mTab1FontSeekBlue?.progress = mTab1CurFontBlue
                mTab2BackSeekRed?.progress = mTab2CurBackRed
                mTab2BackSeekGreen?.progress = mTab2CurBackGreen
                mTab2BackSeekBlue?.progress = mTab2CurBackBlue
                mTab2FontSeekRed?.progress = mTab2CurFontRed
                mTab2FontSeekGreen?.progress = mTab2CurFontGreen
                mTab2FontSeekBlue?.progress = mTab2CurFontBlue
            }
        }
    }

    companion object {

        // Write the prefix to the SharedPreferences object for this widget
        internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
            val prefs = context.getSharedPreferences(APP_WIDGET_PREFERENCE, Context.MODE_PRIVATE).edit()
            prefs.putString(APP_WIDGET_PREFERENCE + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
//        internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
//            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
//            val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
//            return titleValue ?: context.getString(R.string.appwidget_text)
//        }

        internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(APP_WIDGET_PREFERENCE, Context.MODE_PRIVATE).edit()
            prefs.remove(APP_WIDGET_PREFERENCE + appWidgetId)
            prefs.apply()
        }
    }


}
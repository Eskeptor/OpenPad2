package com.esk.openpadnew

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableStringBuilder
import android.widget.RemoteViews
import com.esk.openpadnew.Util.GetKnifeText
import com.esk.openpadnew.Util.LogBot
import io.github.mthli.knife.KnifeParser
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [MemoWidgetConfigureActivity]
 */
class MemoWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getIntExtra(INTENT_EXTRA_WIDGET_FILE_ID, -1) != -1) {
            mWidgetFileID = intent!!.getIntExtra(INTENT_EXTRA_WIDGET_FILE_ID, -1)
        }

        super.onReceive(context, intent)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            MemoWidgetConfigureActivity.deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        const val WIDGET_TITLE_FONT_COLOR_RED = "widget_title_font_color_r_Pref"
        const val WIDGET_TITLE_FONT_COLOR_GREEN = "widget_title_font_color_g_Pref"
        const val WIDGET_TITLE_FONT_COLOR_BLUE = "widget_title_font_color_b_Pref"
        const val WIDGET_TITLE_BACK_COLOR_RED = "widget_title_back_color_r_Pref"
        const val WIDGET_TITLE_BACK_COLOR_GREEN = "widget_title_back_color_g_Pref"
        const val WIDGET_TITLE_BACK_COLOR_BLUE = "widget_title_back_color_b_Pref"
        const val WIDGET_CONTEXT_FONT_COLOR_RED = "widget_context_font_color_r_Pref"
        const val WIDGET_CONTEXT_FONT_COLOR_GREEN = "widget_context_font_color_g_Pref"
        const val WIDGET_CONTEXT_FONT_COLOR_BLUE = "widget_context_font_color_b_Pref"
        const val WIDGET_CONTEXT_BACK_COLOR_RED = "widget_context_back_color_r_Pref"
        const val WIDGET_CONTEXT_BACK_COLOR_GREEN = "widget_context_back_color_g_Pref"
        const val WIDGET_CONTEXT_BACK_COLOR_BLUE = "widget_context_back_color_b_Pref"
        const val WIDGET_FILE_URL = "widget_file_url"
        const val WIDGET_ID = "widget_id"
        const val WIDGET_MAX_LINE = 20

        const val WIDGET_TITLE_FONT_COLOR_RED_DEFAULT = 1
        const val WIDGET_TITLE_FONT_COLOR_GREEN_DEFAULT = 1
        const val WIDGET_TITLE_FONT_COLOR_BLUE_DEFAULT = 1
        const val WIDGET_TITLE_BACK_COLOR_RED_DEFAULT = 239
        const val WIDGET_TITLE_BACK_COLOR_GREEN_DEFAULT = 239
        const val WIDGET_TITLE_BACK_COLOR_BLUE_DEFAULT = 239
        const val WIDGET_CONTEXT_FONT_COLOR_RED_DEFAULT = 20
        const val WIDGET_CONTEXT_FONT_COLOR_GREEN_DEFAULT = 20
        const val WIDGET_CONTEXT_FONT_COLOR_BLUE_DEFAULT = 20
        const val WIDGET_CONTEXT_BACK_COLOR_RED_DEFAULT = 255
        const val WIDGET_CONTEXT_BACK_COLOR_GREEN_DEFAULT = 255
        const val WIDGET_CONTEXT_BACK_COLOR_BLUE_DEFAULT = 255

        var mWidgetFileID = -1

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {
            if (mWidgetFileID == -1) {
                mWidgetFileID = appWidgetId
            }
            val pref = context.getSharedPreferences(APP_WIDGET_PREFERENCE + appWidgetId, MODE_PRIVATE)
            val views = RemoteViews(context.packageName, R.layout.memo_widget)

            val file = File(APP_INTERNAL_WIDGET_FOLDER_PATH)
            if (!file.exists()) {
                if (file.mkdir()) {
                    LogBot.logName("MemoWidget - updateAppWidget").logLevel(LogBot.Level.Error).log("Make directory - Widget Folder")
                }
            }
            val strFileURL = APP_INTERNAL_WIDGET_FOLDER_PATH + File.separator + mWidgetFileID + FILE_EXTENSION_TEXT

            val intent = Intent(context, TextMemoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(INTENT_EXTRA_MEMO_ISWIDGET, true)
            intent.putExtra(INTENT_EXTRA_MEMO_OPEN_FOLDERURL, APP_INTERNAL_WIDGET_FOLDER_PATH)
            intent.putExtra(INTENT_EXTRA_WIDGET_ID, appWidgetId)
            intent.putExtra(INTENT_EXTRA_WIDGET_FILE_ID, mWidgetFileID)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            views.setOnClickPendingIntent(R.id.widget_mainLayout, pendingIntent)

            val titleBackRed = pref.getInt(WIDGET_TITLE_BACK_COLOR_RED, WIDGET_TITLE_BACK_COLOR_RED_DEFAULT)
            val titleBackGreen = pref.getInt(WIDGET_TITLE_BACK_COLOR_GREEN, WIDGET_TITLE_BACK_COLOR_GREEN_DEFAULT)
            val titleBackBlue = pref.getInt(WIDGET_TITLE_BACK_COLOR_BLUE, WIDGET_TITLE_BACK_COLOR_BLUE_DEFAULT)
            val titleFontRed = pref.getInt(WIDGET_TITLE_FONT_COLOR_RED, WIDGET_TITLE_FONT_COLOR_RED_DEFAULT)
            val titleFontGreen = pref.getInt(WIDGET_TITLE_FONT_COLOR_GREEN, WIDGET_TITLE_FONT_COLOR_GREEN_DEFAULT)
            val titleFontBlue = pref.getInt(WIDGET_TITLE_FONT_COLOR_BLUE, WIDGET_TITLE_FONT_COLOR_BLUE_DEFAULT)
            val contextFontRed = pref.getInt(WIDGET_CONTEXT_FONT_COLOR_RED, WIDGET_CONTEXT_FONT_COLOR_RED_DEFAULT)
            val contextFontGreen = pref.getInt(WIDGET_CONTEXT_FONT_COLOR_GREEN, WIDGET_CONTEXT_FONT_COLOR_GREEN_DEFAULT)
            val contextFontBlue = pref.getInt(WIDGET_CONTEXT_FONT_COLOR_BLUE, WIDGET_CONTEXT_FONT_COLOR_BLUE_DEFAULT)
            val contextBackRed = pref.getInt(WIDGET_CONTEXT_BACK_COLOR_RED, WIDGET_CONTEXT_BACK_COLOR_RED_DEFAULT)
            val contextBackGreen = pref.getInt(WIDGET_CONTEXT_BACK_COLOR_GREEN, WIDGET_CONTEXT_BACK_COLOR_GREEN_DEFAULT)
            val contextBackBlue = pref.getInt(WIDGET_CONTEXT_BACK_COLOR_BLUE, WIDGET_CONTEXT_BACK_COLOR_BLUE_DEFAULT)

            views.setTextColor(R.id.widget_title, Color.rgb(titleFontRed, titleFontGreen, titleFontBlue))
            views.setInt(R.id.widget_title_layout, "setBackgroundColor", Color.rgb(titleBackRed, titleBackGreen, titleBackBlue))
            views.setTextColor(R.id.widget_context, Color.rgb(contextFontRed, contextFontGreen, contextFontBlue))
            views.setInt(R.id.widget_mainLayout, "setBackgroundColor", Color.rgb(contextBackRed, contextBackGreen, contextBackBlue))

            val tmpFile = File(APP_INTERNAL_WIDGET_FOLDER_PATH + File.separator + mWidgetFileID + FILE_EXTENSION_TEXT)

            var strTitle = ""
            val strContents: StringBuilder = StringBuilder()

            if (tmpFile.exists()) {
                var fr: FileReader? = null
                var br: BufferedReader? = null
                var nCurLine = 0
                val STR_CUR_COUNTRY = Locale.getDefault().displayCountry
                var strLine = ""

                try {
                    fr = FileReader(strFileURL)
                    br = BufferedReader(fr)

                    strTitle = when (STR_CUR_COUNTRY) {
                        Locale.KOREA.displayCountry -> {
                            SimpleDateFormat(DATE_FORMAT_MAIN_KOREA, Locale.KOREA).format(
                                Date(
                                    File(
                                        strFileURL
                                    ).lastModified()
                                )
                            )
                        }
                        Locale.UK.displayCountry -> {
                            SimpleDateFormat(DATE_FORMAT_MAIN_UK, Locale.UK).format(
                                Date(
                                    File(
                                        strFileURL
                                    ).lastModified()
                                )
                            )
                        }
                        else -> {
                            SimpleDateFormat(DATE_FORMAT_MAIN_USA, Locale.US).format(
                                Date(
                                    File(
                                        strFileURL
                                    ).lastModified()
                                )
                            )
                        }
                    }

                    while (nCurLine < WIDGET_MAX_LINE) {
                        strLine = br.readLine()
                        if (strLine != null) {
                            strContents.appendln(strLine)
                            nCurLine++
                        } else {
                            break
                        }
                    }
                } catch (e: Exception) {
                    LogBot.logName("MemoWidget - updateAppWidget").logLevel(LogBot.Level.Error)
                        .log(e.toString())
                } finally {
                    if (br != null) {
                        try {
                            br.close()
                        } catch (e: Exception) {
                            LogBot.logName("MemoWidget - updateAppWidget")
                                .logLevel(LogBot.Level.Error).log(e.toString())
                        }
                    }
                    if (fr != null) {
                        try {
                            fr.close()
                        } catch (e: Exception) {
                            LogBot.logName("MemoWidget - updateAppWidget")
                                .logLevel(LogBot.Level.Error).log(e.toString())
                        }
                    }
                }

                val builder: SpannableStringBuilder = GetKnifeText.getKnifeText(strContents.toString())
                strContents.clear()
                strContents.append(builder)
            } else {
                strTitle = context.getString(R.string.widget_title_new)
                strContents.append(context.getString(R.string.widget_context_new))
            }

            views.setTextViewText(R.id.widget_title, strTitle)
            views.setTextViewText(R.id.widget_context, strContents)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }


    }
}
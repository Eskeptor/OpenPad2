package com.esk.openpadnew

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

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

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            //val widgetText = MemoWidgetConfigureActivity.loadTitlePref(context, appWidgetId)
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.memo_widget)
            //views.setTextViewText(R.id.appwidget_text, widgetText)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        val WIDGET_TITLE_FONT_COLOR_RED = "widget_title_font_color_r_Pref"
        val WIDGET_TITLE_FONT_COLOR_GREEN = "widget_title_font_color_g_Pref"
        val WIDGET_TITLE_FONT_COLOR_BLUE = "widget_title_font_color_b_Pref"
        val WIDGET_TITLE_BACK_COLOR_RED = "widget_title_back_color_r_Pref"
        val WIDGET_TITLE_BACK_COLOR_GREEN = "widget_title_back_color_g_Pref"
        val WIDGET_TITLE_BACK_COLOR_BLUE = "widget_title_back_color_b_Pref"
        val WIDGET_CONTEXT_FONT_COLOR_RED = "widget_context_font_color_r_Pref"
        val WIDGET_CONTEXT_FONT_COLOR_GREEN = "widget_context_font_color_g_Pref"
        val WIDGET_CONTEXT_FONT_COLOR_BLUE = "widget_context_font_color_b_Pref"
        val WIDGET_CONTEXT_BACK_COLOR_RED = "widget_context_back_color_r_Pref"
        val WIDGET_CONTEXT_BACK_COLOR_GREEN = "widget_context_back_color_g_Pref"
        val WIDGET_CONTEXT_BACK_COLOR_BLUE = "widget_context_back_color_b_Pref"
        val WIDGET_FILE_URL = "widget_file_url"
        val WIDGET_ID = "widget_id"
        val WIDGET_MAX_LINE = 20

        val WIDGET_TITLE_FONT_COLOR_RED_DEFAULT = 1
        val WIDGET_TITLE_FONT_COLOR_GREEN_DEFAULT = 1
        val WIDGET_TITLE_FONT_COLOR_BLUE_DEFAULT = 1
        val WIDGET_TITLE_BACK_COLOR_RED_DEFAULT = 239
        val WIDGET_TITLE_BACK_COLOR_GREEN_DEFAULT = 239
        val WIDGET_TITLE_BACK_COLOR_BLUE_DEFAULT = 239
        val WIDGET_CONTEXT_FONT_COLOR_RED_DEFAULT = 20
        val WIDGET_CONTEXT_FONT_COLOR_GREEN_DEFAULT = 20
        val WIDGET_CONTEXT_FONT_COLOR_BLUE_DEFAULT = 20
        val WIDGET_CONTEXT_BACK_COLOR_RED_DEFAULT = 255
        val WIDGET_CONTEXT_BACK_COLOR_GREEN_DEFAULT = 255
        val WIDGET_CONTEXT_BACK_COLOR_BLUE_DEFAULT = 255
    }
}
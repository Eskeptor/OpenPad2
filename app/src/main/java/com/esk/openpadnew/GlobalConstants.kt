package com.esk.openpadnew

import android.os.Environment
import com.esk.openpadnew.SortType.*
import java.io.File

const val FILE_EXTENSION_TEXT: String = ".html"
const val FILE_EXTENSION_IMAGE: String = ".png"
const val FILE_EXTENSION_IMAGE_SUMMARY: String = ".smy"

const val PREF_SWIPE_DELETE: String = "isSwipeDeleteEnabled"
const val PREF_VIEW_IMAGE: String = "isViewImageEnabled"
const val PREF_FIRST_BOOT: String = "isFirstBoot"
const val PREF_FOLDER_VIEW_STYLE = "isFolderViewStyleBig"
const val PREF_FOLDER_SORT_TYPE = "folderSortType"
const val PREF_FOLDER_SORT_IS_ASC = "isFolderSortASC"
const val PREF_MAIN_VIEW_STYLE = "isMainViewStyleBig"
const val PREF_MAIN_SORT_TYPE = "mainSortType"
const val PREF_MAIN_SORT_IS_ASC = "isMainSortASC"
const val PREF_FONT_SIZE = "fontSize"
const val PREF_ADMOB_VISIBLE = "isAdMobVisible"
const val PREF_SET_PASSWORD = "isSetPassword"
const val PREF_KEY_CODE = "passwordKey"
const val PREF_PASSWORD = "passwordValue"

const val APP_FONT_SIZE_MIN: Int = 8
const val APP_FONT_SIZE_MAX: Int = 30
const val APP_DEFAULT_FONT_SIZE: Float = 18f
const val APP_PREFERENCE: String = "OpenPadPreference"
const val APP_MASTER_FOLDER_NAME: String = "OpenPadMemo"
const val APP_DEFAULT_FOLDER_NAME: String = "Default"
const val APP_WIDGET_FOLDER_NAME: String = "Widget"
const val APP_WIDGET_PREFERENCE: String = "Widget_Pref"
val APP_INTERNAL_PATH: String = Environment.getExternalStorageDirectory().absolutePath + File.separator + APP_MASTER_FOLDER_NAME
val APP_INTERNAL_DEFAULT_FOLDER_PATH: String = APP_INTERNAL_PATH + File.separator + APP_DEFAULT_FOLDER_NAME
val APP_INTERNAL_WIDGET_FOLDER_PATH: String = APP_INTERNAL_PATH + File.separator + APP_WIDGET_FOLDER_NAME
//const val APP_MAIN_LIST_TYPE: String = "isMainListModule"
// val APP_INTERNAL_DEFAULT_FOLDER_PATH: String = filesDir.absolutePath + File.separator + APP_MASTER_FOLDER_NAME

const val EXTRA_MEMO_OPEN_FILE_URL: String = "EXTRA_MEMO_OPEN_FILE_URL"
const val EXTRA_MEMO_OPEN_FOLDER_URL: String = "EXTRA_MEMO_OPEN_FOLDER_URL"
const val EXTRA_PASSWORD_INTENT_TYPE: String = "EXTRA_PASSWORD_INTENT_TYPE"
const val EXTRA_PASSWORD_SET: String = "EXTRA_PASSWORD_SET"
const val EXTRA_PASSWORD_MATCH: String = "EXTRA_PASSWORD_MATCH"

const val INTENT_EXTRA_MEMO_OPEN_FILEURL: String = "MEMO_OPEN_FILEURL"
const val INTENT_EXTRA_MEMO_OPEN_FILENAME: String = "MEMO_OPEN_FILENAME"
const val INTENT_EXTRA_MEMO_OPEN_FOLDERURL: String = "MEMO_OPEN_FOLDERURL"
const val INTENT_EXTRA_BROWSER_TYPE: String = "BROWSER_TYPE"
const val INTENT_EXTRA_CURRENT_FOLDERURL: String = "CURRENT_FOLDERURL"
const val INTENT_EXTRA_MEMO_SAVE_FOLDERURL: String = "MEMO_SAVE_FOLDERURL"
const val INTENT_EXTRA_MEMO_SAVE_FILEURL: String = "MEMO_SAVE_FILEURL"
const val INTENT_EXTRA_HELP_INDEX: String = "HELP_INDEX"
const val INTENT_EXTRA_MEMO_ISWIDGET: String = "MEMO_ISWIDGET"
const val INTENT_EXTRA_WIDGET_ID: String = "WIDGET_ID"
const val INTENT_EXTRA_WIDGET_FILE_ID: String = "WIDGET_FILE_ID"
const val INTENT_EXTRA_PASSWORD: String = "PASSWORD_INTENT_TYPE"
const val INTENT_EXTRA_PASSWORD_MATCH: String = "PASSWORD_MATCH"
const val INTENT_EXTRA_PASSWORD_SET: String = "PASSWORD_SET"

const val REQUEST_CODE_PASSWORD_FLAG: Int = 1000

const val DATE_FORMAT_WIDGET_KOREA = "yyyy년 MM월 dd일"
const val DATE_FORMAT_WIDGET_USA = "MM/dd/yyyy"
const val DATE_FORMAT_WIDGET_UK = "dd/MM/yyyy"
const val DATE_FORMAT_MAIN_KOREA = "yyyy년 MM월 dd일 hh:mm a"
const val DATE_FORMAT_MAIN_USA = "MM/dd/yyyy hh:mm a"
const val DATE_FORMAT_MAIN_UK = "dd/MM/yyyy hh:mm a"

/**
 * 파일 또는 폴더를 정렬할 때 사용하는 Enum
 * @param value Preference 에서 저장, 읽어들이기 편하게 사용하기 위한 변수
 * @property Name 이름으로 정렬
 * @property Time 수정한 시간으로 정렬
 * @property Files 파일 개수로 정렬
 */
enum class SortType(val value: Int) {
    Name(0), Time(1), Files(2)
}

enum class PasswordIntentType(val value: Int) {
    Set(0), Reset(1), Execute(2), MainExecute(3)
}
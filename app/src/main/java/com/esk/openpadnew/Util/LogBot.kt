package com.esk.openpadnew.Util

import android.util.Log
import com.esk.openpadnew.BuildConfig

/**
 * 로그용 봇
 * @param name 로그 명
 * @param level 로그 레벨
 */
class LogBot(val name: String = "", val level: Level = Level.Debug) {
    /**
     * 로그의 레벨을 나타내는 enum class
     * @param level 로그의 레벨을 구분하기 위한 값
     */
    enum class Level(val level: Int) {
        Error(6), Debug(3), Info(4), Verbose(2), Warn(5)
    }

    companion object {
        private var mLogName: String = ""
        private var mLevel: Level = Level.Debug

        /**
         * 로그의 이름 설정한다.
         * @param name 로그의 이름
         */
        fun logName(name: String = ""): LogBot {
            mLogName = name
            return LogBot(mLogName, mLevel)
        }
    }


    /**
     * 로그 레벨을 설정한다.
     * @param level 로그의 레벨
     */
    fun logLevel(level: Level = Level.Debug): LogBot {
        mLevel = level
        return LogBot(mLogName, mLevel)
    }


    /**
     * 로그 내용 입력 및 출력
     * @param context 내용
     */
    fun log(context: String = "") {
        if (BuildConfig.DEBUG) {
            when (mLevel) {
                Level.Verbose   -> Log.v(mLogName, context)
                Level.Debug     -> Log.d(mLogName, context)
                Level.Info      -> Log.i(mLogName, context)
                Level.Warn      -> Log.w(mLogName, context)
                Level.Error     -> Log.e(mLogName, context)
            }
        }
    }
}
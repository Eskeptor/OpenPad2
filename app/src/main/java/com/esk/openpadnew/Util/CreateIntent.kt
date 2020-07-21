package com.esk.openpadnew.Util

import android.content.Context
import android.content.Intent

/**
 * 생성할 Intent 의 타입용 enum class
 */
enum class IntentType(val value: Int) {
    TextMemo(0), PaintMemo(1), Tutorial(2)
}


/**
 * Intent 를 생성하는 클래스
 */
class CreateIntent {
    companion object {
        /**
         * Intent 를 FLAG_ACTIVITY_SINGLE_TOP 플래그와 함께 생성한다.
         * @param packageContext 컨텍스트
         * @param cls 생성할 대상
         */
        fun createIntent(packageContext: Context?, cls: Class<*>) : Intent {
            val intent = Intent(packageContext, cls)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            return intent
        }
    }
}
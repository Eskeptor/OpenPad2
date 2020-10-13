package com.esk.openpadnew.Util

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.QuoteSpan
import android.text.style.URLSpan
import io.github.mthli.knife.KnifeBulletSpan
import io.github.mthli.knife.KnifeParser
import io.github.mthli.knife.KnifeQuoteSpan
import io.github.mthli.knife.KnifeURLSpan

class GetKnifeText {
    companion object {
        /**
         * Intent 를 FLAG_ACTIVITY_SINGLE_TOP 플래그와 함께 생성한다.
         * @param packageContext 컨텍스트
         * @param cls 생성할 대상
         */
        fun getKnifeText(source: String) : SpannableStringBuilder {
            val builder = SpannableStringBuilder()
            builder.append(KnifeParser.fromHtml(source))
            return builder
        }
    }
}
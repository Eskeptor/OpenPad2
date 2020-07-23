package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class CircleSeekbarLicense : License() {
    override fun getUrl(): String = "https://github.com/feeeei/CircleSeekbar"

    override fun getName(): String = "CircleSeekbar"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.circleseekbar_license_summary)

    override fun getVersion(): String = "1.1.2"

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.circleseekbar_license_summary)

    fun getLicenseName(): String = "CircleSeekbar License"
}
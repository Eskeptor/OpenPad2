package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class SlidingTutorialLicense : License() {
    override fun getUrl(): String = "https://github.com/Cleveroad/SlidingTutorial-Android"

    override fun getName(): String = "SlidingTutorial Android"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.slidingtutorial_license_summary)

    override fun getVersion(): String = "1.0.8"

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.mit_full)

    fun getLicenseName(): String = "SlidingTutorial Android License"
}
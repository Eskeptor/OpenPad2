package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class FlycoTabLayoutLicense : License() {
    override fun getUrl(): String = "https://github.com/H07000223/FlycoTabLayout"

    override fun getName(): String = "FlycoTabLayout"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.mit_summary)

    override fun getVersion(): String = "2.0.2"

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.mit_full)

    fun getLicenseName(): String = "FlycoTabLayout License"
}
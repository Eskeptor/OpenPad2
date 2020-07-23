package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class ApacheCCLicense : License() {
    override fun getUrl(): String = "http://commons.apache.org/"

    override fun getName(): String = "Apache Commons Codec"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.asl_20_summary)

    override fun getVersion(): String = "1.11"

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.asl_20_full)

    fun getLicenseName(): String = "Apache Commons Codec License"
}
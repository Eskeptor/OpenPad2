package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class KnifeLicense : License() {
    override fun getUrl(): String = "https://github.com/mthli/Knife"

    override fun getName(): String = "Knife"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.knife_license_summary)

    override fun getVersion(): String = "1.1"

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.asl_20_full)

    fun getLicenseName(): String = "Knife License"
}
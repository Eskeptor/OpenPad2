package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.BuildConfig
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class OpenpadLicense : License() {
    override fun getUrl(): String = "https://github.com/eskeptor"

    override fun getName(): String = "Openpad"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.openpad_license_summary)

    override fun getVersion(): String = BuildConfig.VERSION_NAME

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.asl_20_full)

    fun getLicenseName(): String = "Openpad License"
}
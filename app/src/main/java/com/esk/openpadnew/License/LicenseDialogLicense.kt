package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class LicenseDialogLicense : License() {
    override fun getUrl(): String = "https://github.com/PSDev/LicensesDialog"

    override fun getName(): String = "Licenses Dialog"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.licensesdialog_license_summary)

    override fun getVersion(): String = "1.8.3"

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.asl_20_full)

    fun getLicenseName(): String = "Licenses Dialog License"
}
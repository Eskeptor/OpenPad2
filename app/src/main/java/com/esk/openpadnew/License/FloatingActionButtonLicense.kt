package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class FloatingActionButtonLicense : License() {
    override fun getUrl(): String = "https://github.com/PSDev/LicensesDialog"

    override fun getName(): String = "Floating ActionButton"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.floatingactionbutton_license_summary)

    override fun getVersion(): String = "1.8.3"

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.asl_20_summary)

    fun getLicenseName(): String = "Floating ActionButton License"
}
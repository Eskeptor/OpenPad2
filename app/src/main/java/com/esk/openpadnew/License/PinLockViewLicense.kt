package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class PinLockViewLicense : License() {
    override fun getUrl(): String = "https://github.com/aritraroy/PinLockView"

    override fun getName(): String = "PinLockView"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.pinlockview_license_summary)

    override fun getVersion(): String = "2.1.0"

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.asl_20_full)

    fun getLicenseName(): String = "PinLockView License"
}
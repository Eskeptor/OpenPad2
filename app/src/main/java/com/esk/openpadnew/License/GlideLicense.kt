package com.esk.openpadnew.License

import android.content.Context
import com.esk.openpadnew.R
import de.psdev.licensesdialog.licenses.License

class GlideLicense : License() {
    override fun getUrl(): String = "https://bumptech.github.io/glide/"

    override fun getName(): String = "Glide(v4)"

    override fun readSummaryTextFromResources(context: Context?): String = getContent(context, R.raw.glide_license_summary)

    override fun getVersion(): String = "4.7.1"

    override fun readFullTextFromResources(context: Context?): String = getContent(context, R.raw.glide_license_full)

    fun getLicenseName(): String = "Glide(v4) License"
}
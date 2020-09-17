package com.esk.openpadnew

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.esk.openpadnew.License.*
import com.esk.openpadnew.Util.CreateIntent
import com.esk.openpadnew.Util.LogBot
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

const val MY_GITHUB: String = "https://github.com/Eskeptor"
const val DEVELOPER_MAIL = "skyvvv624@naver.com"
private const val REQUEST_CODE_PASSWORD: Int = 1

enum class FontType(val value: Int) {
    Default(0), BaeDalJUA(1), KOPUBDotum(2)
}

class SettingsActivity : AppCompatActivity() {
    enum class ActiveScreenType {
        Main, Font, Security, UpdateList
    }

    companion object {
        private lateinit var mSharedPref: SharedPreferences
        private lateinit var mSettingsAct: Settings
        private lateinit var mFontAct: FontSettings
        private lateinit var mActiveScreen: ActiveScreenType
        private lateinit var mSecurityAct: Security
        private var mPasswordFlag: Boolean = false
        private lateinit var mPragmentManager: FragmentManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()

        mSharedPref = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)

        // 패스워드창 플래그 설정
        mPasswordFlag = false

        mSettingsAct = Settings()
        mFontAct = FontSettings()
        mSecurityAct = Security()

        mPragmentManager = supportFragmentManager
        mPragmentManager.beginTransaction().replace(android.R.id.content, mSettingsAct).commit()
        mActiveScreen = ActiveScreenType.Main
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportActionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.baseline_back_black_24, null))
        } else {
            supportActionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.baseline_back_black_24))
        }
        supportActionBar?.elevation = 0.0f
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragmentCompat::class.java.name == fragmentName ||
                Settings::class.java.name == fragmentName ||
                FontSettings::class.java.name == fragmentName ||
                Security::class.java.name == fragmentName
    }

    override fun onBackPressed() {
        when (mActiveScreen) {
            ActiveScreenType.Main -> {
                setResult(Activity.RESULT_OK)
                super.onBackPressed()
            }
            ActiveScreenType.Font -> {
                mPragmentManager.beginTransaction().replace(android.R.id.content, mSettingsAct).commit()
                mActiveScreen = ActiveScreenType.Main
            }
            else -> {
                mPragmentManager.beginTransaction().replace(android.R.id.content, mSettingsAct).commit()
                mActiveScreen = ActiveScreenType.Main
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mPasswordFlag = true
    }

    override fun onResume() {
        super.onResume()

        if (mPasswordFlag) {
            if (mSharedPref.getBoolean(PREF_SET_PASSWORD, false)) {
                val intent = CreateIntent.createIntent(applicationContext, PasswordActivity::class.java)
                intent.putExtra(EXTRA_PASSWORD_INTENT_TYPE, PasswordIntentType.MainExecute.value)
                startActivityForResult(intent, REQUEST_CODE_PASSWORD_FLAG)
            }
        }

        mPasswordFlag = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPasswordFlag = false
    }

    class Settings : PreferenceFragmentCompat() {
        private var mAdMob: CheckBoxPreference? = null
        private var mViewImage: CheckBoxPreference? = null
        private var mSwipeDelete: CheckBoxPreference? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    REQUEST_CODE_PASSWORD -> {
                        mPasswordFlag = false
                        mPragmentManager.beginTransaction().replace(android.R.id.content, mSecurityAct).commit()
                        mActiveScreen = ActiveScreenType.Security
                    }
                }
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_settings)

            LogBot.logName("settings").log("Settings - onCreate")

            val info: Preference? = findPreference("settings_key_info")
            val version: Preference? = findPreference("settings_key_version")
            val font: Preference? = findPreference("settings_key_font")
            val bugReport: Preference? = findPreference("settings_key_bugreport")
            val updateList: Preference? = findPreference("settings_key_updatelist")
            mAdMob = findPreference("settings_key_admob") as CheckBoxPreference?
            mViewImage = findPreference("settings_key_viewimage") as CheckBoxPreference?
            mSwipeDelete = findPreference("settings_key_swipe_delete") as CheckBoxPreference?
            val license: Preference? = findPreference("settings_key_license")
            val security: Preference? = findPreference("settings_key_security")

            val clickListener: Preference.OnPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
                if (preference != null) {
                    when (preference.key) {
                        "settings_key_info" -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(MY_GITHUB))
                            startActivity(intent)
                        }
                        "settings_key_version" -> {

                        }
                        "settings_key_font" -> {
                            mPragmentManager.beginTransaction()
                                .replace(android.R.id.content, mFontAct).commit()
                            mActiveScreen = ActiveScreenType.Font
                        }
                        "settings_key_bugreport" -> {
                            val intent = Intent(Intent.ACTION_SEND)
                            val address: Array<String> = arrayOf(DEVELOPER_MAIL)
                            intent.type = "message/rfc822"
                            intent.putExtra(Intent.EXTRA_EMAIL, address)
                            intent.putExtra(Intent.EXTRA_SUBJECT, "")
                            intent.putExtra(Intent.EXTRA_TEXT, "")
                            startActivity(
                                Intent.createChooser(
                                    intent,
                                    getString(R.string.settings_information_bug_email_choose)
                                )
                            )
                        }
                        "settings_key_license" -> {
                            val notices = Notices()
                            val openpadLicense = OpenpadLicense()
                            val accLicense = ApacheCCLicense()
                            val circleLicense = CircleSeekbarLicense()
                            val fabLicense = FloatingActionButtonLicense()
                            val flycoLicense = FlycoTabLayoutLicense()
                            val glideLicense = GlideLicense()
                            val knifeLicense = KnifeLicense()
                            val licenseDialogLicense = LicenseDialogLicense()
                            val stLicense = SlidingTutorialLicense()
                            val pinLockLicense = PinLockViewLicense()

                            notices.addNotice(
                                Notice(
                                    openpadLicense.name,
                                    openpadLicense.url,
                                    openpadLicense.getLicenseName(),
                                    openpadLicense
                                )
                            )
                            notices.addNotice(
                                Notice(
                                    accLicense.name,
                                    accLicense.url,
                                    accLicense.getLicenseName(),
                                    accLicense
                                )
                            )
                            notices.addNotice(
                                Notice(
                                    circleLicense.name,
                                    circleLicense.url,
                                    circleLicense.getLicenseName(),
                                    circleLicense
                                )
                            )
                            notices.addNotice(
                                Notice(
                                    fabLicense.name,
                                    fabLicense.url,
                                    fabLicense.getLicenseName(),
                                    fabLicense
                                )
                            )
                            notices.addNotice(
                                Notice(
                                    flycoLicense.name,
                                    flycoLicense.url,
                                    flycoLicense.getLicenseName(),
                                    flycoLicense
                                )
                            )
                            notices.addNotice(
                                Notice(
                                    glideLicense.name,
                                    glideLicense.url,
                                    glideLicense.getLicenseName(),
                                    glideLicense
                                )
                            )
                            notices.addNotice(
                                Notice(
                                    knifeLicense.name,
                                    knifeLicense.url,
                                    knifeLicense.getLicenseName(),
                                    knifeLicense
                                )
                            )
                            notices.addNotice(
                                Notice(
                                    licenseDialogLicense.name,
                                    licenseDialogLicense.url,
                                    licenseDialogLicense.getLicenseName(),
                                    licenseDialogLicense
                                )
                            )
                            notices.addNotice(
                                Notice(
                                    stLicense.name,
                                    stLicense.url,
                                    stLicense.getLicenseName(),
                                    stLicense
                                )
                            )
                            notices.addNotice(
                                Notice(
                                    pinLockLicense.name,
                                    pinLockLicense.url,
                                    pinLockLicense.getLicenseName(),
                                    pinLockLicense
                                )
                            )
                            LicensesDialog.Builder(activity)
                                .setNotices(notices)
                                .setShowFullLicenseText(false)
                                .setIncludeOwnLicense(false)
                                .build()
                                .show()
                        }
                        "settings_key_admob" -> {
                            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                            builder.setTitle(R.string.settings_general_admob)
                            builder.setMessage(R.string.settings_general_admob_dialog_context)
                            builder.setPositiveButton(R.string.dialog_ok) { dialog: DialogInterface?, _: Int ->
                                dialog!!.dismiss()
                            }
                            builder.show()
                        }
                        "settings_key_security" -> {
                            if (mSharedPref.getBoolean(PREF_SET_PASSWORD, false)) {
                                val intent = CreateIntent.createIntent(
                                    activity,
                                    PasswordActivity::class.java
                                )
                                intent.putExtra(
                                    EXTRA_PASSWORD_INTENT_TYPE,
                                    PasswordIntentType.Execute.value
                                )
                                startActivityForResult(intent, REQUEST_CODE_PASSWORD)
                            } else {
                                mPragmentManager.beginTransaction()
                                    .replace(android.R.id.content, mSecurityAct).commit()
                            }
                            mActiveScreen = ActiveScreenType.Security
                        }
                    }
                }
                return@OnPreferenceClickListener false
            }

            version?.summary = BuildConfig.VERSION_NAME

            info?.onPreferenceClickListener = clickListener
            version?.onPreferenceClickListener = clickListener
            font?.onPreferenceClickListener = clickListener
            bugReport?.onPreferenceClickListener = clickListener
            updateList?.onPreferenceClickListener = clickListener
            license?.onPreferenceClickListener = clickListener
            security?.onPreferenceClickListener = clickListener
            mAdMob?.onPreferenceClickListener = clickListener
            mAdMob?.isChecked = mSharedPref.getBoolean(PREF_ADMOB_VISIBLE, true)
            mViewImage?.isChecked = mSharedPref.getBoolean(PREF_VIEW_IMAGE, true)
            mSwipeDelete?.isChecked = mSharedPref.getBoolean(PREF_SWIPE_DELETE, true)

            retainInstance = true
        }

        override fun onPause() {
            super.onPause()
            val editor: SharedPreferences.Editor = mSharedPref.edit()
            editor.putBoolean(PREF_ADMOB_VISIBLE, mAdMob!!.isChecked)
            editor.putBoolean(PREF_VIEW_IMAGE, mViewImage!!.isChecked)
            editor.putBoolean(PREF_SWIPE_DELETE, mSwipeDelete!!.isChecked)
            editor.apply()
        }
    }

    class FontSettings : PreferenceFragmentCompat() {
        private var mTextSize: Preference? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

        }

        override fun onPause() {
            super.onPause()
            mActiveScreen = ActiveScreenType.Main
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_settings_font)

            mTextSize = findPreference("settings_key_font_fontsize")

            mTextSize?.summary = mSharedPref.getFloat(PREF_FONT_SIZE, APP_DEFAULT_FONT_SIZE).toString()
            mTextSize?.setDefaultValue(mSharedPref.getFloat(PREF_FONT_SIZE, APP_DEFAULT_FONT_SIZE).toString())
            mTextSize?.setOnPreferenceClickListener { _: Preference? ->
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.settings_dialog_font_size)
                builder.setMessage(R.string.settings_dialog_font_context)
                val layout: View = LayoutInflater.from(activity).inflate(R.layout.dialog_fontsize, null)
                val sizePreview: TextView = layout.findViewById(R.id.dialog_font_preview)
                val picker: NumberPicker = layout.findViewById(R.id.dialog_font_picker)
                picker.minValue = APP_FONT_SIZE_MIN
                picker.maxValue = APP_FONT_SIZE_MAX
                sizePreview.textSize = mSharedPref.getFloat(PREF_FONT_SIZE, APP_DEFAULT_FONT_SIZE)
                picker.value = mSharedPref.getFloat(PREF_FONT_SIZE, APP_DEFAULT_FONT_SIZE).toInt()
                picker.setOnScrollListener { _, _ ->
                    sizePreview.textSize = picker.value.toFloat()
                }
                picker.setOnValueChangedListener { _, oldVal, newVal ->
                    if (oldVal != newVal) {
                        sizePreview.textSize = picker.value.toFloat()
                        mTextSize!!.summary = newVal.toString()
                    }
                }
                builder.setView(layout)
                builder.setPositiveButton(R.string.dialog_ok) {dialog: DialogInterface?, _: Int ->
                    val editor: SharedPreferences.Editor = mSharedPref.edit()
                    editor.putFloat(PREF_FONT_SIZE, picker.value.toFloat())
                    editor.apply()
                    dialog!!.dismiss()
                }
                builder.show()
                return@setOnPreferenceClickListener false
            }
            retainInstance = true
        }
    }

    class Security : PreferenceFragmentCompat() {
        private var mSetPasswordApp: CheckBoxPreference? = null
        private var mResetPassword: Preference? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    when (requestCode) {
                        REQUEST_CODE_PASSWORD_FLAG -> {
                            mPasswordFlag = false
                        }
                        REQUEST_CODE_PASSWORD -> {
                            mPasswordFlag = false
                            mResetPassword?.isEnabled = true
                        }
                    }
                }
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_settings_security)

            mSetPasswordApp = findPreference("settings_key_password_app") as CheckBoxPreference?
            mResetPassword = findPreference("settings_key_password_reset")

            val isSetPassword: Boolean = mSharedPref.getBoolean(PREF_SET_PASSWORD, false)
            mSetPasswordApp?.isChecked = isSetPassword
            mResetPassword?.isEnabled = isSetPassword

            mSetPasswordApp?.setOnPreferenceClickListener { _: Preference? ->
                if (mSharedPref.getBoolean(PREF_SET_PASSWORD, false)) {
                    val sharedPrefEditor: SharedPreferences.Editor = mSharedPref.edit()
                    sharedPrefEditor.putBoolean(PREF_SET_PASSWORD, false)
                    sharedPrefEditor.apply()
                    mResetPassword?.isEnabled = false
                } else {
                    val intent = CreateIntent.createIntent(activity, PasswordActivity::class.java)
                    intent.putExtra(EXTRA_PASSWORD_INTENT_TYPE, PasswordIntentType.Set.value)
                    startActivityForResult(intent, REQUEST_CODE_PASSWORD)
                }
                return@setOnPreferenceClickListener false
            }
            mResetPassword?.setOnPreferenceClickListener { _: Preference? ->
                val intent = CreateIntent.createIntent(activity, PasswordActivity::class.java)
                intent.putExtra(EXTRA_PASSWORD_INTENT_TYPE, PasswordIntentType.Reset.value)
                startActivityForResult(intent, REQUEST_CODE_PASSWORD_FLAG)
                return@setOnPreferenceClickListener false
            }
        }
    }
}
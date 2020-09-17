package com.esk.openpadnew

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.andrognito.pinlockview.PinLockView
import com.esk.openpadnew.Util.AES256Util
import com.esk.openpadnew.Util.LogBot
import kotlinx.android.synthetic.main.activity_password.*

private const val DEFAULT_KEY_CODE: String = "ABCDABCDABCDABCD"

class PasswordActivity : AppCompatActivity() {
    private var mKeyToken: String = ""
    private lateinit var mAES256: AES256Util
    private var mPasswordType: Int = PasswordIntentType.Set.value
    private var mPrevPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        val sharedPref: SharedPreferences = getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)
        mKeyToken = sharedPref.getString(PREF_KEY_CODE, DEFAULT_KEY_CODE)!!
        mAES256 = AES256Util(mKeyToken)

        mPasswordType = intent.getIntExtra(EXTRA_PASSWORD_INTENT_TYPE, PasswordIntentType.Set.value)

        when (mPasswordType) {
            PasswordIntentType.Set.value    -> {
                pass_txtComment.text = getString(R.string.password_set)
            }
            PasswordIntentType.Reset.value  -> {
                pass_txtComment.text = getString(R.string.password_reset)
            }
            else                            -> {
                pass_txtComment.text = getString(R.string.password_enter)
            }
        }

        var isRepeat = false
        val pinLockView: PinLockView = findViewById(R.id.pin_lock_view)
        val pinLockIndicator: IndicatorDots = findViewById(R.id.pin_indicator)
        val pinLockListener: PinLockListener = object : PinLockListener {
            override fun onComplete(pin: String) {
                //LogBot.logName("Debugbug").logLevel(LogBot.Level.Error).log("Pin complete: $pin")
            }

            override fun onEmpty() {
                //LogBot.logName("Debugbug").logLevel(LogBot.Level.Error).log("Pin empty")
            }

            override fun onPinChange(pinLength: Int, intermediatePin: String) {
                //LogBot.logName("Debugbug").logLevel(LogBot.Level.Error).log("Pin changed, new length $pinLength with intermediate pin $intermediatePin")

                if (pinLength == 4) {
                    when (mPasswordType) {
                        PasswordIntentType.Execute.value, PasswordIntentType.MainExecute.value -> {
                            val realPass = sharedPref.getString(PREF_PASSWORD, "")
                            if (realPass != null && realPass.isNotEmpty()) {
                                val decodePass = mAES256.aesDecode(realPass)
                                if (intermediatePin == decodePass) {
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                } else {
                                    pinLockView.resetPinLockView()
                                    pass_txtComment.text = getString(R.string.password_incorrect)
                                }
                            }
                        }
                        else -> {

                            if (isRepeat)
                            {
                                if (mPrevPassword == intermediatePin) {
                                    val editor: SharedPreferences.Editor = sharedPref.edit()
                                    editor.putString(PREF_PASSWORD, mAES256.aesEncode(intermediatePin))
                                    editor.putBoolean(PREF_SET_PASSWORD, true)
                                    editor.apply()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                } else {
                                    pinLockView.resetPinLockView()
                                }
                            }
                            else
                            {
                                pinLockView.resetPinLockView()
                                mPrevPassword = intermediatePin
                                isRepeat = true
                                pass_txtComment.text = getString(R.string.password_again)
                            }
                        }
                    }

                }
                //val realPass = mAES256.aesDecode(sharedPref.getString(PREF_PASSWORD, ""))
            }
        }
        pinLockView.setPinLockListener(pinLockListener)
        pinLockView.attachIndicatorDots(pinLockIndicator)

    }

    override fun onBackPressed() {
        if (mPasswordType != PasswordIntentType.MainExecute.value) {
            super.onBackPressed()
        }
    }
}
package com.esk.openpadnew

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esk.openpadnew.Util.AES256Util
import kotlinx.android.synthetic.main.activity_password.*

private const val DEFAULT_KEY_CODE: String = "ABCDABCDABCDABCD"

class PasswordActivity : AppCompatActivity() {
    private var mKeyToken: String = ""
    private lateinit var mAES256: AES256Util
    private var mPasswordType: Int = PasswordIntentType.Set.value
    private var mPass: String = ""

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

//        var isRepeat = false
//        val passCodeView: PassCodeView = findViewById(R.id.pass_view)
//        passCodeView.setOnTextChangeListener { pass: String? ->
//            when (mPasswordType) {
//                PasswordIntentType.Execute.value, PasswordIntentType.MainExecute.value -> {
//                    val realPass = mAES256.aesDecode(sharedPref.getString(PREF_PASSWORD, ""))
//                    if (pass?.length == 4) {
//                        if (pass == realPass) {
//                            setResult(Activity.RESULT_OK)
//                            finish()
//                        } else {
//                            passCodeView.reset()
//                            pass_txtComment.text = getString(R.string.password_incorrect)
//                        }
//                    }
//                }
//                else -> {
//                    if (pass?.length == 4) {
//                        if (isRepeat) {
//                            if (pass == mPass) {
//                                val editor: SharedPreferences.Editor = sharedPref.edit()
//                                editor.putString(PREF_PASSWORD, mAES256.aesEncode(pass))
//                                editor.putBoolean(PREF_SET_PASSWORD, true)
//                                editor.apply()
//                                setResult(Activity.RESULT_OK)
//                                finish()
//                            } else {
//                                passCodeView.setError(true)
//                            }
//                        } else {
//                            mPass = pass
//                            passCodeView.reset()
//                            isRepeat = true
//                            pass_txtComment.text = getString(R.string.password_again)
//                        }
//                    }
//                }
//            }
//        }
    }

    override fun onBackPressed() {
        if (mPasswordType != PasswordIntentType.MainExecute.value) {
            super.onBackPressed()
        }
    }
}
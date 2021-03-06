package com.esk.openpadnew

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        supportActionBar?.hide()

        if (savedInstanceState == null) {
            replaceTutorialFragment()
        }
    }


    private fun replaceTutorialFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.tutorial_container, CustomTutorialSupportFragment())
            .commit()
    }


    override fun onBackPressed() {

    }
}
package com.esk.openpadnew

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.cleveroad.slidingtutorial.*

private const val TOTAL_PAGES: Int = 6
private const val REQUEST_CODE_APP_PERMISSION = 10

class CustomTutorialSupportFragment : TutorialSupportFragment(), OnTutorialPageChangeListener {
    private var mPagesColors: IntArray = intArrayOf()
    private val mOnSkipClickListener: View.OnClickListener = View.OnClickListener { _: View? ->
        checkPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_APP_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                val sharedPref: SharedPreferences = requireActivity().getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)
                val prefEdit: SharedPreferences.Editor = sharedPref.edit()
                prefEdit.putBoolean(PREF_FIRST_BOOT, false)
                prefEdit.apply()

                activity?.setResult(RESULT_OK)
                activity?.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mPagesColors.isEmpty()) {
            mPagesColors = intArrayOf(
                ContextCompat.getColor(requireContext(), R.color.materialBlack),
                ContextCompat.getColor(requireContext(), R.color.materialBasilGreen),
                ContextCompat.getColor(requireContext(), R.color.materialBlueBlack),
                ContextCompat.getColor(requireContext(), R.color.defaultColorPrimaryDark),
                ContextCompat.getColor(requireContext(), R.color.materialPurpleBlack),
                ContextCompat.getColor(requireContext(), R.color.materialBrown)
            )
        }
        if (context != null)
            Log.e("Context", (context != null).toString())

        addOnTutorialPageChangeListener(this)
    }

    override fun onPageChanged(position: Int) {
        Log.e("Tutorial", "onPageChanged: position = $position")
    }

    override fun provideTutorialOptions(): TutorialOptions {
        val indicatorOptions: IndicatorOptions = IndicatorOptions.newBuilder(requireContext()).build()
        return newTutorialOptionsBuilder(requireContext())
            .setUseInfiniteScroll(false)
            .setPagesColors(mPagesColors)
            .setPagesCount(TOTAL_PAGES)
            .setTutorialPageProvider(TutorialPagesProvider())
            .setIndicatorOptions(indicatorOptions)
            .setOnSkipClickListener(mOnSkipClickListener)
            .build()
    }

    override fun getLayoutResId(): Int {
        return R.layout.custom_tutorial_layout_ids_example
    }

    override fun getIndicatorResId(): Int {
        return R.id.indicatorCustom
    }

    override fun getSeparatorResId(): Int {
        return R.id.separatorCustom
    }

    override fun getButtonSkipResId(): Int {
        return R.id.tvSkipCustom
    }

    override fun getViewPagerResId(): Int {
        return R.id.viewPagerCustom
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                requireActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (requireActivity().shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_APP_PERMISSION)
                } else {
                    requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_APP_PERMISSION)
                }
            }
        } else {
            val sharedPref: SharedPreferences = requireActivity().getSharedPreferences(APP_PREFERENCE, Context.MODE_PRIVATE)
            val prefEdit: SharedPreferences.Editor = sharedPref.edit()
            prefEdit.putBoolean(PREF_FIRST_BOOT, false)
            prefEdit.apply()
            requireActivity().setResult(RESULT_OK)
            requireActivity().finish()
        }
    }

    private class TutorialPagesProvider : TutorialPageOptionsProvider {
        override fun provide(position: Int): PageOptions {
            @LayoutRes val pageLayoutResId: Int
            val tutorialItems: Array<TransformItem>
            when (position) {
                0   ->  {
                    pageLayoutResId = R.layout.tutorial_page_01
                    tutorialItems = arrayOf(TransformItem.create(R.id.tutorial_page1_image, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page1_title, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page1_contents, Direction.RIGHT_TO_LEFT, 0.2f))
                }
                1   ->  {
                    pageLayoutResId = R.layout.tutorial_page_02
                    tutorialItems = arrayOf(TransformItem.create(R.id.tutorial_page2_image, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page2_title, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page2_contents, Direction.RIGHT_TO_LEFT, 0.2f))
                }
                2   ->  {
                    pageLayoutResId = R.layout.tutorial_page_03
                    tutorialItems = arrayOf(TransformItem.create(R.id.tutorial_page3_image, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page3_title, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page3_contents, Direction.RIGHT_TO_LEFT, 0.2f))
                }
                3   ->  {
                    pageLayoutResId = R.layout.tutorial_page_04
                    tutorialItems = arrayOf(TransformItem.create(R.id.tutorial_page4_image, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page4_title, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page4_contents, Direction.RIGHT_TO_LEFT, 0.2f))
                }
                4   ->  {
                    pageLayoutResId = R.layout.tutorial_page_05
                    tutorialItems = arrayOf(TransformItem.create(R.id.tutorial_page5_image, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page5_title, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page5_contents, Direction.RIGHT_TO_LEFT, 0.2f))
                }
                else -> {
                    pageLayoutResId = R.layout.tutorial_page_06
                    tutorialItems = arrayOf(TransformItem.create(R.id.tutorial_page6_image, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page6_title, Direction.RIGHT_TO_LEFT, 0.2f),
                        TransformItem.create(R.id.tutorial_page6_contents, Direction.RIGHT_TO_LEFT, 0.2f))
                }
            }

            return PageOptions.create(pageLayoutResId, position, *tutorialItems)
        }
    }
}
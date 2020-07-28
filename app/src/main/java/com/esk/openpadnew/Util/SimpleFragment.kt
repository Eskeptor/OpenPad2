package com.esk.openpadnew.Util

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.esk.openpadnew.*
import com.esk.openpadnew.DataType.BrushObject
import io.feeeei.circleseekbar.CircleSeekBar
import kotlinx.android.synthetic.main.fragment_brush.view.*
import kotlinx.android.synthetic.main.fragment_eraser.view.*
import kotlinx.android.synthetic.main.fragment_shape.view.*
import java.util.regex.Pattern

// 브러쉬의 최대 굵기
private const val BRUSH_WIDTH_MAX: Int = 40
// 브러쉬의 최소 굵기
private const val BRUSH_WIDTH_MIN: Int = 1
// 브러쉬 색상의 최대 값
private const val BRUSH_COLOR_MAX: Int = 255
// 브러쉬의 초기 값
private const val BRUSH_BASIC_SIZE: Int = 10
// 브러쉬의 입력값에 숫자만 들어가 있는지 판단을 위한 정규식
private const val STRING_NUMBER_REGEX: String = "^[0-9]*$"

/**
 * 프래그먼트의 타입
 */
enum class FragmentType {
    Brush, Eraser, Shape
}

/**
 * 페인트 메모의 하단부에 들어갈 도구 모음용 프래그먼트
 */
class SimpleFragment : Fragment() {
    private lateinit var mFragmentType: FragmentType            // 프래그먼트의 타입
    private var mPaintActivity: PaintMemoActivity? = null       // Paint 액티비티

    var mCurBrushSize: Int = BRUSH_BASIC_SIZE                   // 현재 브러쉬의 크기
        private set
    var mColorRed: Int = 0                                      // 현재 색상의 Red 값
        private set
    var mColorGreen: Int = 0                                    // 현재 색상의 Green 값
        private set
    var mColorBlue: Int = 0                                     // 현재 색상의 Blue 값
        private set
    var mCurEraserSize: Int = BRUSH_BASIC_SIZE                  // 현재 지우개의 크기
        private set

    companion object {
        /**
         * 프래그먼트를 생성하여 반환하는 함수
         * @param fragmentType 생성할 프래그먼트의 타입
         * @param paintActivity Paint 액티비티
         */
        fun getInstance(fragmentType: FragmentType, paintActivity: PaintMemoActivity): SimpleFragment {
            val fragment = SimpleFragment()
            fragment.mFragmentType = fragmentType
            fragment.mPaintActivity = paintActivity

            LogBot.logName("Debugbug").logLevel(LogBot.Level.Error).log("Fragment Create : $fragmentType")
            return fragment
        }

        /**
         * Pixel 값을 DP 값으로 변환해주는 함수(int 값)
         */
        fun convertPixelsToDpInt(px: Float, context: Context): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.resources.displayMetrics).toInt()

        /**
         * Pixel 값을 DP 값으로 변환해주는 함수(Float 값)
         */
        fun convertPixelsToDpFloat(px: Float, context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.resources.displayMetrics)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = when (mFragmentType) {
            FragmentType.Brush  -> inflater.inflate(R.layout.fragment_brush, null)
            FragmentType.Eraser -> inflater.inflate(R.layout.fragment_eraser, null)
            FragmentType.Shape  -> inflater.inflate(R.layout.fragment_shape, null)
        }

        when (mFragmentType) {
            // 브러쉬를 선택했을 때
            FragmentType.Brush -> {
                val brushWidthImg = view.frag_brush_width_img       // 브러쉬 크기를 나타내는 이미지
                val brushWidthTxt = view.frag_brush_width_txt       // 브러쉬 크기를 숫자로 보여주는 텍스트
                val brushWidthSeek = view.frag_brush_width_seek     // 브러쉬 크기를 조절하는 SeekBar
                val brushRedTxt = view.frag_brush_red_txt           // 브러쉬의 Red 를 숫자로 보여주는 텍스트
                val brushRedSeek = view.frag_brush_red_seek         // 브러쉬의 Red 를 조절하는 SeekBar
                val brushGreenTxt = view.frag_brush_green_txt       // 브러쉬의 Green 를 숫자로 보여주는 텍스트
                val brushGreenSeek = view.frag_brush_green_seek     // 브러쉬의 Green 를 조절하는 SeekBar
                val brushBlueTxt = view.frag_brush_blue_txt         // 브러쉬의 Blue 를 숫자로 보여주는 텍스트
                val brushBlueSeek = view.frag_brush_blue_seek       // 브러쉬의 Blue 를 조절하는 SeekBar

                // 브러쉬의 크기를 조절하는 SeekBar 에 연결할 리스너
                val seekBarChangeListener: SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        val id: Int = seekBar!!.id
                        when (id) {
                            R.id.frag_brush_width_seek -> {
                                // 브러쉬의 크기가 1보다 작으면 1로 고정
                                if (brushWidthSeek.progress < BRUSH_WIDTH_MIN) {
                                    brushWidthSeek.progress = BRUSH_WIDTH_MIN
                                }
                                mCurBrushSize = brushWidthSeek.progress
                                brushWidthTxt.setText(mCurBrushSize.toString())
                            }
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                    // 브러쉬 SeekBar 가 멈추면 브러쉬의 크기 조절
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        changeBrushWidth(brushWidthImg, brushWidthSeek, view, FragmentType.Brush)
                    }
                }

                // 브러쉬의 각 색상 SeekBar 에 연결할 리스너
                val circleSeekBarChangeListener = CircleSeekBar.OnSeekBarChangeListener { seekBar: CircleSeekBar?, i: Int ->
                    when (seekBar) {
                        brushRedSeek -> {
                            brushRedTxt.text = i.toString()
                            mColorRed = i
                            brushWidthImg.setColorFilter(Color.rgb(mColorRed, mColorGreen, mColorBlue), PorterDuff.Mode.SRC)
                        }
                        brushGreenSeek -> {
                            brushGreenTxt.text = i.toString()
                            mColorGreen = i
                            brushWidthImg.setColorFilter(Color.rgb(mColorRed, mColorGreen, mColorBlue), PorterDuff.Mode.SRC)
                        }
                        brushBlueSeek -> {
                            brushBlueTxt.text = i.toString()
                            mColorBlue = i
                            brushWidthImg.setColorFilter(Color.rgb(mColorRed, mColorGreen, mColorBlue), PorterDuff.Mode.SRC)
                        }
                    }
                    val message = Message()
                    message.what = HANDLER_CHANGE_COLOR
                    message.obj = Color.rgb(mColorRed, mColorGreen, mColorBlue)
                    mPaintActivity?.paintHandler?.sendMessage(message)
                }

                // 현재 브러쉬의 크기는 mCurBrushSize
                brushWidthSeek.progress = mCurBrushSize
                brushWidthTxt.setText(brushWidthSeek.progress.toString())
                brushWidthTxt.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        // 공백일 경우에 Exception 이 나오므로
                        // 공백이 될 경우에 브러쉬 크기를 기본크기로 고정
                        try {
                            mCurBrushSize = s.toString().toInt()
                        } catch (e: Exception) {
                            mCurBrushSize = BRUSH_BASIC_SIZE
                        }
                        changeBrushWidth(brushWidthImg, brushWidthSeek, view, FragmentType.Brush)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // 브러쉬 크기 입력창에 숫자를 넣었나 확인
                        val match = Pattern.matches(STRING_NUMBER_REGEX, s)
                        if (match) {
                            // 공백일 경우에 Exception 이 나오므로
                            // 공백이 될 경우에 브러쉬 크기를 기본크기로 고정
                            try {
                                val value = s.toString().toInt()
                                // 브러쉬 크기가 0 이상 BRUSH_COLOR_MAX 이하인지 확인
                                if (value in 0..BRUSH_COLOR_MAX) {
                                    brushWidthSeek.progress = value
                                } else {
                                    brushWidthTxt.setText(BRUSH_BASIC_SIZE.toString())
                                }
                            } catch (_: Exception) {
                                brushWidthTxt.setText(BRUSH_BASIC_SIZE.toString())
                            }

                        } else {
                            brushWidthTxt.setText(BRUSH_BASIC_SIZE.toString())
                        }
                    }
                })

                brushWidthSeek.setOnSeekBarChangeListener(seekBarChangeListener)
                brushRedSeek.setOnSeekBarChangeListener(circleSeekBarChangeListener)
                brushGreenSeek.setOnSeekBarChangeListener(circleSeekBarChangeListener)
                brushBlueSeek.setOnSeekBarChangeListener(circleSeekBarChangeListener)
                brushWidthSeek.max = BRUSH_WIDTH_MAX
                brushRedSeek.maxProcess = BRUSH_COLOR_MAX
                brushBlueSeek.maxProcess = BRUSH_COLOR_MAX
                brushGreenSeek.maxProcess = BRUSH_COLOR_MAX
                brushRedSeek.curProcess = mColorRed
                brushGreenSeek.curProcess = mColorGreen
                brushBlueSeek.curProcess = mColorBlue
                brushRedTxt.text = brushRedSeek.curProcess.toString()
                brushGreenTxt.text = brushGreenSeek.curProcess.toString()
                brushGreenTxt.text = brushGreenSeek.curProcess.toString()

                changeBrushWidth(brushWidthImg, brushWidthSeek, view, FragmentType.Brush)
            }
            // 지우개를 선택했을 때
            FragmentType.Eraser -> {
                val eraserWidthImg = view.frag_eraser_width_img
                val eraserWidthTxt = view.frag_eraser_width_txt
                val eraserWidthSeek = view.frag_eraser_width_seek

                val seekBarChangeListener: SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        val id: Int = seekBar!!.id
                        when (id) {
                            R.id.frag_brush_width_seek -> {
                                // 브러쉬의 크기가 1보다 작으면 1로 고정
                                if (eraserWidthSeek.progress < BRUSH_WIDTH_MIN) {
                                    eraserWidthSeek.progress = BRUSH_WIDTH_MIN
                                }
                                mCurBrushSize = eraserWidthSeek.progress
                                eraserWidthTxt.setText(mCurBrushSize.toString())
                            }
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                    // 브러쉬 SeekBar 가 멈추면 브러쉬의 크기 조절
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        changeBrushWidth(eraserWidthImg, eraserWidthSeek, view, FragmentType.Eraser)
                    }
                }

                eraserWidthSeek.setOnSeekBarChangeListener(seekBarChangeListener)
                eraserWidthSeek.progress = mCurEraserSize
                eraserWidthSeek.max = BRUSH_WIDTH_MAX
                eraserWidthTxt.setText(eraserWidthSeek.progress.toString())
                eraserWidthTxt.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        // 공백일 경우에 Exception 이 나오므로
                        // 공백이 될 경우에 브러쉬 크기를 기본크기로 고정
                        try {
                            mCurBrushSize = s.toString().toInt()
                        } catch (e: Exception) {
                            mCurBrushSize = BRUSH_BASIC_SIZE
                        }
                        changeBrushWidth(eraserWidthImg, eraserWidthSeek, view, FragmentType.Eraser)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // 브러쉬 크기 입력창에 숫자를 넣었나 확인
                        val match = Pattern.matches(STRING_NUMBER_REGEX, s)
                        if (match) {
                            // 공백일 경우에 Exception 이 나오므로
                            // 공백이 될 경우에 브러쉬 크기를 기본크기로 고정
                            try {
                                val value = s.toString().toInt()
                                // 브러쉬 크기가 0 이상 BRUSH_COLOR_MAX 이하인지 확인
                                if (value in 0..BRUSH_COLOR_MAX) {
                                    eraserWidthSeek.progress = value
                                } else {
                                    eraserWidthTxt.setText(BRUSH_BASIC_SIZE.toString())
                                }
                            } catch (_: Exception) {
                                eraserWidthTxt.setText(BRUSH_BASIC_SIZE.toString())
                            }

                        } else {
                            eraserWidthTxt.setText(BRUSH_BASIC_SIZE.toString())
                        }
                    }
                })
            }
            // 도형을 선택했을 때
            FragmentType.Shape -> {
                val shapeCircle = view.frag_shape_circle
                val shapeRectangle = view.frag_shape_rectangle
                val onClickListener = View.OnClickListener { v: View? ->
                    val id: Int = v!!.id
                    when (id) {
                        R.id.frag_shape_circle -> {
                            val message = Message()
                            message.what = HANDLER_CHANGE_BRUSH_TYPE
                            message.obj = BrushObject.ShapeType.Circle
                            mPaintActivity?.paintHandler?.sendMessage(message)
                        }
                        R.id.frag_shape_rectangle -> {
                            val message = Message()
                            message.what = HANDLER_CHANGE_BRUSH_TYPE
                            message.obj = BrushObject.ShapeType.Rectangle
                            mPaintActivity?.paintHandler?.sendMessage(message)
                        }
                    }
                }
                shapeCircle.setOnClickListener(onClickListener)
                shapeRectangle.setOnClickListener(onClickListener)
            }
        }

        return view
    }


    /**
     * 브러쉬의 굵기를 변경하는 함수
     * @param type 프래그먼트의 타입
     * @param view 뷰
     * @param brushWidthImg 브러쉬의 굵기를 시각적으로 보여줄 ImageView
     * @param brushWidthSeek 브러쉬의 굵기를 조절하는 SeekBar
     */
    private fun changeBrushWidth(brushWidthImg: ImageView, brushWidthSeek: SeekBar, view: View, type: FragmentType) {
        val params: LinearLayout.LayoutParams
                = LinearLayout.LayoutParams(brushWidthImg.width, convertPixelsToDpInt(brushWidthSeek.progress.toFloat(), view.context))
        params.weight = 1f
        params.gravity = Gravity.CENTER_VERTICAL
        brushWidthImg.layoutParams = params
        if (type == FragmentType.Brush) {
            brushWidthImg.setColorFilter(Color.rgb(mColorRed, mColorGreen, mColorBlue), PorterDuff.Mode.SRC)
        }

        val message = Message()
        message.what = HANDLER_CHANGE_BRUSH_SIZE
        message.obj = convertPixelsToDpInt(brushWidthSeek.progress.toFloat(), view.context)
        mPaintActivity?.paintHandler?.sendMessage(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogBot.logName("Debugbug").logLevel(LogBot.Level.Error).log("Fragment Create : $mFragmentType")
        mPaintActivity = null
    }
}
package com.glen519.animExample

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.text.isDigitsOnly
import com.mymoney.biz.accessibleaddtrans.VolumeWaveDrawable
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private val  mMainHandler = Handler(Looper.getMainLooper())

    private var changeValue = 0F
    private var targetValue = 0F
    private var stepValue = 0F
    private var up = true
    val drawable = VolumeWaveDrawable().setMajorLineColor(Color.RED, Color.GREEN)
//        .setMinorLineColor(Color.BLUE, Color.YELLOW)
    private val mRunnable = object: Runnable {
        override fun run() {
            if (targetValue >= 0) {
                if (changeValue - targetValue >= stepValue) {
                    changeValue -= stepValue
                } else if (targetValue - changeValue > stepValue) {
                    changeValue += stepValue
                } else {
                    targetValue = -1F

                }
            } else {
                targetValue = -1F
                if(up) {
                    changeValue ++
                    if(changeValue >= 1) {
                        up = false
                    }
                } else {
                    changeValue --
                    if(changeValue <= 0) {
                        up = true
                    }
                }
            }

            drawable.setWaveLevel(changeValue/50F)
            mMainHandler.postDelayed(this, 10L)
            Log.i("abwbw101","target $targetValue  change $changeValue")

        }


    }

    private fun launchValue(value: Int) {
        targetValue = value.toFloat()
        stepValue = abs(targetValue - changeValue) /10F
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        image_iv.background = drawable
//        mMainHandler.post(mRunnable)
        drawable.start()

        launchBtn.setOnClickListener {
            val text = textEt.text.toString()
            if(text.isNotEmpty() && text.isDigitsOnly()) {
                drawable.setWaveLevel(text.toFloat() / 50F)
//                launchValue(text.toInt())
            }
        }
    }
}

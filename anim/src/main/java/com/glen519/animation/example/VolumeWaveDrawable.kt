package com.glen519.animation.example

import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import androidx.annotation.ColorInt
import kotlin.math.*

/**
 * 音频衰减动画
 */
class VolumeWaveDrawable : Drawable(), Runnable, Animatable {

    private val idleAmplitude : Float = 0.01F
    private val frequency = 1.2F
    private val density = 1.0F
    private val majorPainter = Paint(Paint.ANTI_ALIAS_FLAG)
    private val minorPainter = Paint(Paint.ANTI_ALIAS_FLAG)
    private var majorLineWidth = 2F
    private var minorLineWidth = 1F

    private var majorLineColor = Color.TRANSPARENT
    private var majorLineStartColor = Color.TRANSPARENT
    private var majorLineEndColor = Color.TRANSPARENT

    private var minorLineColor = Color.TRANSPARENT
    private var minorLineStartColor = Color.TRANSPARENT
    private var minorLineEndColor = Color.TRANSPARENT


    @Volatile
    private var isRunning = false
    private var phase: Float = 0F
    private var amplitude: Float = 1F
    private var maxAmplitude: Float = 0F
    private var waveHeight: Float = 0F
    private var waveWidth : Float = 0F
    private var waveMid: Float = 0F
    private var numberOfWaves = 5
    private var phaseShift = -0.25F
    @Volatile
    private var targetLevel : Float = 0.0F
    @Volatile
    private var stepLevel : Float = 0.0F
    @Volatile
    private var needChangeLevel : Boolean = false
    @Volatile
    private var level: Float = 0.0F
    @Volatile
    private var minLevel: Float = 0.01F
    private var waveBufferTime: Long = 100 // 默认100毫秒曲线缓冲
    private var renderTimeUnit: Long = 1000 / 60L
    private var waveLines = arrayListOf<Path>()

    init {
        init()
    }

    fun setWaveLevel(level: Float) : VolumeWaveDrawable {
        if(level < 0.1F) {
            return this
        }
        this.targetLevel = level * 0.7F
        // 16 毫秒一次，这里分割60份相当于 60 * 16 为一个缓冲时间
        this.stepLevel = (targetLevel - this.level) / (waveBufferTime / renderTimeUnit)
        if(abs(stepLevel) < minLevel) {
            stepLevel = if(stepLevel < 0.00001) -minLevel else minLevel
        }
        needChangeLevel = true
        setWaveLevelInternal(level)
        return this
    }

    fun setWaveMajorLineWidth(width: Float): VolumeWaveDrawable {
        majorPainter.strokeWidth = width
        return this
    }

    fun setWaveMinorLineWidth(width: Float): VolumeWaveDrawable {
        minorPainter.strokeWidth = width
        return this
    }

    private fun setWaveLevelInternal(level: Float) {
        this.phase += phaseShift
        this.amplitude = max(level, idleAmplitude)
        invalidateSelf()
    }


    private fun init() {
        numberOfWaves = 5
        phaseShift = -0.25F

        waveHeight = bounds.height().toFloat()
        waveWidth = bounds.width().toFloat()

        waveMid = waveWidth / 2.0F
        maxAmplitude = waveHeight - 4.0F

        majorPainter.style = Paint.Style.STROKE
        majorPainter.strokeWidth = majorLineWidth

        minorPainter.style = Paint.Style.STROKE
        minorPainter.strokeWidth = minorLineWidth
    }

    fun setMajorLineWidth(width: Float): VolumeWaveDrawable {
        majorPainter.strokeWidth = width
        invalidateSelf()
        return this
    }

    fun setMinorLineWidth(width: Float): VolumeWaveDrawable {
        minorPainter.strokeWidth = width
        invalidateSelf()
        return this
    }

    fun setMajorLineColor(@ColorInt color: Int): VolumeWaveDrawable {
        majorLineColor = color
        majorLineStartColor = Color.TRANSPARENT
        majorLineEndColor = Color.TRANSPARENT
        majorPainter.color = color
        invalidateSelf()
        return this
    }

    fun setMajorLineColor(@ColorInt fromColor: Int, @ColorInt toColor: Int): VolumeWaveDrawable {
        majorLineColor = Color.TRANSPARENT
        majorLineStartColor = fromColor
        majorLineEndColor = toColor
        invalidateSelf()
        return this
    }

    fun setMinorLineWidth(@ColorInt color: Int): VolumeWaveDrawable {
        minorLineColor = color
        minorLineStartColor = Color.TRANSPARENT
        minorLineEndColor = Color.TRANSPARENT
        minorPainter.color = color
        invalidateSelf()
        return this
    }

    fun setMinorLineColor(@ColorInt fromColor: Int, @ColorInt toColor: Int): VolumeWaveDrawable {
        minorLineColor = Color.TRANSPARENT
        minorLineStartColor = fromColor
        minorLineEndColor = toColor
        invalidateSelf()
        return this
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)

        if(minorLineColor != Color.TRANSPARENT) {
            minorPainter.color = minorLineColor
        }

        if(minorLineStartColor != Color.TRANSPARENT && minorLineEndColor != Color.TRANSPARENT) {
            minorPainter.shader = LinearGradient(
                0F,
                waveHeight / 2,
                waveWidth,
                waveHeight / 2,
                minorLineStartColor,
                minorLineEndColor,
                Shader.TileMode.CLAMP
            )
        }

        if(majorLineColor != Color.TRANSPARENT) {
            majorPainter.color = majorLineColor
        }

        if(majorLineStartColor != Color.TRANSPARENT && majorLineEndColor != Color.TRANSPARENT) {
            majorPainter.shader = LinearGradient(
                0F,
                waveHeight / 2,
                waveWidth,
                waveHeight / 2,
                majorLineStartColor,
                majorLineEndColor,
                Shader.TileMode.CLAMP
            )
        }
    }


    private fun measure() {
        waveHeight = bounds.height().toFloat()
        waveWidth = bounds.width().toFloat()
        waveMid = waveWidth / 2.0F
        maxAmplitude = waveHeight - 4.0F

        waveLines.clear()
        for(index in 0 until numberOfWaves){

            val progress = 1.0F - index.toFloat() / numberOfWaves.toFloat()
            val normedAmplitude = (1.5F * progress - 0.5F) * amplitude
            val path = Path()
            for(x in 0..((waveWidth + density).toInt()) step density.toInt() ) {
                val scaling = -(x / waveMid.toDouble() - 1.0).pow(2.0) + 1.0
                val y = scaling * maxAmplitude * normedAmplitude * sin(2.0 * Math.PI * (x.toDouble() / waveWidth) * frequency + phase) + (waveHeight * 0.5)

                if(x == 0) {
                    path.moveTo(x.toFloat(), y.toFloat())
                } else {
                    path.lineTo(x.toFloat(), y.toFloat())
                }
            }
            waveLines.add(path)
        }
    }


    override fun draw(canvas: Canvas) {
        measure()
        for(index in waveLines.indices) {
            var painter = minorPainter
            if(index == 0) {
                painter = majorPainter
            }
            canvas.drawPath(waveLines[index], painter)
        }
    }

    override fun setAlpha(alpha: Int) {
        // no support
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // no support
    }

    override fun run() {
        if(needChangeLevel) {
            if(abs(level - targetLevel) < stepLevel) {
                // 达到目标值，进行恢复
                this.targetLevel = level
                this.stepLevel = (level - targetLevel) / 10F
                needChangeLevel = false
            } else {
                level += stepLevel
            }
        } else {
            if(level < minLevel) {
                level += minLevel
                stepLevel = minLevel
            } else {
                level -= minLevel
            }
        }
        setWaveLevelInternal(level)
        scheduleSelf(this, SystemClock.uptimeMillis() + renderTimeUnit)
    }

    override fun isRunning(): Boolean = isRunning

    override fun start() {
        scheduleSelf(this, 0)
        isRunning = true
    }

    override fun stop() {
        isRunning = false
        unscheduleSelf(this)
    }

}
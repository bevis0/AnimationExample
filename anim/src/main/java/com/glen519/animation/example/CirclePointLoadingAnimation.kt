package com.glen519.animation.example

import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.AnimationUtils

/**
 * 原地圆圈loading
 */
class CirclePointLoadingAnimation : Drawable(), Animatable, Runnable {
    override fun start() {
        next()
        isRunning = true
    }

    private fun next() {
        scheduleSelf(this, 0)
    }

    override fun stop() {
        unscheduleSelf(this)
        isRunning = false
    }

    private var isRunning = false

    override fun isRunning(): Boolean = isRunning

    override fun run() {
        val time = AnimationUtils.currentAnimationTimeMillis()
        val lastScheduleFactory = scheduleFactory
        // 开始为0 或者 已经结束则重置状态
        if(startTime == 0L || lastScheduleFactory >= 1) {
            startTime =  time
        }
        scheduleFactory = (time - startTime) / timePeriod.toFloat()
        if(scheduleFactory > 1F) {
            scheduleFactory = 1F
        }

        invalidateSelf()

        next()
    }

    data class CirCleNode(
        var x: Int = 0,
        var y: Int = 0,
        var radius: Int = 0,
        var fillColor: Int = 0
    )

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        val result = super.setVisible(visible, restart)
        if(visible) {
            if(result) {
                start()
            }
        } else {
            stop()
        }
        return result
    }

    private var circleNodes = mutableListOf(CirCleNode(), CirCleNode(), CirCleNode())
    private var scheduleFactory = 0F
    private val circlePaint = Paint()
    private var startTime = 0L
    private var timePeriod = 500L

    init {
        circlePaint.style = Paint.Style.FILL_AND_STROKE
        circlePaint.isAntiAlias = true
        circlePaint.color = Color.BLACK
    }

    private fun measure() {
        val bounds = bounds
        var left = bounds.left
        var right = bounds.right
        var top = bounds.top
        var bottom = bounds.bottom

        val canvasHeight = bounds.height()
        val canvasWidth = bounds.width()

        val maxHeightRadius = canvasHeight / 2
        val maxWidthRadius = canvasWidth / 6
        val maxRadius = if (maxHeightRadius > maxWidthRadius) {
            val padding = (canvasHeight - maxWidthRadius * 2) / 2
            top += padding
            bottom -= padding
            maxWidthRadius
        } else {
            val padding = (canvasWidth - maxHeightRadius * 2) / 2
            left += padding
            right -= padding
            maxHeightRadius
        }

        val drawableHeight = bottom - top
        val drawableWidth = right - left

        // 不能超出边界 不要进位 地板除
        val minRadius = (maxRadius / 2F).toInt()

        circleNodes[0].apply {
            x = left + minRadius +  ((drawableWidth / 2F - minRadius) * scheduleFactory).toInt()
            y = top + (drawableHeight / 2F).toInt()
            radius = ((maxRadius - minRadius) * scheduleFactory + minRadius).toInt()
        }

        circleNodes[1].apply {
            x = left + (drawableWidth / 2F + ((drawableWidth / 2F - minRadius) * scheduleFactory)).toInt()
            y = top + (drawableHeight / 2F).toInt()
            radius = (maxRadius - (maxRadius - minRadius) * scheduleFactory ).toInt()
        }

        circleNodes[2].apply {
            x = left + ((drawableWidth - minRadius - (drawableWidth - 2 * minRadius) * scheduleFactory )).toInt()
            y = top + (drawableHeight / 2F).toInt()
            radius = minRadius
        }

    }

    override fun draw(canvas: Canvas) {
        measure()
        for(circleNode in circleNodes) {
            canvas.drawCircle(circleNode.x.toFloat(), circleNode.y.toFloat(), circleNode.radius.toFloat(), circlePaint)
        }
    }

    /**
     * no support
     */
    override fun setAlpha(alpha: Int) {}

    /**
     * no support
     */
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    /**
     * no support
     */
    override fun setColorFilter(colorFilter: ColorFilter?) {}

}
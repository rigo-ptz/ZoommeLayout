package com.jollypanda.zoommelayout

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.LinearLayout

/**
 * @author Yamushev Igor
 * @since  09.06.17
 */
class ZommeLayout @JvmOverloads constructor(context: Context,
                                            attrs: AttributeSet? = null,
                                            defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), ScaleGestureDetector.OnScaleGestureListener {

    enum class State {
        CALM,
        DRAG,
        ZOOM
    }

    val MIN_ZOOM = 1f
    val MAX_ZOOM = 5f

    var state = State.CALM

    var scale = 1f
    var previousScaleFactor = 0f

    private var startX = 0f
    private var startY = 0f

    private var dx = 0f
    private var dy = 0f
    private var prevDx = 0f
    private var prevDy = 0f

    private val child: View by lazy { getChildAt(0) }

    init {
        setListeners()
    }

    private fun setListeners() {
        val scaleDetector = ScaleGestureDetector(context, this)
        setOnTouchListener { _, motionEvent ->
            motionEvent.let {
                when (motionEvent.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = motionEvent.x - prevDx
                        startY = motionEvent.y - prevDy
                    }

                    MotionEvent.ACTION_MOVE -> {
                        dx = motionEvent.x - startX
                        dy = motionEvent.y - startY


                        if (state == State.ZOOM) {

                        } else {

                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        prevDx = dx
                        prevDy = dy

                        if (Math.abs(dx) > 3 || Math.abs(dy) > 3)
                            return@let

                        if (state == State.ZOOM)
                            return@let

                        // TODO try to get layout here
                        state = State.CALM
                    }

                    MotionEvent.ACTION_POINTER_DOWN -> {
                        state = State.ZOOM
                    }

                    MotionEvent.ACTION_POINTER_UP -> {
                        state = State.DRAG
                    }

                    else -> {  }
                }
            }

            scaleDetector.onTouchEvent(motionEvent)

            if (state == State.DRAG && scale >= MIN_ZOOM || state == State.ZOOM) {
                parent.requestDisallowInterceptTouchEvent(true)
                val maxDx = (child.width - child.width / scale) / 2 * scale
                val maxDy = (child.height - child.height / scale) / 2 * scale
                dx = Math.min(Math.max(dx, -maxDx), maxDx)
                dy = Math.min(Math.max(dy, -maxDy), maxDy)
                applyScaleAndTranslation()
            }

            return@setOnTouchListener true
        }
    }

    private fun applyScaleAndTranslation() {
        child.scaleX = scale
        child.scaleY = scale
        child.translationX = dx
        child.translationY = dy
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?) = true

    override fun onScaleEnd(detector: ScaleGestureDetector?) {}

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val scaleFactor = detector?.scaleFactor ?: 1f
        if (previousScaleFactor == 0f || Math.signum(scaleFactor) == Math.signum(previousScaleFactor)) {
            scale *= scaleFactor
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM))
            previousScaleFactor = scaleFactor
        } else {
            previousScaleFactor = 0f
        }
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }
}
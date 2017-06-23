package com.jollypanda.zoommelayout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
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
    : LinearLayout(context, attrs, defStyleAttr), ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnGestureListener {

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

    val screenHeight by lazy { resources.displayMetrics.heightPixels }

    val screenWidth by lazy { resources.displayMetrics.widthPixels }

    init {
        setListeners()
    }

    private fun setListeners() {
        val scaleDetector = ScaleGestureDetector(context, this)
        val gestureDetector = GestureDetector(context, this)
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
                    }

                    MotionEvent.ACTION_UP -> {
                        prevDx = dx
                        prevDy = dy
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



//            if (state == State.DRAG && scale >= MIN_ZOOM || state == State.ZOOM) {
                parent.requestDisallowInterceptTouchEvent(true)
                val maxDx = Math.abs((screenWidth - child.width * scale) / 2)
                val maxDy = Math.abs((screenHeight - child.height * scale) / 2)
                dx = Math.min(Math.max(dx, -maxDx), maxDx)
                dy = Math.min(Math.max(dy, -maxDy), maxDy)
            if (dy > 0)
                dy = 0f
            Log.e("MOTION EVENT", "maxDx = $maxDx maxDy = $maxDy dx = $dx dy = $dy")
//                applyScaleAndTranslation()
//            }

            scaleDetector.onTouchEvent(motionEvent)
            gestureDetector.onTouchEvent(motionEvent)

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
        Log.e("EVENT", "ON SCALE")
//        val scaleFactor = detector?.scaleFactor ?: 1f
//        if (previousScaleFactor == 0f || Math.signum(scaleFactor) == Math.signum(previousScaleFactor)) {
//            scale *= scaleFactor
//            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM))
//            previousScaleFactor = scaleFactor
//        } else {
//            previousScaleFactor = 0f
//        }
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        Log.e("EVENT", "SINGLE TAP UP")
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        Log.e("EVENT", "ON DOWN")
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        Log.e("EVENT", "ON FLING")
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        Log.e("EVENT", "ON SCROLL : dx = $dx dy = $dy distX = $distanceX distY = $distanceY")
        child.translationX = dx
        child.translationY = dy
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {
        Log.e("EVENT", "ON LONG PRESS")
    }
}
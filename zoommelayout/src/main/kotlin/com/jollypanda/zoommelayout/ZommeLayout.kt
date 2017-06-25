package com.jollypanda.zoommelayout

import android.content.Context
import android.graphics.Rect
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.util.Log
import android.view.*
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
        SCROLL,
        SCALE,
        SINGLE_TOUCH
    }

    val MIN_ZOOM = 1f
    val MAX_ZOOM = 5f

    var state = State.CALM

    var scale = 1f
    var previousScaleFactor = 0f

    var touchInterceptX = 0f
    var touchInterceptY = 0f
    private var startX = 0f
    private var startY = 0f

    private var dx = 0f
    private var dy = 0f
    private var prevDx = 0f
    private var prevDy = 0f

    private var topOffsetAfterScale = 0
    private var bottomOffsetAfterScale = 0

    private val child: View by lazy { getChildAt(0) }
    private val childInitHeight: Int by lazy { getChildAt(0).height }
    private val childInitWidth: Int by lazy { getChildAt(0).width }

    val thisHeight by lazy { height }

    val thisWidth by lazy { width }

    var vc = ViewConfiguration.get(context)
    private val scaledTouchSlop = vc.scaledTouchSlop
    private val minFlingVelocity = vc.scaledMinimumFlingVelocity
    private val maxFlingVelocity = vc.scaledMaximumFlingVelocity


    init {
        setListeners()
    }

    private var scaleWasDone = false

    private fun setListeners() {
        val scaleDetector = ScaleGestureDetector(context, this)
        val gestureDetector = GestureDetector(context, this)
        setOnTouchListener { _, motionEvent ->
            motionEvent.let {
                when (motionEvent.actionMasked) {
                   /* MotionEvent.ACTION_DOWN ->  handled in Intercept */

                    MotionEvent.ACTION_MOVE -> {
//                           Log.e("LISTENER", "ACTION_MOVE")
                            dx = motionEvent.x - startX
                            dy = motionEvent.y - startY
                            Log.e("XXX", "event_x = ${motionEvent.x} startY = ${startX} dy = $dx")
                            Log.e("YYY", "event_y = ${motionEvent.y} startY = ${startY} dy = $dy")
                    }

                    MotionEvent.ACTION_UP -> {
//                        Log.e("LISTENER", "ACTION_UP")
                        prevDx = dx
                        prevDy = dy
                        state = State.CALM
                    }

                    MotionEvent.ACTION_POINTER_DOWN -> {
//                        Log.e("LISTENER", "ACTION_POINTER_DOWN")
                        state = State.SCALE
                    }

                    MotionEvent.ACTION_POINTER_UP -> {
//                        Log.e("LISTENER", "ACTION_POINTER_UP")
                        state = State.SCROLL
                        scaleWasDone = false
                    }

                    else -> {  }
                }
            }
            calculateState()
            scaleDetector.onTouchEvent(motionEvent)
            gestureDetector.onTouchEvent(motionEvent)

            return@setOnTouchListener true
        }
    }

    private fun calculateState() {
        val (topOffset, bottomOffset) = getOffsetRelativeToThis()

        val maxDx = Math.abs((thisWidth - childInitWidth * scale) / 2)
        dx = Math.min(Math.max(dx, -maxDx), maxDx)
        Log.e("XXX", "maxDx = $maxDx  dx = $dx")

        Log.e("YYY", "screenHeight = ${thisHeight}  childInitHeight = $childInitHeight}")
        val maxDy = Math.abs((thisHeight  - childInitHeight * scale) / 2)
        val maxInitDy = Math.abs((thisHeight - childInitHeight) / 2f)

        Log.e("YYY", "maxDy = $maxDy  dy = $dy maxDy - dy = ${maxDy - Math.abs(dy)} maxInitDy = ${maxInitDy} ")
        Log.e("YYY", "maxDy - maxInitDy = ${maxDy - maxInitDy}")

        if (dy + maxInitDy > maxDy)
            dy = maxDy - maxInitDy

        if (dy + maxInitDy < maxDy * -1)
            dy = -maxDy - maxInitDy

    }

    private fun getOffsetRelativeToThis(): Pair<Int, Int> {
        val offsetViewBounds = Rect()
        child.getHitRect(offsetViewBounds)
        offsetDescendantRectToMyCoords(child, offsetViewBounds)
        val relativeTop = offsetViewBounds.top
        val relativeBottom = offsetViewBounds.bottom
        return Pair(relativeTop, relativeBottom)
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?) = true

    override fun onScaleEnd(detector: ScaleGestureDetector?) {}

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
//        Log.e("EVENT", "ON SCALE")
        state = State.SCALE
        val scaleFactor = detector?.scaleFactor ?: 1f
        if (previousScaleFactor == 0f || Math.signum(scaleFactor) == Math.signum(previousScaleFactor)) {
            scale *= scaleFactor
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM))
            previousScaleFactor = scaleFactor
        } else {
            previousScaleFactor = 0f
        }
        child.scaleX = scale
        child.scaleY = scale
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
//        Log.e("EVENT", "ON INTERCEPT TOUCH")
        val event = MotionEventCompat.getActionMasked(ev)

        if (event == MotionEvent.ACTION_DOWN)  {
            touchInterceptX = ev.x
            touchInterceptY = ev.y

            startX = ev.x - prevDx
            startY = ev.y - prevDy
        }

        if (event == MotionEvent.ACTION_MOVE) {
            if (state == State.SCROLL)
                return true

            val dInterceptX = ev.x - touchInterceptX
            val dInterceptY = ev.y - touchInterceptY
            if (Math.abs(dInterceptX) > scaledTouchSlop || Math.abs(dInterceptY) > scaledTouchSlop) {
                state = State.SCROLL
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            } else {
                state = State.CALM
                parent.requestDisallowInterceptTouchEvent(false)
                return false
            }
        }
        if (event == MotionEvent.ACTION_POINTER_DOWN) {
            state = State.SCALE
            parent.requestDisallowInterceptTouchEvent(true)
            return true
        }
        if (event == MotionEvent.ACTION_POINTER_UP) {
            state = State.SCROLL
            parent.requestDisallowInterceptTouchEvent(true)
            return true
        }
        parent.requestDisallowInterceptTouchEvent(false)
        return false
    }

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(event: MotionEvent?): Boolean {
//        Log.e("EVENT", "SINGLE TAP UP")
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
//        Log.e("EVENT", "ON DOWN")
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        state = State.SCROLL
//        Log.e("EVENT", "ON FLING")
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
//        Log.e("EVENT", "ON SCROLL : dx = $dx dy = $dy distX = $distanceX distY = $distanceY")
        state = State.SCROLL
        child.translationX = dx
        child.translationY = dy
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {
//        Log.e("EVENT", "ON LONG PRESS")
    }
}
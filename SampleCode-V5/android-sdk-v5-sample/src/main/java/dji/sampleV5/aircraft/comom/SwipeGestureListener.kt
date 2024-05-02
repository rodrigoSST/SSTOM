package dji.sampleV5.aircraft.comom

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class SwipeGestureListener(private val context: Context,  private val onSwipeRight: () -> Unit, private val onSwipeLeft: () -> Unit) : View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val deltaX = e2.x - (e1?.x ?: 0f)
            if (deltaX > 0) {
                onSwipeRight.invoke() // Detectou um swipe para a direita
            } else {
                onSwipeLeft.invoke() // Detectou um swipe para a esquerda
            }
            return true
        }
    })

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }
}
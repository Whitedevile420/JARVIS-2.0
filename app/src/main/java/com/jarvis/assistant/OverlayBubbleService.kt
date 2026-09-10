package com.jarvis.assistant

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

class OverlayBubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null

    private var initialX = 0
    private var initialY = 0
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isDragging = false

    override fun onCreate() {
        super.onCreate()
        addBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun addBubble() {

        if (bubbleView != null) {
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val bubble = ImageView(this)
        bubble.setImageResource(android.R.drawable.ic_btn_speak_now)
        bubble.setBackgroundColor(0x992196F3.toInt())
        bubble.setPadding(30, 30, 30, 30)

        val layoutFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 300

        bubble.setOnTouchListener { view, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    initialX = params.x
                    initialY = params.y
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    isDragging = false

                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    val dx = event.rawX - touchStartX
                    val dy = event.rawY - touchStartY

                    if (
                        Math.abs(dx) > 15 ||
                        Math.abs(dy) > 15
                    ) {
                        isDragging = true
                    }

                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()

                    try {
                        windowManager?.updateViewLayout(bubble, params)
                    } catch (e: Exception) {
                    }

                    true
                }

                MotionEvent.ACTION_UP -> {

                    if (!isDragging) {
                        onBubbleTapped()
                    }

                    true
                }

                else -> false
            }
        }

        try {
            windowManager?.addView(bubble, params)
            bubbleView = bubble
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun onBubbleTapped() {

        val intent = Intent(this, JarvisVoiceService::class.java)

        if (JarvisVoiceService.isRunning) {

            intent.action = JarvisVoiceService.ACTION_LISTEN_NOW
            startService(intent)

        } else {

            intent.action = JarvisVoiceService.ACTION_START

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    override fun onDestroy() {

        try {
            bubbleView?.let {
                windowManager?.removeView(it)
            }
        } catch (e: Exception) {
        }

        bubbleView = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
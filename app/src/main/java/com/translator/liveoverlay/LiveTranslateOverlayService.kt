package com.translator.liveoverlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.google.mlkit.nl.translate.*

class LiveTranslateOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayCanvasLayout: FrameLayout? = null
    private var floatingSwitchView: View? = null
    private var isTranslationActive = true

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 1. 터치 투과 레이어 생성 (뒤의 앱 터치/스크롤을 전혀 방해하지 않음)
        initTransparentOverlayLayer()

        // 2. 화면에 띄울 미니 ON/OFF 플로팅 스위치
        initFloatingSwitch()
    }

    private fun initTransparentOverlayLayer() {
        overlayCanvasLayout = FrameLayout(this)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            // FLAG_NOT_TOUCHABLE: 사용자의 터치를 받지 않고 뒷 앱으로 그대로 통과
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(overlayCanvasLayout, params)
    }

    private fun initFloatingSwitch() {
        val btn = Button(this).apply {
            text = "번역 ON"
            textSize = 11f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#4F46E5"))
            setPadding(16, 8, 16, 8)
        }

        val switchParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        btn.setOnClickListener {
            isTranslationActive = !isTranslationActive
            if (isTranslationActive) {
                btn.text = "번역 ON"
                btn.setBackgroundColor(Color.parseColor("#4F46E5"))
            } else {
                btn.text = "번역 OFF"
                btn.setBackgroundColor(Color.DKGRAY)
                overlayCanvasLayout?.removeAllViews()
            }
        }

        // 플로팅 버튼 드래그 이동
        btn.setOnTouchListener(object : View.OnTouchListener {
            private var initX = 0; private var initY = 0
            private var touchX = 0f; private var touchY = 0f
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = switchParams.x; initY = switchParams.y
                        touchX = event.rawX; touchY = event.rawY
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        switchParams.x = initX + (event.rawX - touchX).toInt()
                        switchParams.y = initY + (event.rawY - touchY).toInt()
                        windowManager.updateViewLayout(btn, switchParams)
                        return true
                    }
                }
                return false
            }
        })

        floatingSwitchView = btn
        windowManager.addView(floatingSwitchView, switchParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayCanvasLayout?.let { windowManager.removeView(it) }
        floatingSwitchView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

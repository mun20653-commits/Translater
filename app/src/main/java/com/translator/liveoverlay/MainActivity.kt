package com.translator.liveoverlay

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 100, 60, 60)
        }

        val title = TextView(this).apply {
            text = "실시간 화면 번역기"
            textSize = 22f
        }
        val desc = TextView(this).apply {
            text = "다른 앱 위에 번역창을 띄우려면 오버레이 권한이 필요합니다."
            textSize = 14f
            setPadding(0, 20, 0, 40)
        }
        val btn = Button(this).apply {
            text = "번역 오버레이 서비스 시작"
            setOnClickListener { checkOverlayPermissionAndStart() }
        }

        layout.addView(title)
        layout.addView(desc)
        layout.addView(btn)
        setContentView(layout)
    }

    private fun checkOverlayPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1001)
            Toast.makeText(this, "다른 앱 위에 표시 권한을 허용해 주세요.", Toast.LENGTH_LONG).show()
        } else {
            startService(Intent(this, LiveTranslateOverlayService::class.java))
            Toast.makeText(this, "실시간 번역기가 켜졌습니다!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

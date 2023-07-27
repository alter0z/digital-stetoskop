package com.tensorflow.android.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.tensorflow.android.R
import com.tensorflow.android.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {
    private var _binding : ActivitySplashScreenBinding? = null
    private val binding get() = _binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.white)

        val decorView: View = window.decorView
        val flags: Int = decorView.systemUiVisibility
        decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.tensorflow.android.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.tensorflow.android.R
import com.tensorflow.android.databinding.ActivitySplashScreenBinding
import com.tensorflow.android.utils.UserPreferences
import com.tensorflow.android.views.doctors.DoctorMainActivity
import com.tensorflow.android.views.patients.PatientMainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {
    private var _binding : ActivitySplashScreenBinding? = null
    private val binding get() = _binding
    private var userPreferences: UserPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        userPreferences = UserPreferences(this)

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.white)

        val decorView: View = window.decorView
        val flags: Int = decorView.systemUiVisibility
        decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            if (userPreferences?.isLogin() == false) {
                startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SplashScreen, if (userPreferences?.getUserRole().equals("pasien")) PatientMainActivity::class.java else DoctorMainActivity::class.java))
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
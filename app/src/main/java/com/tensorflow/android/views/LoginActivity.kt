package com.tensorflow.android.views

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.tensorflow.android.R
import com.tensorflow.android.databinding.ActivityLoginBinding
import com.tensorflow.android.views.patients.PatientMainActivity

class LoginActivity : AppCompatActivity() {
    private var _binding : ActivityLoginBinding? = null
    private val binding get() = _binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.white)

        val decorView: View = window.decorView
        val flags: Int = decorView.systemUiVisibility
        decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding?.showPassword?.setOnClickListener{
            binding?.showPassword?.visibility = View.INVISIBLE
            binding?.hidePassword?.visibility = View.VISIBLE
            binding?.password?.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        binding?.hidePassword?.setOnClickListener{
            binding?.hidePassword?.visibility = View.INVISIBLE
            binding?.showPassword?.visibility = View.VISIBLE
            binding?.password?.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }

        binding?.register?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding?.login?.setOnClickListener {
            startActivity(Intent(this, PatientMainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.tensorflow.android.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.tensorflow.android.R
import com.tensorflow.android.databinding.ActivityRegisterBinding
import com.tensorflow.android.views.fragments.RegisterAsDoctor
import com.tensorflow.android.views.fragments.RegisterAsPatient

class RegisterActivity : AppCompatActivity() {
    private var _binding : ActivityRegisterBinding? = null
    private val binding get() = _binding
    private var currentFragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.white)

        val decorView: View = window.decorView
        val flags: Int = decorView.systemUiVisibility
        decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        currentFragment = supportFragmentManager.findFragmentById(R.id.frame)
        val patient = RegisterAsPatient()
        val doctor = RegisterAsDoctor()
        setupRegisterAs(patient, doctor)

        binding?.login?.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        binding?.register?.setOnClickListener {
            if (currentFragment is RegisterAsPatient) patient.register()
            if (currentFragment is RegisterAsDoctor) doctor.register()
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame,fragment)
        fragmentTransaction.commit()
        currentFragment = fragment
    }

    private fun setupRegisterAs(patient: RegisterAsPatient, doctor: RegisterAsDoctor) {
        replaceFragment(patient)
        binding?.doctorInactive?.setOnClickListener {
            binding?.patientActive?.visibility = View.INVISIBLE
            binding?.patientInactive?.visibility = View.VISIBLE
            binding?.doctorInactive?.visibility = View.INVISIBLE
            binding?.doctorActive?.visibility = View.VISIBLE
            replaceFragment(doctor)
        }

        binding?.patientInactive?.setOnClickListener {
            binding?.patientActive?.visibility = View.VISIBLE
            binding?.patientInactive?.visibility = View.INVISIBLE
            binding?.doctorInactive?.visibility = View.VISIBLE
            binding?.doctorActive?.visibility = View.INVISIBLE
            replaceFragment(patient)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
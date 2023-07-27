package com.tensorflow.android.views.patients

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tensorflow.android.R
import com.tensorflow.android.databinding.ActivityPatientMainBinding
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

class PatientMainActivity : AppCompatActivity() {
    private var _binding: ActivityPatientMainBinding? = null
    private val binding get() = _binding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPatientMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.myToolbar!!)
        val navView: BottomNavigationView = binding?.navView!!
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.patient_home, R.id.patient_record, R.id.patient_predict))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.patient_home -> {
                    navController.navigate(R.id.patient_home)
                    false
                }
                R.id.patient_record -> {
                    navController.navigate(R.id.patient_record)
                    false
                }
                R.id.patient_predict -> {
                    navController.navigate(R.id.patient_predict)
                    false
                }
                else -> true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.tensorflow.android.views.doctors

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tensorflow.android.R
import com.tensorflow.android.databinding.ActivityDoctorMainBinding

class DoctorMainActivity : AppCompatActivity() {
    private var _binding: ActivityDoctorMainBinding? = null
    private val binding get() = _binding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDoctorMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.myToolbar!!)
        val navView: BottomNavigationView = binding?.navView!!
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.doctor_home, R.id.doctor_record))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.doctor_home -> {
                    navController.navigate(R.id.doctor_home)
                    false
                }
                R.id.doctor_record -> {
                    navController.navigate(R.id.doctor_record)
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
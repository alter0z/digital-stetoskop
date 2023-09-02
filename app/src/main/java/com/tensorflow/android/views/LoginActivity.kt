package com.tensorflow.android.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import com.tensorflow.android.R
import com.tensorflow.android.databinding.ActivityLoginBinding
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.utils.LoadingDialog
import com.tensorflow.android.utils.UserPreferences
import com.tensorflow.android.viewmodels.AuthViewModel
import com.tensorflow.android.views.doctors.DoctorMainActivity
import com.tensorflow.android.views.patients.PatientMainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class LoginActivity : AppCompatActivity() {
    private var _binding : ActivityLoginBinding? = null
    private val binding get() = _binding
    private var loading: LoadingDialog? = null
    private val viewModel: AuthViewModel by viewModels()
    private var userPreferences: UserPreferences? = null
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

        loading = LoadingDialog(this)
        userPreferences = UserPreferences(this)

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
//            startActivity(Intent(this, PatientMainActivity::class.java))
//            finish()
            login()
        }
    }

    private fun login() {
        val email = binding?.email?.text.toString()
        val password = binding?.password?.text.toString()
        when {
            email.isEmpty() -> binding?.email?.error = "Email tidak boleh kosong!"
            !email.isValidEmail() -> binding?.email?.error = "Pola email salah!"
            password.isEmpty() -> binding?.password?.error = "Buat password anda!"
//            password.length < 8 -> binding?.password?.error = "Password minimal 8 karakter!"
            else -> {
                val emailBody = email.toRequestBody("text/plain".toMediaType())
                val passwordBody = password.toRequestBody("text/plain".toMediaType())
                viewModel.login(emailBody, passwordBody).observe(this) {
                    if (it != null) {
                        when (it) {
                            is RequestState.Loading -> loading?.show()
                            is RequestState.Success -> {
                                userPreferences?.setLogin(true)
                                it.data.authorisation?.token?.let { token -> userPreferences?.setToken(token) }
                                it.data.user?.id?.let { id -> userPreferences?.setUserId(id) }
                                loading?.hide()
                                startActivity(Intent(this, if (it.data.user?.role?.equals("pasien") == true) PatientMainActivity::class.java else DoctorMainActivity::class.java))
                                finish()
                            }

                            is RequestState.Error -> {
                                loading?.hide()
                                AlertDialog.Builder(this).apply {
                                    setTitle("Peringatan!")
                                    setMessage(it.message)
                                    setPositiveButton("Oke") { _, _ -> }
                                    create()
                                    show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
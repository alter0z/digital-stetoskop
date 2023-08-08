package com.tensorflow.android.views.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.tensorflow.android.R
import com.tensorflow.android.databinding.FragmentRegisterAsPatientBinding
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.utils.LoadingDialog
import com.tensorflow.android.utils.UserPreferences
import com.tensorflow.android.viewmodels.AuthViewModel
import com.tensorflow.android.views.patients.PatientMainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class RegisterAsPatient : Fragment() {
    private var _binding: FragmentRegisterAsPatientBinding? = null
    private val binding get() = _binding!!
    private var loading: LoadingDialog? = null
    private var gender: String = "Laki-Laki"
    private val viewModel: AuthViewModel by viewModels()
    private var userPreferences: UserPreferences? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterAsPatientBinding.inflate(inflater,container,false)
        loading = LoadingDialog((activity as AppCompatActivity?)!!)
        userPreferences = UserPreferences(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPassworVisibility()

        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.gender.adapter = adapter
        }

        binding.gender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGender = parent?.getItemAtPosition(position).toString()
                gender = selectedGender
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupPassworVisibility() {
        binding.showPassword.setOnClickListener{
            binding.showPassword.visibility = View.INVISIBLE
            binding.hidePassword.visibility = View.VISIBLE
            binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        binding.hidePassword.setOnClickListener{
            binding.hidePassword.visibility = View.INVISIBLE
            binding.showPassword.visibility = View.VISIBLE
            binding.password.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }

        binding.showConfirmPassword.setOnClickListener{
            binding.showConfirmPassword.visibility = View.INVISIBLE
            binding.hideConfirmPassword.visibility = View.VISIBLE
            binding.passwordConfirm.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        binding.hideConfirmPassword.setOnClickListener{
            binding.hideConfirmPassword.visibility = View.INVISIBLE
            binding.showConfirmPassword.visibility = View.VISIBLE
            binding.passwordConfirm.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
    }

    fun register() {
        val name = binding.name.text.toString()
        val address = binding.address.text.toString()
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.passwordConfirm.text.toString()
        when {
            name.isEmpty() -> binding.name.error = "Email tidak boleh kosong!"
            address.isEmpty() -> binding.address.error = "Email tidak boleh kosong!"
            email.isEmpty() -> binding.email.error = "Email tidak boleh kosong!"
            !email.isValidEmail() -> binding.email.error = "Pola email salah!"
            password.isEmpty() -> binding.password.error = "Buat password anda!"
            password.length < 8 -> binding.password.error = "Password minimal 8 karakter!"
            confirmPassword.isEmpty() -> binding.passwordConfirm.error = "Konfirmasi password anda!"
            password != confirmPassword -> binding.passwordConfirm.error = "Password tidak sama!"
            else -> {
                val nameBody = name.toRequestBody("text/plain".toMediaType())
                val addressBody = address.toRequestBody("text/plain".toMediaType())
                val genderBody = gender.toRequestBody("text/plain".toMediaType())
                val emailBody = email.toRequestBody("text/plain".toMediaType())
                val passwordBody = password.toRequestBody("text/plain".toMediaType())
                viewModel.patientRegister(nameBody, addressBody, genderBody, emailBody, passwordBody).observe(this) {
                    if (it != null) {
                        when (it) {
                            is RequestState.Loading -> loading?.show()
                            is RequestState.Success -> {
                                userPreferences?.setLogin(true)
                                it.data.authorisation?.token?.let { token -> userPreferences?.setToken(token) }
                                it.data.user?.id?.let { id -> userPreferences?.setUserId(id) }
                                loading?.hide()
                                startActivity(Intent(requireActivity(), PatientMainActivity::class.java))
                                requireActivity().finish()
                            }

                            is RequestState.Error -> {
                                loading?.hide()
                                AlertDialog.Builder(requireActivity()).apply {
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
}
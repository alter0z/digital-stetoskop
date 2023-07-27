package com.tensorflow.android.views.fragments

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tensorflow.android.databinding.FragmentRegisterAsDoctorBinding

class RegisterAsDoctor : Fragment() {
    private var _binding: FragmentRegisterAsDoctorBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterAsDoctorBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPassworVisibility()
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
}
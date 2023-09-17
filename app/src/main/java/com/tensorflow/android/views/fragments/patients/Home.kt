package com.tensorflow.android.views.fragments.patients

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.tensorflow.android.R
import com.tensorflow.android.databinding.FragmentHomeBinding
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.utils.LoadingDialog
import com.tensorflow.android.utils.UserPreferences
import com.tensorflow.android.viewmodels.PatientViewModel
import com.tensorflow.android.views.ConnectBluetoothActivity
import com.tensorflow.android.views.LoginActivity
import com.tensorflow.android.views.patients.PatientMainActivity

class Home : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var loading: LoadingDialog? = null
    private val viewModel: PatientViewModel by viewModels()
    private var userPreferences: UserPreferences? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        loading = LoadingDialog((activity as AppCompatActivity?)!!)
        userPreferences = UserPreferences(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireActivity())
            .load(R.drawable.profile_icon_design_free_vector).circleCrop()
            .into(binding.profilePhoto)

        val screenWidth = resources.displayMetrics.widthPixels
        val predictColumnWidth = (screenWidth * 0.6).toInt()
        val infoColumnWidth = screenWidth - predictColumnWidth - (30*2)

        val predictColumnLayoutParams = binding.predict.layoutParams as ConstraintLayout.LayoutParams
        predictColumnLayoutParams.width = predictColumnWidth
        predictColumnLayoutParams.marginStart = 30
        binding.predict.layoutParams = predictColumnLayoutParams

        val infoColumnLayoutParams = binding.info.layoutParams as ConstraintLayout.LayoutParams
        infoColumnLayoutParams.width = infoColumnWidth
        infoColumnLayoutParams.marginEnd = 30
        binding.info.layoutParams = infoColumnLayoutParams

        setupHomeData()

        binding.connect.setOnClickListener { startActivity(Intent(requireContext(), ConnectBluetoothActivity::class.java)) }
        binding.logout.setOnClickListener {
            userPreferences?.setToken("")
            userPreferences?.setUserRole("")
            userPreferences?.setUserId(0)
            userPreferences?.setLogin(false)
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }
    }

    private fun setupHomeData() {
        val id = userPreferences?.getUserId()
        id?.let {  uid ->
            viewModel.getUserById(uid).observe(viewLifecycleOwner) {
                if (it != null) {
                    when (it) {
                        is RequestState.Loading -> loading?.show()
                        is RequestState.Success -> {
                            loading?.hide()
                            binding.username.text = it.data.data?.namaLengkap
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
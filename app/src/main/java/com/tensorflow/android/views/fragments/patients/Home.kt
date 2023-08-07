package com.tensorflow.android.views.fragments.patients

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.tensorflow.android.R
import com.tensorflow.android.databinding.FragmentHomeBinding
import com.tensorflow.android.databinding.FragmentRegisterAsDoctorBinding
import com.tensorflow.android.views.ConnectBluetoothActivity

class Home : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
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

        binding.connect.setOnClickListener { startActivity(Intent(requireContext(), ConnectBluetoothActivity::class.java)) }
    }
}
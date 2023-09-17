package com.tensorflow.android.views.fragments.doctors

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tensorflow.android.R
import com.tensorflow.android.adapters.PatientListAdapter
import com.tensorflow.android.databinding.FragmentHomeDoctorBinding
import com.tensorflow.android.listeners.OnPatientDetailClickListener
import com.tensorflow.android.models.response.base.User
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.utils.LoadingDialog
import com.tensorflow.android.utils.UserPreferences
import com.tensorflow.android.viewmodels.DoctorViewModel
import com.tensorflow.android.views.ConnectBluetoothActivity
import com.tensorflow.android.views.LoginActivity
import com.tensorflow.android.views.doctors.PatientDetailActivity

class Home : Fragment() {
    private var _binding: FragmentHomeDoctorBinding? = null
    private val binding get() = _binding!!
    private var loading: LoadingDialog? = null
    private val viewModel: DoctorViewModel by viewModels()
    private var userPreferences: UserPreferences? = null
    private var adapter: PatientListAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private val filteredList = ArrayList<User>()
    private val tmpList = ArrayList<User>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDoctorBinding.inflate(inflater,container,false)
        loading = LoadingDialog((activity as AppCompatActivity?)!!)
        userPreferences = UserPreferences(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireActivity())
            .load(R.drawable.profile_icon_design_free_vector).circleCrop()
            .into(binding.profilePhoto)

        setupListView()
        setupHomeData()

        adapter?.setOnPatientDetailClickListener(object: OnPatientDetailClickListener{
            override fun onPatinetDetailClick(id: Int) {
                startActivity(Intent(requireContext(), PatientDetailActivity::class.java).putExtra("ID", id))
            }
        })

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                filterPatients(searchText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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

    private fun setupListView() {
        adapter = PatientListAdapter(requireContext())
        layoutManager = LinearLayoutManager(requireContext())
        binding.patientList.layoutManager = layoutManager
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

        viewModel.getAllPatient().observe(viewLifecycleOwner) {
            if (it != null) {
                when (it) {
                    is RequestState.Loading -> loading?.show()
                    is RequestState.Success -> {
                        loading?.hide()
                        it.data.data?.let { data ->
                            adapter?.setData(data)
                            tmpList.addAll(data)
                        }
                        binding.patientList.adapter = adapter
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

    private fun filterPatients(query: String) {
        filteredList.clear()

        for (item in tmpList) {
            if (item.namaLengkap?.contains(query, ignoreCase = true) == true) filteredList.add(item)
        }

        adapter?.setData(filteredList)
        binding.patientList.adapter = adapter
    }
}
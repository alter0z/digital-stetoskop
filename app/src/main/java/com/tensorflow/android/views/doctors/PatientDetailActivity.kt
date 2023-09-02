package com.tensorflow.android.views.doctors

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tensorflow.android.R
import com.tensorflow.android.adapters.PatientListAdapter
import com.tensorflow.android.adapters.UnlocalFileListAdapter
import com.tensorflow.android.databinding.ActivityPatientDetailBinding
import com.tensorflow.android.listeners.OnUnlocalFileResultClickListener
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.utils.LoadingDialog
import com.tensorflow.android.utils.UserPreferences
import com.tensorflow.android.viewmodels.DoctorViewModel
import com.tensorflow.android.views.PredictResultActivity

class PatientDetailActivity : AppCompatActivity() {
    private var _binding: ActivityPatientDetailBinding? = null
    private val binding get() = _binding
    private var loading: LoadingDialog? = null
    private val viewModel: DoctorViewModel by viewModels()
    private var userPreferences: UserPreferences? = null
    private var adapter: UnlocalFileListAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPatientDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        loading = LoadingDialog(this)
        userPreferences = UserPreferences(this)
        val id = intent.getIntExtra("ID",0)
        binding?.profilePhoto?.let {
            Glide.with(this)
                .load(R.drawable.profile_icon_design_free_vector).circleCrop()
                .into(it)
        }

        binding?.patientProfilePhoto?.let {
            Glide.with(this)
                .load(R.drawable.profile_icon_design_free_vector).circleCrop()
                .into(it)
        }

        setupDetailData(id)
        setupListView()
        fetchUserPredict(id)

        adapter?.setOnUnlocalFileResultClickListener(object: OnUnlocalFileResultClickListener{
            override fun onunlocalFileResultClick(id: Int) {
                startActivity(Intent(this@PatientDetailActivity, PredictResultActivity::class.java).putExtra("ID", id))
            }
        })
    }

    private fun setupListView() {
        adapter = UnlocalFileListAdapter()
        layoutManager = LinearLayoutManager(this)
        binding?.fileList?.layoutManager = layoutManager
    }

    private fun setupDetailData(id: Int) {
        viewModel.getUserById(id).observe(this) {
            if (it != null) {
                when (it) {
                    is RequestState.Loading -> loading?.show()
                    is RequestState.Success -> {
                        loading?.hide()
                        binding?.name?.text = it.data.data?.namaLengkap
                        binding?.gender?.text = it.data.data?.gender
                        binding?.email?.text = it.data.data?.email
                        binding?.address?.text = it.data.data?.gender
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

    private fun fetchUserPredict(id: Int) {
        viewModel.getUserPredict(id).observe(this) {
            if (it != null) {
                when (it) {
                    is RequestState.Loading -> loading?.show()
                    is RequestState.Success -> {
                        loading?.hide()
                        it.data.data?.let { data -> adapter?.setData(data) }
                        binding?.fileList?.adapter = adapter
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
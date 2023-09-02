package com.tensorflow.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tensorflow.android.R
import com.tensorflow.android.databinding.PatientListContentBinding
import com.tensorflow.android.listeners.OnPatientDetailClickListener
import com.tensorflow.android.models.response.base.User

class PatientListAdapter(private val context: Context): RecyclerView.Adapter<PatientListAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: PatientListContentBinding): RecyclerView.ViewHolder(binding.root)

    private var data = ArrayList<User>()
    private var onPatientDetailClickListener: OnPatientDetailClickListener? = null

    fun setOnPatientDetailClickListener(onPatientDetailClickListener: OnPatientDetailClickListener) {
        this.onPatientDetailClickListener = onPatientDetailClickListener
    }

    fun setData(list: List<User>) {
        this.data.clear()
        this.data.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PatientListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(data[position]) {
                binding.name.text = namaLengkap
                binding.gender.text = gender
                Glide.with(context)
                    .load(R.drawable.profile_icon_design_free_vector).circleCrop()
                    .into(binding.profilePhoto)
                binding.detail.setOnClickListener { this.id?.let { id -> onPatientDetailClickListener?.onPatinetDetailClick(id) } }
            }
        }
    }
}
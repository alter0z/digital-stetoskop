package com.tensorflow.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tensorflow.android.models.DeviceModel
import com.tensorflow.android.R
import com.tensorflow.android.databinding.DeviceListContentBinding
import com.tensorflow.android.databinding.FileListContentBinding
import com.tensorflow.android.databinding.ResultListContentBinding
import com.tensorflow.android.listeners.OnDeviceClickListener
import com.tensorflow.android.listeners.OnFileResultClickListener
import com.tensorflow.android.models.FileModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PredictResultListAdapter(private val context: Context): RecyclerView.Adapter<PredictResultListAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ResultListContentBinding): RecyclerView.ViewHolder(binding.root)

    private val fileList = ArrayList<String>()

    fun setList(list: List<String>) {
        this.fileList.clear()
        this.fileList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ResultListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(fileList[position]) {
                binding.status.text = this
                if (this != "Normal") {
                    binding.frame.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red))
                    binding.icon.setImageResource(R.drawable.x_circle)
                }
            }
        }
    }
}
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
import com.tensorflow.android.listeners.OnDeviceClickListener
import com.tensorflow.android.listeners.OnFileResultClickListener
import com.tensorflow.android.models.FileModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileListAdapter(private val fileList: ArrayList<FileModel>): RecyclerView.Adapter<FileListAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: FileListContentBinding): RecyclerView.ViewHolder(binding.root)

    private var onFileResultClickListener: OnFileResultClickListener? = null

    fun onFileResultClickListener(onFileResultClickListener: OnFileResultClickListener) {
        this.onFileResultClickListener = onFileResultClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FileListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(fileList[position]) {
                binding.fileName.text = name
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val formattedDate = date?.let { Date(it) }?.let { dateFormat.format(it) }
                binding.date.text = formattedDate

                binding.frame.setOnClickListener {
                    if (name != null) {
                        onFileResultClickListener?.onFileResultClick(this)
                    }
                }
            }
        }
    }
}
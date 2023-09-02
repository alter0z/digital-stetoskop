package com.tensorflow.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tensorflow.android.databinding.FileListContentBinding
import com.tensorflow.android.listeners.OnUnlocalFileResultClickListener
import com.tensorflow.android.models.FileModel
import com.tensorflow.android.models.response.base.WavRecordResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UnlocalFileListAdapter: RecyclerView.Adapter<UnlocalFileListAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: FileListContentBinding): RecyclerView.ViewHolder(binding.root)

    private var onUnlocalFileResultClickListener: OnUnlocalFileResultClickListener? = null
    private val fileList = ArrayList<WavRecordResponse>()

    fun setOnUnlocalFileResultClickListener(onUnlocalFileResultClickListener: OnUnlocalFileResultClickListener) {
        this.onUnlocalFileResultClickListener = onUnlocalFileResultClickListener
    }

    fun setData(list: List<WavRecordResponse>) {
        this.fileList.clear()
        this.fileList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FileListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(fileList[position]) {
                binding.fileName.text = suara
                binding.date.text = createdAt?.let { formatDate(it) }
                binding.pedict.setOnClickListener { this.id?.let { id -> onUnlocalFileResultClickListener?.onunlocalFileResultClick(id) } }
            }
        }
    }

    private fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
        val outputFormat = SimpleDateFormat("d MMMM yyyy HH:mm", Locale.US)

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }
}
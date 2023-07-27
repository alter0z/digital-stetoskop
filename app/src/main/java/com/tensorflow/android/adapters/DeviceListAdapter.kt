package com.tensorflow.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tensorflow.android.models.DeviceModel
import com.tensorflow.android.R
import com.tensorflow.android.databinding.DeviceListContentBinding
import com.tensorflow.android.listeners.OnDeviceClickListener

class DeviceListAdapter(private val context: Context, private val deviceList: ArrayList<DeviceModel>): RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: DeviceListContentBinding): RecyclerView.ViewHolder(binding.root)

    private var onDeviceClickListener: OnDeviceClickListener? = null
    private var index = -1

    fun onDeviceClickListener(onDeviceClickListener: OnDeviceClickListener) {
        this.onDeviceClickListener = onDeviceClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DeviceListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = deviceList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(deviceList[position]) {
                binding.deviceName.text = name
                binding.macAddress.text = macAddress
                binding.frame.setCardBackgroundColor(ContextCompat.getColor(context, R.color.orange))

                binding.frame.setOnClickListener {
                    onDeviceClickListener?.onDeviceClick(position)
                    index = adapterPosition
                    notifyDataSetChanged()
                }

                if (index == position) {
                    binding.frame.setCardBackgroundColor(ContextCompat.getColor(context, R.color.blue))
                }
            }
        }
    }
}
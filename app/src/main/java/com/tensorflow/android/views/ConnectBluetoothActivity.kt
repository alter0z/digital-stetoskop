package com.tensorflow.android.views

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tensorflow.android.models.DeviceModel
import com.tensorflow.android.adapters.DeviceListAdapter
import com.tensorflow.android.databinding.ActivityConnectBluetoothBinding
import com.tensorflow.android.listeners.OnDeviceClickListener
import com.tensorflow.android.listeners.OnSocketConnectedListener
import com.tensorflow.android.utils.BluetoothSocketHolder
import com.tensorflow.android.views.fragments.patients.Record
import java.io.IOException
import java.util.UUID

class ConnectBluetoothActivity : AppCompatActivity() {
    private var _binding: ActivityConnectBluetoothBinding? = null
    private val binding get() = _binding
    private var adapter: DeviceListAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var myUUID: UUID? = null
    private var pairedDeviceArrayList: ArrayList<BluetoothDevice>? = null
    private var deviceList: ArrayList<DeviceModel>? = null
    private var device: BluetoothDevice? = null
    private var myThreadConnectBTdevice: ThreadConnectBTdevice? = null
    var onSocketConnectedListener: OnSocketConnectedListener? = null
//    fun onSocketConnectedListener(onSocketConnectedListener: OnSocketConnectedListener) {
//        this.onSocketConnectedListener = onSocketConnectedListener
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityConnectBluetoothBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Your device does not support Bluetooth", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB"
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            finish()
        }

        binding?.refresh?.setOnClickListener {
            startActivity(Intent(this, ConnectBluetoothActivity::class.java))
            finish()
        }

        binding?.done?.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermission()

        if (!bluetoothAdapter!!.isEnabled) {
            val enableIntent: Intent
            if (checkBtPermission()) {
                enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, REQUEST_PERMISSION)
            }
        }
        setupBluetooth()
    }

    private fun setupBluetooth() {
        if (checkBtPermission()) {
            val pairedDevices = bluetoothAdapter!!.bondedDevices
            if (pairedDevices.size > 0) {
                pairedDeviceArrayList = ArrayList()
                deviceList = ArrayList()
                pairedDeviceArrayList?.addAll(pairedDevices)
                for (item in pairedDeviceArrayList!!) {
                    deviceList?.add(DeviceModel(item.name, item.address))
                }

                setupDeviceList(deviceList!!)

                adapter?.onDeviceClickListener(object: OnDeviceClickListener{
                    override fun onDeviceClick(pos: Int) {
                        device = pairedDeviceArrayList?.get(pos)
                        if (checkBtPermission()) {
                            Toast.makeText(
                                this@ConnectBluetoothActivity,
                                "Connectiong to  " + device!!.name,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        myThreadConnectBTdevice = device?.let { ThreadConnectBTdevice(it) }
                        myThreadConnectBTdevice?.start()
                    }
                })
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION
            )
        }
        val check =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return check == PackageManager.PERMISSION_GRANTED
    }

    fun checkBtPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_PERMISSION
            )
        }
        val check = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        return check == PackageManager.PERMISSION_GRANTED
    }

    private fun setupDeviceList(list: ArrayList<DeviceModel>) {
        adapter = DeviceListAdapter(this@ConnectBluetoothActivity, list)
        layoutManager = LinearLayoutManager(this)
        binding?.deviceList?.layoutManager = layoutManager
        binding?.deviceList?.adapter = adapter
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION) {
            if (resultCode == RESULT_OK) {
                setupBluetooth()
            } else {
                Toast.makeText(
                    this,
                    "BlueTooth NOT enabled",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    inner class ThreadConnectBTdevice constructor(device: BluetoothDevice) :
        Thread() {
        private var bluetoothSocket: BluetoothSocket? = null

        init {
            if (checkBtPermission()) {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        override fun run() {
            var success = false
            if (checkBtPermission()) {
                try {
                    bluetoothSocket!!.connect()
                    success = true
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(
                            this@ConnectBluetoothActivity,
                            "Could not connect to your device!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    try {
                        bluetoothSocket!!.close()
                    } catch (e1: IOException) {
                        e1.printStackTrace()
                    }
                }
            }
            if (success) {
                runOnUiThread {
                    Toast.makeText(
                        this@ConnectBluetoothActivity,
                        "connection successful",
                        Toast.LENGTH_LONG
                    ).show()
                }
                bluetoothSocket?.let { BluetoothSocketHolder.setBluetoothSocket(it) }
            }
        }

        fun cancel() {
            try {
                bluetoothSocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private val REQUEST_PERMISSION = 1
}
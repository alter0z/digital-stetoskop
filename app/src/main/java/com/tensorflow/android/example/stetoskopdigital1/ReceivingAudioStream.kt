package com.tensorflow.android.example.stetoskopdigital1

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tensorflow.android.R
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.util.*

open class ReceivingAudioStream: AppCompatActivity() {
    private var INTERVAL = 5000
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var pairedDeviceArrayList: ArrayList<BluetoothDevice>? = null
    private var connStatus: TextView? = null
    private var receiveStatus:TextView? = null
    private var intervalStatus:TextView? = null
    private var refresh: Button? = null
    private var listViewPairedDevice: ListView? = null
    private var btnDisconnect: Button? = null
    private var device: BluetoothDevice? = null
    private var data: String? = null
    private var id: String? = null
    private var username:String? = null
    private var isConnected = false
    private val isContinuesData = false
    private var popup: Dialog? = null
    private var sharedpreferences: SharedPreferences? = null
    private var pairedDeviceAdapter: ArrayAdapter<String>? = null
    private var myUUID: UUID? = null
    private var myThreadConnectBTdevice: Job? = null
    private var myThreadConnected: Job? = null
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bluetoothrecord)

        // get id & username
        sharedpreferences = getSharedPreferences(my_shared_preferences, MODE_PRIVATE)
        id = sharedpreferences?.getString(TAG_ID, null)
        username = sharedpreferences?.getString(TAG_USERNAME, null)

        refresh = findViewById(R.id.refresh)
        connStatus = findViewById(R.id.connStatus)
        receiveStatus = findViewById(R.id.recStatus)
        intervalStatus = findViewById(R.id.interval)
        listViewPairedDevice = findViewById(R.id.pairedlist)
        btnDisconnect = findViewById(R.id.disconnect)
        val back = findViewById<ImageButton>(R.id.back)
        val setInterval = findViewById<Button>(R.id.setint)
        //        fileStatus = findViewById(R.id.writefile);
        popup = Dialog(this)

//        client = new MqttClient(this);
        back.setOnClickListener {
            if (!isConnected) {
                val intent =
                    Intent(this, MainActivity1::class.java)
                intent.putExtra(TAG_ID, id)
                intent.putExtra(TAG_USERNAME, username)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Disconnect first!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        setInterval.setOnClickListener {
            popup?.setContentView(R.layout.duration_menu)
            popup?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val sec_5: Button? = popup?.findViewById(R.id.sec_5)
            val sec_10: Button? = popup?.findViewById(R.id.sec_10)
            val sec_15: Button? = popup?.findViewById(R.id.sec_15)
            popup?.show()
            sec_5?.setOnClickListener {
                INTERVAL = 1000 * 5
                intervalStatus?.text = "Your data will be sent every 5 secon"
                popup?.dismiss()
            }
            sec_10?.setOnClickListener {
                INTERVAL = 1000 * 10
                intervalStatus?.text = "Your data will be sent every 10 secon"
                popup?.dismiss()
            }
            sec_15?.setOnClickListener {
                INTERVAL = 1000 * 15
                intervalStatus?.setText("Your data will be sent every 15 secon")
                popup?.dismiss()
            }
        }

        // mqtt service object
//        client = new MqttClient(this);
        refresh?.setOnClickListener {
            startActivity(Intent(this@ReceivingAudioStream, ReceivingAudioStream::class.java))
            finish()
        }
        btnDisconnect?.setOnClickListener {
            if (myThreadConnectBTdevice != null) {
                myThreadConnectBTdevice?.cancel()
                myThreadConnected?.cancel()
                myThreadConnectBTdevice?.cancel()
                isConnected = false
                refresh?.visibility = View.VISIBLE
                listViewPairedDevice?.visibility = View.VISIBLE
                btnDisconnect?.visibility = View.GONE
                receiveStatus?.text = data
            }
        }
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Your device does not support Bluetooth", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        //using the well-known SPP UUID
        val UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB"
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter!!.isEnabled) {
            val enableIntent: Intent
            if (checkBtPermission()) {
                enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, REQUEST_PERMISSION)
            }
        }
        setup()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    private fun setup() {
        if (checkBtPermission()) {
            val pairedDevices = bluetoothAdapter!!.bondedDevices
            if (pairedDevices.size > 0) {
                pairedDeviceArrayList = ArrayList()
                pairedDeviceArrayList!!.addAll(pairedDevices)
                pairedDeviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, f(pairedDeviceArrayList!!))
                listViewPairedDevice!!.adapter = pairedDeviceAdapter
                listViewPairedDevice!!.onItemClickListener =
                    AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                        device = pairedDeviceArrayList!![position]
                        Toast.makeText(
                            this@ReceivingAudioStream,
                            "Connectiong to  " + device?.name,
                            Toast.LENGTH_LONG
                        ).show()
                        connStatus!!.text = "Connecting to ${device?.getName()}"
                        myThreadConnectBTdevice = GlobalScope.launch(Dispatchers.Main) { threadConnectBTdevice(device!!) }
                    }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun f(pairedDeviceArrayList: ArrayList<BluetoothDevice>): ArrayList<String> {
        val list = ArrayList<String>()
        if (checkBtPermission()) {
            for (e in pairedDeviceArrayList.indices) {
                list.add(
                    """
                    ${pairedDeviceArrayList[e].name}
                    ${pairedDeviceArrayList[e].address}
                    """.trimIndent()
                )
            }
            return list
        }
        return list
    }

    override fun onDestroy() {
        super.onDestroy()
        if (myThreadConnectBTdevice != null) {
            myThreadConnectBTdevice!!.cancel()
            isConnected = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION) {
            if (resultCode == RESULT_OK) {
                setup()
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

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    private fun threadConnectBTdevice(device: BluetoothDevice) {
        var bluetoothSocket: BluetoothSocket? = null
        if (checkBtPermission()) {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        var success = false
        if (checkBtPermission()) {
            try {
                bluetoothSocket!!.connect()
                success = true
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread { connStatus!!.text = "Could not connect to your device!" }
                try {
                    bluetoothSocket!!.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }

        if (success) {
            //connect successful
            val connMessage = "Connected to " + device.name
            isConnected = true
            runOnUiThread {
                connStatus!!.text = connMessage
                Toast.makeText(
                    this@ReceivingAudioStream,
                    "connection successful",
                    Toast.LENGTH_LONG
                ).show()
                listViewPairedDevice!!.visibility = View.GONE
                btnDisconnect!!.visibility = View.VISIBLE
                refresh!!.visibility = View.GONE
            }
//            myThreadConnected = GlobalScope.launch(Dispatchers.Main) { threadConnected(bluetoothSocket!!) }
            myThreadConnected = GlobalScope.launch(Dispatchers.Main) { threadConnected(bluetoothSocket!!) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun threadConnected(socket: BluetoothSocket) {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        var files: File? = null
        if (isExtStorageWritable() && checkPermission()) {
            try {
                val contextWrapper = ContextWrapper(this@ReceivingAudioStream)
                val audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)
                files = File(audioDir, System.currentTimeMillis().toString() + ".wav")
                `in` = socket.inputStream
                out = Files.newOutputStream(files.toPath())
            } catch (e: Exception) {
                Toast.makeText(this@ReceivingAudioStream, e.toString(), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this@ReceivingAudioStream, "Cant write data", Toast.LENGTH_LONG).show()
        }

        val buffer = ByteArray(1024)
        var bytesRead = 0
        val sampleRate = 44100
        val channels = 1
        val bitsPerSample = 16
        var audioLength: Long = 0

        if (isConnected) {
            audioLength += (44 - 8).toLong() // this actually header length - 8
            try {
//                    runOnUiThread { receiveStatus!!.text = isConnected.toString() }
                while (bytesRead != -1) {
//                    runOnUiThread { receiveStatus!!.text = "inside while" }
                    bytesRead = `in`?.read(buffer) ?: -1
                    data = String(buffer, 0, bytesRead)
//                    out?.write(buffer, 0, bytesRead)
                    audioLength += bytesRead.toLong()
//                    runOnUiThread { receiveStatus!!.text = data }
                }
            } catch (e: IOException) {
                runOnUiThread { receiveStatus!!.text = audioLength.toString() }
//                try {
//                    out?.close()
//                    out = Files.newOutputStream(files?.toPath())
//                    out.write(
//                        getWavHeader(
//                            sampleRate.toLong(),
//                            channels.toLong(),
//                            bitsPerSample.toLong(),
//                            audioLength
//                        )
//                    )
//                    `in`?.close()
//                    out?.close()
//                    socket.close()
//                } catch (ex: IOException) {
//                    throw RuntimeException(ex)
//                }
                myThreadConnected?.cancel()
                myThreadConnectBTdevice?.cancel()
//                val msgConnectionLost = "Connection lost"
//                runOnUiThread {
//                    connStatus!!.text = msgConnectionLost
//                    receiveStatus!!.text = "No data Received"
//                }
                e.printStackTrace()
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        }
        val check =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return check == PackageManager.PERMISSION_GRANTED
    }

    private fun isExtStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    protected fun checkBtPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_PERMISSION
            )
        }
        val check = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        return check == PackageManager.PERMISSION_GRANTED
    }

    private fun getWavHeader(
        sampleRate: Long,
        channels: Long,
        bitsPerSample: Long,
        audioLength: Long
    ): ByteArray {
        val fileLength = audioLength + 36
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF chunk descriptor
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (fileLength and 0xFFL).toByte()
        header[5] = (fileLength shr 8 and 0xFFL).toByte()
        header[6] = (fileLength shr 16 and 0xFFL).toByte()
        header[7] = (fileLength shr 24 and 0xFFL).toByte()
        header[8] = 'W'.code.toByte() // wave format
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // fmt subchunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // fmt subchunk size
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // audio format (PCM)
        header[21] = 0
        header[22] = channels.toByte() // number of channels
        header[23] = 0
        header[24] = (sampleRate and 0xFFL).toByte() // sample rate
        header[25] = (sampleRate shr 8 and 0xFFL).toByte()
        header[26] = (sampleRate shr 16 and 0xFFL).toByte()
        header[27] = (sampleRate shr 24 and 0xFFL).toByte()
        header[28] = (sampleRate * channels * bitsPerSample / 8 and 0xFFL).toByte() // byte rate
        header[29] = (sampleRate * channels * bitsPerSample / 8 shr 8 and 0xFFL).toByte()
        header[30] = (sampleRate * channels * bitsPerSample / 8 shr 16 and 0xFFL).toByte()
        header[31] = (sampleRate * channels * bitsPerSample / 8 shr 24 and 0xFFL).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte() // block align
        header[33] = 0
        header[34] = bitsPerSample.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte() // data subchunk
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (audioLength and 0xFFL).toByte()
        header[41] = (audioLength shr 8 and 0xFFL).toByte()
        header[42] = (audioLength shr 16 and 0xFFL).toByte()
        header[43] = (audioLength shr 24 and 0xFFL).toByte()
        println("file length: $fileLength")
        println("audio length: $audioLength")
        return header
    }

    companion object {
        private const val REQUEST_PERMISSION = 1
        const val TAG_ID = "id"
        const val TAG_USERNAME = "username"
        const val my_shared_preferences = "my_shared_preferences"
    }
}
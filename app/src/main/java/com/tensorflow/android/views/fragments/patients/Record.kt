package com.tensorflow.android.views.fragments.patients

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.tensorflow.android.audio.features.NormalizeAudio
import com.tensorflow.android.databinding.FragmentRecordBinding
import com.tensorflow.android.example.stetoskopdigital1.Btreceiver
import com.tensorflow.android.listeners.OnSocketConnectedListener
import com.tensorflow.android.utils.BluetoothSocketHolder
import com.tensorflow.android.views.ConnectBluetoothActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime

class Record : Fragment(), OnSocketConnectedListener {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!
    private var myThreadConnected: ThreadConnected? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.date.text = LocalDateTime.now().toString()
        if (checkBtPermission()) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter.isEnabled) {
                val connectedDevices = bluetoothAdapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                        if (profile == BluetoothProfile.HEADSET) {
                            val devices = proxy.connectedDevices
                            for (device: BluetoothDevice in devices) {
                                if (checkBtPermission()) {
                                    val deviceName = device.name
                                    binding.deviceName.text = deviceName
                                }
                            }
                        }
                        bluetoothAdapter.closeProfileProxy(profile, proxy)
                    }

                    override fun onServiceDisconnected(profile: Int) {
                        binding.deviceName.text = "Dissconnected"
                    }
                }, BluetoothProfile.HEADSET)

                if (connectedDevices) {
//                    binding.deviceName.text = "asw"
                } else {
                    binding.deviceName.text = "No Device Connected"
                }
            } else {
                binding.deviceName.text = "Bluetooth is not enabled"
            }
        }
        val socket = BluetoothSocketHolder.getBluetoothSocket()
        binding.record.setOnClickListener {
            if (binding.fileName.text?.isEmpty() == true) {
                binding.fileName.requestFocus()
                binding.fileName.error = "please insert file name first"
            } else {
                if (socket != null) {
                    myThreadConnected = socket?.let { it1 -> ThreadConnected(it1) }
                    myThreadConnected!!.start()
                } else Toast.makeText(requireContext(), "Connect to  your device first!", Toast.LENGTH_LONG).show()
            }
        }

        binding.stop.setOnClickListener {
            myThreadConnected?.cancel()
        }
    }

    private val REQUEST_PERMISSION = 1
    private val SAMPLE_RATE = 8000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

    private fun checkPermission(): Boolean {
        if (requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION
            )
        }
        val check =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return check == PackageManager.PERMISSION_GRANTED
    }

    private fun isExtStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    fun checkBtPermission(): Boolean {
        if (requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_PERMISSION
            )
        }
        val check = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)
        return check == PackageManager.PERMISSION_GRANTED
    }

    override fun onSocketConnected(socket: BluetoothSocket) {
        binding.deviceName.text = "asw"
        binding.record.setOnClickListener {
            if (binding.fileName.text?.isEmpty() == true) {
                binding.fileName.requestFocus()
                binding.fileName.error = "please insert file name first"
            } else {
                myThreadConnected = ThreadConnected(socket)
                myThreadConnected!!.start()
            }
        }
    }

    inner class ThreadConnected(socket: BluetoothSocket) :
        Thread() {
        private val connectedInputStream: InputStream?
        private val connectedOutputStream: FileOutputStream?
        private val connectedSocket: BluetoothSocket

        init {
            var `in`: InputStream? = null
            var out: FileOutputStream? = null
            connectedSocket = socket
            try {
                `in` = socket.inputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (isExtStorageWritable() && checkPermission()) {
                try {
                    val contextWrapper = ContextWrapper(requireContext())
                    //                    File audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
//                    File files = new File(audioDir,System.currentTimeMillis()+".wav");
                    val audioDir = contextWrapper.getExternalFilesDir("sample wav")
                    val files = File(audioDir, "sample.wav")
                    out = FileOutputStream(files)
                    writeWavHeader(out)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Cant write data", Toast.LENGTH_LONG).show()
            }
            connectedInputStream = `in`
            connectedOutputStream = out
        }

        override fun run() {
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead = 0
            var audioLength: Long = 0
            val byteArrayOutputStream = ByteArrayOutputStream()

            // continuous data
            audioLength += (44 - 8).toLong() // this actually header length - 8
            //                isContinuesData = true;
            try {
                while (bytesRead != -1) {
                    bytesRead = connectedInputStream!!.read(buffer)
                    if (bytesRead != -1) {
                        connectedOutputStream!!.write(buffer, 0, bytesRead)
                        //                            connectedOutputStream.write(buffer, 0, bytesRead);
                        byteArrayOutputStream.write(buffer, 0, bytesRead)
                        audioLength += bytesRead.toLong()
                    }
//                    data = byteArrayOutputStream.toByteArray()
                }
            } catch (e: IOException) {
//                    long finalAudioLength = audioLength;
//                    runOnUiThread(() -> receiveStatus.setText(String.valueOf(finalAudioLength+36)));
//                    saveCleanWav();
                try {
//                        writeWavHeader(connectedOutputStream);
                    val contextWrapper = ContextWrapper(requireContext())
                    val file = contextWrapper.getExternalFilesDir("sample wav")
                    val audioFileAbsolutePath = file!!.absolutePath + "/sample.wav"
                    val normalizeAudio = NormalizeAudio()
                    normalizeAudio.normalize(audioFileAbsolutePath, binding.fileName.text.toString(), requireContext())
                    connectedOutputStream!!.close()
                    //                        connectedSocket.close();
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                }
                e.printStackTrace()
            }
        }

        fun cancel() {
            try {
                val contextWrapper = ContextWrapper(requireContext())
                val file = contextWrapper.getExternalFilesDir("sample wav")
                val audioFileAbsolutePath = file!!.absolutePath + "/sample.wav"
                val normalizeAudio = NormalizeAudio()
                normalizeAudio.normalize(audioFileAbsolutePath, binding.fileName.text.toString(), requireContext())
                connectedOutputStream!!.close()
                connectedSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun writeWavHeader(outputStream: FileOutputStream) {
        val audioDataLength = outputStream.channel.size() - 44 // Subtract header size
        val overallSize = audioDataLength + 36 // Add header size
        val header = ByteArray(44)

        // RIFF chunk descriptor
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // Overall size (file size - 8 bytes for RIFF and WAVE tags)
        header[4] = (overallSize and 0xffL).toByte()
        header[5] = (overallSize shr 8 and 0xffL).toByte()
        header[6] = (overallSize shr 16 and 0xffL).toByte()
        header[7] = (overallSize shr 24 and 0xffL).toByte()

        // WAVE chunk
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt sub-chunk
        header[12] = 'f'.code.toByte() // Sub-chunk identifier
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte() // Chunk size
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        // Audio format (PCM = 1)
        header[20] = 1
        header[21] = 0

        // Number of channels (2 = stereo)
        header[22] = (if (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO) 1 else 2).toByte()
        header[23] = 0

        // Sample rate
        header[24] = (SAMPLE_RATE and 0xff).toByte()
        header[25] = (SAMPLE_RATE shr 8 and 0xff).toByte()
        header[26] = (SAMPLE_RATE shr 16 and 0xff).toByte()
        header[27] = (SAMPLE_RATE shr 24 and 0xff).toByte()

        // Byte rate (Sample rate * Number of channels * Bits per sample / 8)
        val byteRate = SAMPLE_RATE * (if (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT) 2 else 1
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()

        // Block align (Number of channels * Bits per sample / 8)
        header[32] = ((if (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT) 2 else 1).toByte()
        header[33] = 0

        // Bits per sample
        header[34] = (if (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT) 16 else 8).toByte()
        header[35] = 0

        // data sub-chunk
        header[36] = 'd'.code.toByte() // Sub-chunk identifier
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte() // Chunk size
        header[40] = (audioDataLength and 0xffL).toByte()
        header[41] = (audioDataLength shr 8 and 0xffL).toByte()
        header[42] = (audioDataLength shr 16 and 0xffL).toByte()
        header[43] = (audioDataLength shr 24 and 0xffL).toByte()
        outputStream.write(header)
    }
}

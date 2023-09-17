package com.tensorflow.android.views

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.tensorflow.android.R
import com.tensorflow.android.adapters.DeviceListAdapter
import com.tensorflow.android.adapters.PredictResultListAdapter
import com.tensorflow.android.audio.features.MFCC
import com.tensorflow.android.audio.features.WavFile
import com.tensorflow.android.audio.features.WavFileException
import com.tensorflow.android.databinding.ActivityPredictResultBinding
import com.tensorflow.android.models.DeviceModel
import com.tensorflow.android.models.FileModel
import com.tensorflow.android.noiseclassifier.Recognition
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.utils.LoadingDialog
import com.tensorflow.android.utils.UserPreferences
import com.tensorflow.android.viewmodels.PatientViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.PriorityQueue
import java.util.Timer
import java.util.TimerTask

class PredictResultActivity : AppCompatActivity() {
    private var _binding: ActivityPredictResultBinding? = null
    private val binding get() = _binding
    private val SAMPLE_RATE = 8000
    private val SHRT_MAX = 32767
    private var adapter: PredictResultListAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    val list = ArrayList<String>()
    private val viewModel: PatientViewModel by viewModels()
    private var userPreferences: UserPreferences? = null
    private var loading: LoadingDialog? = null
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPredictResultBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        userPreferences = UserPreferences(this)
        loading = LoadingDialog(this)

//        val file = intent.getParcelableExtra<FileModel>("FILE") as FileModel
        val id = intent.getIntExtra("ID", 0)
//        binding?.fileName?.text = file.name
//        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
//        val formattedDate = file.date?.let { Date(it) }?.let { dateFormat.format(it) }
//        binding?.date?.text = formattedDate

        setupChart()
        val contextWrapper = ContextWrapper(this);
        val externalStorage: File = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
        val audioDirPath = externalStorage.absolutePath
        fetchDetailPredict(id, audioDirPath)

        setupResultList()
    }

    private fun setupResultList() {
        adapter = PredictResultListAdapter(this)
        layoutManager = LinearLayoutManager(this)
        binding?.resultList?.layoutManager = layoutManager
    }

    private fun setupChart() {
//        val ecgEntry: ArrayList<Entry> = ArrayList()
//        val dataset: LineDataSet?
//        val linedata: LineData?
////        val ttask: com.faishalrachman.amonsecg.DetailActivity.MyTimerTask = com.faishalrachman.amonsecg.DetailActivity.MyTimerTask()
////        val t = Timer()
//        ecgEntry.add(Entry(0f, 0f))
//        dataset = LineDataSet(ecgEntry, "Heart Beat Signal")
//        dataset.color = resources.getColor(R.color.orange)
//        dataset.lineWidth = 2f
//        dataset.setDrawCircles(false)
//        dataset.mode = LineDataSet.Mode.CUBIC_BEZIER
//        dataset.setDrawValues(false)
//        dataset.fillColor = R.color.orange
//        dataset.valueTextColor = R.color.orange
//        linedata = LineData(dataset)
//        binding?.signalView?.data = linedata
//        binding?.signalView?.setDrawMarkers(false)
//        binding?.signalView?.setDrawBorders(true)
//        binding?.signalView?.setBorderWidth(0.001f)
//        binding?.signalView?.setVisibleXRangeMaximum(150f)
//        binding?.signalView?.setVisibleXRangeMinimum(0f)
//        val desc = Description()
//        desc.text = "Heart Beat Signal"
//        binding?.signalView?.description = desc
//        binding?.signalView?.isAutoScaleMinMaxEnabled
//        //        binding?.signalView?.setScaleMinima(800,1000);
//        val min = -2f
//        val max = 3f
//        val count = 10
//        val leftAxis: YAxis? = binding?.signalView?.axisLeft
//        //                        leftAxis.setAxisLineWidth(0.01f);
//        leftAxis?.axisMinimum = min
//        leftAxis?.axisMaximum = max
//        //                        leftAxis.setGranularity(0.5f);
//        leftAxis?.gridLineWidth = 1f
//        leftAxis?.isGranularityEnabled = true
//        leftAxis?.setDrawGridLines(true)
//        leftAxis?.axisLineColor = R.color.green
//        leftAxis?.setLabelCount(count, true)
//        val rightAxis: YAxis? = binding?.signalView?.getAxisRight()
//        rightAxis?.axisMaximum = max
//        rightAxis?.axisMinimum = min
//        rightAxis?.setDrawLabels(false)
//        rightAxis?.gridLineWidth = 1f
//        rightAxis?.setLabelCount(count, true)
//        //                        rightAxis.setGranularity(0.5f);
//        val xAxis: XAxis? = binding?.signalView?.getXAxis()
//        xAxis?.granularity = 1f
//        xAxis?.setDrawLabels(false)
//        xAxis?.setLabelCount(40, true)
//        xAxis?.setDrawGridLines(true)
////        t.scheduleAtFixedRate(ttask, 17, 10)
        // Configure chart properties
        binding?.signalView?.description?.isEnabled = false
        binding?.signalView?.setTouchEnabled(true)
        binding?.signalView?.setPinchZoom(true)
        binding?.signalView?.setBackgroundColor(resources.getColor(R.color.white))
        binding?.signalView?.setDrawGridBackground(false)

        // Customize X-axis properties if needed
        val xAxis = binding?.signalView?.xAxis
        xAxis?.setDrawGridLines(false)

        // Customize Y-axis properties if needed
        val yAxis = binding?.signalView?.axisLeft
        yAxis?.setDrawGridLines(false)
    }

    private fun processWavFileData(wavFilePath: String) {
//        val wavFile = File(wavFilePath)
//        val audioData = ArrayList<Entry>()
//
//        // Read the WAV file using AudioRecord
//        val bufferSize = AudioRecord.getMinBufferSize(
//            SAMPLE_RATE,
//            AudioFormat.CHANNEL_IN_MONO,
//            AudioFormat.ENCODING_PCM_16BIT
//        )
////        val audioRecord = if (ActivityCompat.checkSelfPermission(
////                this,
////                Manifest.permission.RECORD_AUDIO
////            ) != PackageManager.PERMISSION_GRANTED
////        ) {
////
////        } else {
////
////        }
////        AudioRecord(
////            MediaRecorder.AudioSource.DEFAULT,
////            SAMPLE_RATE,
////            AudioFormat.CHANNEL_IN_MONO,
////            AudioFormat.ENCODING_PCM_16BIT,
////            bufferSize
////        )
//
//        val wavData = ByteArray(bufferSize)
//        val dataPoints = ArrayList<Float>()
//
//        val inputStream = FileInputStream(wavFile)
//        val bufferedInputStream = BufferedInputStream(inputStream)
//        val dataInputStream = DataInputStream(bufferedInputStream)
//
//        try {
//            var bytesRead = dataInputStream.read(wavData, 0, bufferSize)
//
//            while (bytesRead != -1) {
//                // Process the WAV data and convert it to data points suitable for the chart
//                for (i in 0 until bytesRead / 2) { // Assuming 16-bit PCM
//                    val sample = wavData[i * 2].toInt() and 0xFF or (wavData[i * 2 + 1].toInt() shl 8)
//                    val amplitude = sample.toFloat() / SHRT_MAX.toFloat() // Normalize amplitude
//                    dataPoints.add(amplitude)
//                }
//
//                bytesRead = dataInputStream.read(wavData, 0, bufferSize)
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
////            audioRecord.release()
//            dataInputStream.close()
//        }
//
//        // Convert data points to Entry objects for the chart
//        for (i in dataPoints.indices) {
//            val entry = Entry(i.toFloat(), dataPoints[i])
//            audioData.add(entry)
//        }
//
//        // Create a LineDataSet with the audio data
//        val dataSet = LineDataSet(audioData, "Audio Data")
//        dataSet.color = Color.BLUE
//        dataSet.setDrawCircles(false)
//
//        // Create a LineData object and set the LineDataSet
//        val lineData = LineData(dataSet)
//
//        // Set the LineData object to the chart
//        binding?.signalView?.data = lineData
//
//        // Refresh the chart
//        binding?.signalView?.invalidate()

        val wavFile = File(wavFilePath)
        val audioData = ArrayList<Entry>()

        // Read the WAV file using AudioRecord
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val wavData = ByteArray(bufferSize)
        val dataPoints = ArrayList<Float>()

        val inputStream = FileInputStream(wavFile)
        val bufferedInputStream = BufferedInputStream(inputStream)
        val dataInputStream = DataInputStream(bufferedInputStream)

        try {
            var bytesRead = dataInputStream.read(wavData, 0, bufferSize)

            while (bytesRead != -1) {
                // Process the WAV data and convert it to data points suitable for the chart
                for (i in 0 until bytesRead / 2) { // Assuming 16-bit PCM
                    val sample = wavData[i * 2].toInt() and 0xFF or (wavData[i * 2 + 1].toInt() shl 8)
                    val amplitude = sample.toFloat() / SHRT_MAX.toFloat() // Normalize amplitude
                    dataPoints.add(amplitude)
                }

                bytesRead = dataInputStream.read(wavData, 0, bufferSize)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            dataInputStream.close()
        }

        // Convert data points to Entry objects for the chart
        for (i in dataPoints.indices) {
            val entry = Entry(i.toFloat(), dataPoints[i])
            audioData.add(entry)
        }

        // Create a LineDataSet with the audio data
        val dataSet = LineDataSet(audioData, "Heart Beat Signal")
        dataSet.color = resources.getColor(R.color.green)
        dataSet.setDrawCircles(false)

        // Create a LineData object and set the LineDataSet
        val lineData = LineData(dataSet)

        // Set the LineData object to the chart
        binding?.signalView?.data = lineData

        // Refresh the chart
        binding?.signalView?.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun predict(fileName: String) {
        val contextWrapper = ContextWrapper(this);
        val externalStorage: File = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
        val audioDirPath = externalStorage.absolutePath

        try {
            val audioFilePath = "$audioDirPath/$fileName"
            if (!TextUtils.isEmpty(fileName)) {
                try {
                    val result = classifyNoise(audioFilePath)

                    if (result != null) {
                        list.add(result)
                        adapter?.setList(list)
                        binding?.resultList?.adapter = adapter
                    }
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this@PredictResultActivity, e.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this@PredictResultActivity, "Please enter a message.", Toast.LENGTH_LONG).show();
            }
        } catch (e: NullPointerException) {
            binding?.statusTitle?.text = "Please choose the wav file!"
            Toast.makeText(this@PredictResultActivity, "Spinner still null", Toast.LENGTH_LONG).show();
        }
    }

    private fun classifyNoise(audioFilePath: String): String? {

        val mNumFrames: Int
        val mSampleRate: Int
        val mChannels: Int
        var meanMFCCValues : FloatArray = FloatArray(1)

        var predictedResult: String? = "Unknown"

        var wavFile: WavFile? = null
        try {
            wavFile = WavFile.openWavFile(File(audioFilePath))
            mNumFrames = wavFile.numFrames.toInt()
            mSampleRate = wavFile.sampleRate.toInt()
            mChannels = wavFile.numChannels
            val buffer =
                Array(mChannels) { DoubleArray(mNumFrames) }

            var frameOffset = 0
            val loopCounter: Int = mNumFrames * mChannels / 4096 + 1
            for (i in 0 until loopCounter) {
                frameOffset = wavFile.readFrames(buffer, mNumFrames, frameOffset)
            }

            //trimming the magnitude values to 5 decimal digits
            val df = DecimalFormat("#.#####")
            df.roundingMode = RoundingMode.CEILING
            val meanBuffer = DoubleArray(mNumFrames)
            for (q in 0 until mNumFrames) {
                var frameVal = 0.0
                for (p in 0 until mChannels) {
                    frameVal += buffer[p][q]
                }
                meanBuffer[q] = df.format(frameVal / mChannels).toDouble()
            }


            //MFCC java library.
            val mfccConvert = MFCC()
            mfccConvert.setSampleRate(mSampleRate)

            val nMFCC = 40
            mfccConvert.setN_mfcc(nMFCC)
            val mfccInput = mfccConvert.process(meanBuffer)
            val nFFT = mfccInput.size / nMFCC
            val mfccValues =
                Array(nMFCC) { DoubleArray(nFFT) }

            //loop to convert the mfcc values into multi-dimensional array
            for (i in 0 until nFFT) {
                var indexCounter = i * nMFCC
                val rowIndexValue = i % nFFT
                for (j in 0 until nMFCC) {
                    mfccValues[j][rowIndexValue] = mfccInput[indexCounter].toDouble()
                    indexCounter++
                }
            }

            //code to take the mean of mfcc values across the rows such that
            //[nMFCC x nFFT] matrix would be converted into
            //[nMFCC x 1] dimension - which would act as an input to tflite model
            meanMFCCValues = FloatArray(nMFCC)
            for (p in 0 until nMFCC) {
                var fftValAcrossRow = 0.0
                for (q in 0 until nFFT) {
                    fftValAcrossRow += mfccValues[p][q]
                }
                val fftMeanValAcrossRow = fftValAcrossRow / nFFT
                meanMFCCValues[p] = fftMeanValAcrossRow.toFloat()
            }


        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: WavFileException) {
            e.printStackTrace()
        }
        Log.v("Mfcc :", meanMFCCValues.contentToString())
        predictedResult = loadModelAndMakePredictions(meanMFCCValues)

        return predictedResult
        Log.d("Predicted result", predictedResult!!)
    }


    private fun loadModelAndMakePredictions(meanMFCCValues: FloatArray) : String? {

        var predictedResult: String? = "unknown"

        //load the TFLite model in 'MappedByteBuffer' format using TF Interpreter
        val tfliteModel: MappedByteBuffer =
            FileUtil.loadMappedFile(this, getModelPath())
        val tflite: Interpreter

        /** Options for configuring the Interpreter.  */
        val tfliteOptions =
            Interpreter.Options()
        tfliteOptions.setNumThreads(2)
        tflite = Interpreter(tfliteModel, tfliteOptions)

        //obtain the input and output tensor size required by the model
        //for urban sound classification, input tensor should be of 1x40x1x1 shape
        val imageTensorIndex = 0
        val imageShape =
            tflite.getInputTensor(imageTensorIndex).shape()
        val imageDataType: DataType = tflite.getInputTensor(imageTensorIndex).dataType()
        val probabilityTensorIndex = 0
        val probabilityShape =
            tflite.getOutputTensor(probabilityTensorIndex).shape()
        val probabilityDataType: DataType =
            tflite.getOutputTensor(probabilityTensorIndex).dataType()

        //need to transform the MFCC 1d float buffer into 1x40x1x1 dimension tensor using TensorBuffer
        val inBuffer: TensorBuffer = TensorBuffer.createDynamic(imageDataType)
        inBuffer.loadArray(meanMFCCValues, imageShape)
        val inpBuffer: ByteBuffer = inBuffer.buffer
        val outputTensorBuffer: TensorBuffer =
            TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)
        //run the predictions with input and output buffer tensors to get probability values across the labels
        tflite.run(inpBuffer, outputTensorBuffer.buffer)


        //Code to transform the probability predictions into label values
        val ASSOCIATED_AXIS_LABELS = "labels.txt"
        var associatedAxisLabels: List<String?>? = null
        try {
            associatedAxisLabels = FileUtil.loadLabels(this, ASSOCIATED_AXIS_LABELS)
        } catch (e: IOException) {
            Log.e("tfliteSupport", "Error reading label file", e)
        }

        //Tensor processor for processing the probability values and to sort them based on the descending order of probabilities
        val probabilityProcessor: TensorProcessor = TensorProcessor.Builder()
            .add(NormalizeOp(0.0f, 255.0f)).build()
        if (null != associatedAxisLabels) {
            // Map of labels and their corresponding probability
            val labels = TensorLabel(
                associatedAxisLabels,
                probabilityProcessor.process(outputTensorBuffer)

            )

            // Create a map to access the result based on label
            val floatMap: Map<String, Float> =
                labels.mapWithFloatValue

            //function to retrieve the top K probability values, in this case 'k' value is 1.
            //retrieved values are storied in 'Recognition' object with label details.
            val resultPrediction: List<Recognition>? = getTopKProbability(floatMap);
            //get the top 1 prediction from the retrieved list of top predictions
            predictedResult = getPredictedValue(resultPrediction)
            Log.d("Predict :", outputTensorBuffer.floatArray.contentToString())


        }
        return predictedResult

    }


    private fun getPredictedValue(predictedList: List<Recognition>?): String?{
        val top1PredictedValue : Recognition? = predictedList?.get(0)
        return top1PredictedValue?.getTitle()
    }

    private fun getModelPath(): String {
        // you can download this file from
        // see build.gradle for where to obtain this file. It should be auto
        // downloaded into assets.
        return "model.tflite"
    }

    /** Gets the top-k results.  */
    private fun getTopKProbability(labelProb: Map<String, Float>): List<Recognition>? {
        // Find the best classifications.
        val MAX_RESULTS: Int = 1
        val pq: PriorityQueue<Recognition> = PriorityQueue(
            MAX_RESULTS,
            Comparator<Recognition> { lhs, rhs -> // Intentionally reversed to put high confidence at the head of the queue.
                rhs.getConfidence().compareTo(lhs.getConfidence())

            })
        for (entry in labelProb.entries) {
            pq.add(Recognition("" + entry.key, entry.key, entry.value))
        }
        val recognitions: ArrayList<Recognition> = ArrayList()
        val recognitionsSize: Int = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        return recognitions
    }

    var Z = 0

//    class MyTimerTask : TimerTask() {
//        var is_running = false
//        override fun run() {
//            is_running = true
//            //        Toast.makeText(getApplicationContext(),"Ganteng",Toast.LENGTH_SHORT).show();
//            if (ecgAllData.size > 3) {
//                val data: Float = ecgAllData.get(0)
//                linedata.addEntry(Entry(Z.toFloat(), data), 0) //INSERT LAST
//                Z++
//                ecgAllData.removeAt(0)
//                runOnUiThread(Runnable { //                        lineChart.setData(linedata);
//                    if (Z > 200) {
//                        //                            dataset.removeFirst();
//                        lineChart.setVisibleXRangeMaximum(1000f)
//                        lineChart.moveViewToX((Z - 200).toFloat())
//                        //                            lineChart.centerViewTo(Z-200,0, YAxis.AxisDependency.RIGHT);
//                        //                            dataset.removeEntry(0);
//                    }
//                    lineChart.notifyDataSetChanged()
//                    lineChart.invalidate()
//                })
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun fetchDetailPredict(id: Int, audioDirPath: String) {
        viewModel.getPredict(id).observe(this) {
            if (it != null) {
                when (it) {
                    is RequestState.Loading -> loading?.show()
                    is RequestState.Success -> {
                        loading?.hide()
                        binding?.fileName?.text = it.data.data?.get(0)?.suara
                        binding?.doctor?.text = "Dokter : Galih Santoso"
                        binding?.date?.text = it.data.data?.get(0)?.createdAt?.let { date -> formatDate(date) }
                        binding?.status?.text = it.data.data?.get(0)?.jenis
                        val audioFilePath = "$audioDirPath/${it.data.data?.get(0)?.suara}"
                        try {
                            processWavFileData(audioFilePath)
                        } catch (e: Exception) {
                            val fileUrl = "https://vhd.telekardiologi.com/${it.data.data?.get(0)?.filePath}"
                            downloadFile(this, fileUrl, it.data.data?.get(0)?.suara ?: "unknown", audioFilePath)
                        }

                        if (it.data.data?.get(0)?.result != "5") {
                            binding?.frame?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.red))
                            binding?.icon?.setImageResource(R.drawable.x_circle)
                        }

                        binding?.predict?.setOnClickListener { v ->
                            it.data.data?.get(0)?.suara?.let { fileName -> predict(fileName) }
                        }
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

    private fun formatDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
        val outputFormat = SimpleDateFormat("d MMMM yyyy HH:mm", Locale.US)

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun downloadFile(context: Context, url: String, fileName: String, audioFilePath: String) {
        loading?.show()
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Downloading File")
        request.setDescription("Downloading...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_RECORDINGS, fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        CoroutineScope(Dispatchers.IO).launch {
            delay(3000)
            processWavFileData(audioFilePath)
            loading?.hide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
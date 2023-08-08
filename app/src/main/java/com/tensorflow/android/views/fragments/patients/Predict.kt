package com.tensorflow.android.views.fragments.patients

import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tensorflow.android.R
import com.tensorflow.android.adapters.DeviceListAdapter
import com.tensorflow.android.adapters.FileListAdapter
import com.tensorflow.android.databinding.FragmentPredictBinding
import com.tensorflow.android.databinding.FragmentRecordBinding
import com.tensorflow.android.listeners.OnFileResultClickListener
import com.tensorflow.android.models.DeviceModel
import com.tensorflow.android.models.FileModel
import com.tensorflow.android.views.PredictResultActivity
import java.io.File

class Predict : Fragment() {
    private var _binding: FragmentPredictBinding? = null
    private val binding get() = _binding!!
    private var adapter: FileListAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private val filteredList = ArrayList<FileModel>()
    private val fileList = ArrayList<FileModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictBinding.inflate(inflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contextWrapper = ContextWrapper(requireContext());
        val externalStorage: File = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!

        val audioDirPath = externalStorage.absolutePath

        File(audioDirPath).walk().forEach {
            if(it.absolutePath.endsWith(".wav")) fileList.add(FileModel(it.name, it.lastModified()))
        }

        setupFileList(fileList)

        adapter?.onFileResultClickListener(object: OnFileResultClickListener{
            override fun onFileResultClick(file: FileModel) {
                startActivity(Intent(requireContext(), PredictResultActivity::class.java).putExtra("FILE", file))
            }
        })

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                filterData(searchText)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFileList(list: ArrayList<FileModel>) {
        adapter = FileListAdapter(list)
        layoutManager = LinearLayoutManager(requireContext())
        binding.fileList.layoutManager = layoutManager
        binding.fileList.adapter = adapter
    }

    private fun filterData(query: String) {
        filteredList.clear()

        for (item in fileList) {
            if (item.name?.contains(query, ignoreCase = true) == true) filteredList.add(item)
        }

        setupFileList(filteredList)
    }
}
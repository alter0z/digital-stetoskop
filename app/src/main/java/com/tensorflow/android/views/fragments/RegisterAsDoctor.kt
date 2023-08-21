package com.tensorflow.android.views.fragments

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.tensorflow.android.R
import com.tensorflow.android.databinding.FragmentRegisterAsDoctorBinding
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.utils.LoadingDialog
import com.tensorflow.android.utils.UserPreferences
import com.tensorflow.android.viewmodels.AuthViewModel
import com.tensorflow.android.views.patients.PatientMainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RegisterAsDoctor : Fragment() {
    private var _binding: FragmentRegisterAsDoctorBinding? = null
    private val binding get() = _binding!!
    private var loading: LoadingDialog? = null
    private val viewModel: AuthViewModel by viewModels()
    private var userPreferences: UserPreferences? = null
    private var filePath: String? = null
    private var filePaths: ArrayList<String>? = null
    private var fileUris: ArrayList<Uri>? = null
    private var gender: String = "Laki-Laki"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterAsDoctorBinding.inflate(inflater,container,false)
        if (requireActivity().checkSelfPermission(REQUIRED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED) requireActivity().requestPermissions(REQUIRED_PERMISSIONS, 1)
        loading = LoadingDialog((activity as AppCompatActivity?)!!)
        userPreferences = UserPreferences(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPassworVisibility()
        filePaths = ArrayList()
        fileUris = ArrayList()

        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.gender.adapter = adapter
        }

        binding.gender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGender = parent?.getItemAtPosition(position).toString()
                gender = selectedGender
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.sip.setOnClickListener {
//            if (filePaths?.size == 2) {
//                filePaths?.removeAt(0)
//                fileUris?.removeAt(0)
//            } else if (filePaths?.size == 1) {
//                filePaths?.clear()
//                fileUris?.removeAt(0)
//            }
            startPicFile()
        }
        binding.ktp.setOnClickListener { startPicFile() }
    }

    private fun setupPassworVisibility() {
        binding.showPassword.setOnClickListener{
            binding.showPassword.visibility = View.INVISIBLE
            binding.hidePassword.visibility = View.VISIBLE
            binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        binding.hidePassword.setOnClickListener{
            binding.hidePassword.visibility = View.INVISIBLE
            binding.showPassword.visibility = View.VISIBLE
            binding.password.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }

        binding.showConfirmPassword.setOnClickListener{
            binding.showConfirmPassword.visibility = View.INVISIBLE
            binding.hideConfirmPassword.visibility = View.VISIBLE
            binding.passwordConfirm.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        binding.hideConfirmPassword.setOnClickListener{
            binding.hideConfirmPassword.visibility = View.INVISIBLE
            binding.showConfirmPassword.visibility = View.VISIBLE
            binding.passwordConfirm.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
    }

    private val launcherIntentFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val selectedFile: Uri = result.data?.data as Uri
            filePath = selectedFile.toString()
            filePaths?.add(filePath!!)
            fileUris?.add(selectedFile)
            if (filePaths?.size == 1) binding.sipFileName.text = fileUris?.get(0)?.let { getFileName(it) } else if (filePaths?.size == 2) {
                binding.sipFileName.text = fileUris?.get(0)?.let { getFileName(it) }
                binding.ktpFileName.text = fileUris?.get(1)?.let { getFileName(it) }
            }
        }
    }

    private fun startPicFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val chooser = Intent.createChooser(intent, "Pilih File")
        launcherIntentFile.launch(chooser)
    }

    fun register() {
        val name = binding.name.text.toString()
        val address = binding.address.text.toString()
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.passwordConfirm.text.toString()
        when {
            email.isEmpty() -> binding.email.error = "Email tidak boleh kosong!"
            !email.isValidEmail() -> binding.email.error = "Pola email salah!"
            name.isEmpty() -> binding.name.error = "Nama tidak boleh kosong!"
            address.isEmpty() -> binding.address.error = "Alamat tidak boleh kosong!"
            password.isEmpty() -> binding.password.error = "Buat password anda!"
            password.length < 8 -> binding.password.error = "Password minimal 8 karakter!"
            confirmPassword.isEmpty() -> binding.passwordConfirm.error = "Konfirmasi password anda!"
            password != confirmPassword -> binding.passwordConfirm.error = "Password tidak sama!"
            filePaths?.size!! <= 2 -> Toast.makeText(requireActivity(), "Lengkapi required dokumen", Toast.LENGTH_LONG).show()
            else -> {
                val nameBody = name.toRequestBody("text/plain".toMediaType())
                val addressBody = address.toRequestBody("text/plain".toMediaType())
                val genderBody = gender.toRequestBody("text/plain".toMediaType())
                val emailBody = email.toRequestBody("text/plain".toMediaType())
                val passwordBody = password.toRequestBody("text/plain".toMediaType())
                val sipFile = filePaths?.get(0)?.let { File(it) }
                val ktpFile = filePaths?.get(1)?.let { File(it) }
                val requestSipFile = sipFile?.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val requestKtpFile = ktpFile?.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val sipFilePart = requestSipFile?.let { MultipartBody.Part.createFormData("sip", sipFile.name, it) }
                val ktpFilePart = requestKtpFile?.let { MultipartBody.Part.createFormData("ktp", ktpFile.name, it) }
                viewModel.doctorRegister(nameBody, addressBody, genderBody, emailBody, passwordBody, sipFilePart!!, ktpFilePart!!).observe(this) {
                    if (it != null) {
                        when (it) {
                            is RequestState.Loading -> loading?.show()
                            is RequestState.Success -> {
                                userPreferences?.setLogin(true)
                                it.data.authorisation?.token?.let { token -> userPreferences?.setToken(token) }
                                it.data.user?.id?.let { id -> userPreferences?.setUserId(id) }
                                loading?.hide()
                                startActivity(Intent(requireActivity(), PatientMainActivity::class.java))
                                requireActivity().finish()
                            }

                            is RequestState.Error -> {
                                loading?.hide()
                                AlertDialog.Builder(requireActivity()).apply {
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
        }
    }

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    result = if (columnIndex != -1) {
                        it.getString(columnIndex)
                    } else {
                        uri.path
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}
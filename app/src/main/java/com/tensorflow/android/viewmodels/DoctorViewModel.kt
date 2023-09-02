package com.tensorflow.android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.models.response.auth.LoginResponse
import com.tensorflow.android.models.response.base.BaseDataListResponse
import com.tensorflow.android.models.response.base.BaseDataResponse
import com.tensorflow.android.models.response.base.User
import com.tensorflow.android.models.response.base.WavRecordResponse
import com.tensorflow.android.repositories.DoctorRepository
import com.tensorflow.android.repositories.PatientRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException

class DoctorViewModel: ViewModel() {
    private val repository = DoctorRepository()

    fun getUserById(id: Int): LiveData<RequestState<BaseDataResponse<User>>> = liveData {
        emit(RequestState.Loading)
        try {
            val response = repository.getUserById(id)
            emit(RequestState.Success(response))
        } catch (e: HttpException) {
            emit(RequestState.Error(try { e.response()?.errorBody()?.string()?.let { JSONObject(it).get("message") } } catch (e: JSONException) { e.localizedMessage } as String))
        }
    }

    fun sendFile(name: RequestBody, file: MultipartBody.Part): LiveData<RequestState<BaseDataResponse<WavRecordResponse>>> = liveData {
        emit(RequestState.Loading)
        try {
            val response = repository.sendFile(name, file)
            emit(RequestState.Success(response))
        } catch (e: HttpException) {
            emit(RequestState.Error(try { e.response()?.errorBody()?.string()?.let { JSONObject(it).get("error") } } catch (e: JSONException) { e.localizedMessage } as String))
        }
    }

    fun getAllPatient(): LiveData<RequestState<BaseDataListResponse<User>>> = liveData {
        emit(RequestState.Loading)
        try {
            val response = repository.getAllPatient()
            emit(RequestState.Success(response))
        } catch (e: HttpException) {
            emit(RequestState.Error(try { e.response()?.errorBody()?.string()?.let { JSONObject(it).get("error") } } catch (e: JSONException) { e.localizedMessage } as String))
        }
    }

    fun getUserPredict(id: Int): LiveData<RequestState<BaseDataListResponse<WavRecordResponse>>> = liveData {
        emit(RequestState.Loading)
        try {
            val response = repository.getUserPredict(id)
            emit(RequestState.Success(response))
        } catch (e: HttpException) {
            emit(RequestState.Error(try { e.response()?.errorBody()?.string()?.let { JSONObject(it).get("error") } } catch (e: JSONException) { e.localizedMessage } as String))
        }
    }
}
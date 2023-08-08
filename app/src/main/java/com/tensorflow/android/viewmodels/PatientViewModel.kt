package com.tensorflow.android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.models.response.auth.LoginResponse
import com.tensorflow.android.models.response.base.BaseDataResponse
import com.tensorflow.android.models.response.base.User
import com.tensorflow.android.repositories.PatientRepository
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException

class PatientViewModel: ViewModel() {
    private val repository = PatientRepository()

    fun getUserById(id: Int): LiveData<RequestState<BaseDataResponse<User>>> = liveData {
        emit(RequestState.Loading)
        try {
            val response = repository.getUserById(id)
            emit(RequestState.Success(response))
        } catch (e: HttpException) {
            emit(RequestState.Error(try { e.response()?.errorBody()?.string()?.let { JSONObject(it).get("message") } } catch (e: JSONException) { e.localizedMessage } as String))
        }
    }
}
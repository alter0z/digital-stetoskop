package com.tensorflow.android.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.tensorflow.android.repositories.AuthRepository
import com.tensorflow.android.services.api.RequestState
import com.tensorflow.android.services.response.auth.LoginResponse
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException

class AuthViewModel: ViewModel() {
    private val repository = AuthRepository()

    fun login(email: RequestBody, password: RequestBody): LiveData<RequestState<LoginResponse>> = liveData {
        emit(RequestState.Loading)
        try {
            val response = repository.login(email, password)
            emit(RequestState.Success(response))
        } catch (e: HttpException) {
            emit(RequestState.Error(try { e.response()?.errorBody()?.string()?.let { JSONObject(it).get("message") } } catch (e: JSONException) { e.localizedMessage } as String))
        }
    }
}
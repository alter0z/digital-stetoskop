package com.tensorflow.android.services.api

import com.tensorflow.android.models.response.auth.LoginResponse
import com.tensorflow.android.models.response.auth.RegisterResponse
import com.tensorflow.android.models.response.base.BaseDataListResponse
import com.tensorflow.android.models.response.base.BaseDataResponse
import com.tensorflow.android.models.response.base.User
import com.tensorflow.android.models.response.base.WavRecordResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @Multipart
    @POST("login")
    suspend fun login(@Part("email") email: RequestBody, @Part("password") password: RequestBody): LoginResponse
    @Multipart
    @POST("register_pasien")
    suspend fun patientRegister(@Part("nama_lengkap") name: RequestBody,
                                @Part("alamat") address: RequestBody,
                                @Part("gender") gender: RequestBody,
                                @Part("email") email: RequestBody,
                                @Part("password") password: RequestBody): RegisterResponse
    @Multipart
    @POST("register_pasien")
    suspend fun doctorRegister(@Part("nama_lengklap") name: RequestBody,
                               @Part("alamat") address: RequestBody,
                               @Part("gender") gender: RequestBody,
                               @Part("email") email: RequestBody,
                               @Part("password") password: RequestBody,
                               @Part ktp: MultipartBody.Part,
                               @Part sip: MultipartBody.Part): RegisterResponse

    @GET("user/{id}")
    suspend fun getUserById(@Path("id") id: Int): BaseDataResponse<User>

    @Multipart
    @POST("ownCheck")
    suspend fun sendSignalFile(@Part("user_id") id: RequestBody,
                               @Part file: MultipartBody.Part): BaseDataResponse<WavRecordResponse>

    @GET("prediksi/user/{id}")
    suspend fun getUserPredict(@Path("id") id: Int): BaseDataListResponse<WavRecordResponse>

    @GET("prediksi/{id}")
    suspend fun getPredict(@Path("id") id: Int): BaseDataListResponse<WavRecordResponse>

    @GET("pasien")
    suspend fun getAllPatient(): BaseDataListResponse<User>
}
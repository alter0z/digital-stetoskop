package com.tensorflow.android.services.api

sealed class RequestState<out R> private constructor() {
    data class Success<out T>(val data: T): RequestState<T>()
    data class Error(val message: String): RequestState<Nothing>()
    object Loading: RequestState<Nothing>()
}

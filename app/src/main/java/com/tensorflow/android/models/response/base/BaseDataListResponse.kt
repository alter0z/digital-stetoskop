package com.tensorflow.android.models.response.base

import com.google.gson.annotations.SerializedName

data class BaseDataListResponse<out T>(
    @field:SerializedName("data")
    val data: List<T>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("status")
    val status: String? = null
)

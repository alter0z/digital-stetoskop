package com.tensorflow.android.models.response.base

import com.google.gson.annotations.SerializedName

data class BaseDataResponse<out T>(
    @field:SerializedName("data")
    val data: T? = null,

    @field:SerializedName("message")
    val message: String? = null,
)

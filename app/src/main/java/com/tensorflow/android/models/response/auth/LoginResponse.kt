package com.tensorflow.android.models.response.auth

import com.google.gson.annotations.SerializedName
import com.tensorflow.android.models.response.base.Authorisation
import com.tensorflow.android.models.response.base.User

data class LoginResponse(

    @field:SerializedName("authorisation")
	val authorisation: Authorisation? = null,

    @field:SerializedName("user")
	val user: User? = null,

    @field:SerializedName("status")
	val status: String? = null
)
package com.tensorflow.android.models.response.base

import com.google.gson.annotations.SerializedName

data class Authorisation(

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("token")
	val token: String? = null
)
package com.tensorflow.android.models.response.base

import com.google.gson.annotations.SerializedName

data class WavRecordResponse(

	@field:SerializedName("result")
	val result: String? = null,

	@field:SerializedName("file_path")
	val filePath: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("suara")
	val suara: String? = null,

	@field:SerializedName("jenis")
	val jenis: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
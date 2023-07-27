package com.tensorflow.android.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileModel(val name: String?, val date: Long?): Parcelable

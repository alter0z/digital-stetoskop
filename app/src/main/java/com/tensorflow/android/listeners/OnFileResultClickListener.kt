package com.tensorflow.android.listeners

import com.tensorflow.android.models.FileModel

interface OnFileResultClickListener {
    fun onFileResultClick(file: FileModel)
}
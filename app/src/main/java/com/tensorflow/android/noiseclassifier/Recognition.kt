package com.tensorflow.android.noiseclassifier


class Recognition(id: String, title: String, private var confidence: Float) {

    private var id : String? = id
    private var title : String? = title

    fun getId(): String? {
        return id
    }

    fun getTitle(): String? {
        return title
    }

    fun getConfidence(): Float {
        return confidence
    }
}
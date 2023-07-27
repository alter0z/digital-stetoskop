package com.tensorflow.android.listeners

import android.bluetooth.BluetoothSocket

interface OnSocketConnectedListener {
    fun onSocketConnected(socket: BluetoothSocket)
}
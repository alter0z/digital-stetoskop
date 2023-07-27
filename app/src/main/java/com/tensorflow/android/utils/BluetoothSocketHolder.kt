package com.tensorflow.android.utils

import android.bluetooth.BluetoothSocket

object BluetoothSocketHolder {
    private var bluetoothSocket: BluetoothSocket? = null

    fun setBluetoothSocket(socket: BluetoothSocket) {
        bluetoothSocket = socket
    }

    fun getBluetoothSocket(): BluetoothSocket? {
        return bluetoothSocket
    }
}
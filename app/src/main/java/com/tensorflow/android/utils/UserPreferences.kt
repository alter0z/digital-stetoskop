package com.tensorflow.android.utils

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    init {
        sharedPreferences = context.getSharedPreferences("AppKey",0)
        editor = sharedPreferences?.edit()
        editor?.apply()
    }

    fun setToken(token: String) {
        editor?.putString("TOKEN", token)
        editor?.commit()
    }

    fun getToken(): String? = sharedPreferences?.getString("TOKEN", null)

    fun setLogin(condition: Boolean) {
        editor?.putBoolean("LOGIN", condition)
        editor?.commit()
    }

    fun isLogin(): Boolean? = sharedPreferences?.getBoolean("LOGIN", false)
}
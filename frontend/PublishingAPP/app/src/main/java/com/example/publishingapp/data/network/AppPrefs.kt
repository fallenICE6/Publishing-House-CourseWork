package com.example.publishingapp.data.network

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object AppPrefs {

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? = prefs.getString("token", null)

    fun saveUser(user: UserDto) {
        prefs.edit()
            .putString("user", Gson().toJson(user))
            .apply()
    }

    fun getUser(): UserDto? {
        val json = prefs.getString("user", null) ?: return null
        return Gson().fromJson(json, UserDto::class.java)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

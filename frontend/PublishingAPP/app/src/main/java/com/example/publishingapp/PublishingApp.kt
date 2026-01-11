package com.example.publishingapp

import android.app.Application
import com.example.publishingapp.data.network.AppPrefs
import com.example.publishingapp.data.repository.AuthRepository

class PublishingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppPrefs.init(this)
    }
}

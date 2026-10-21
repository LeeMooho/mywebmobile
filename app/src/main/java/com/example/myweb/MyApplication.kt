package com.example.myweb

import android.app.Application
import com.example.myweb.data.AppContainer

class MyApplication : Application() {
    val container by lazy { AppContainer(this) }
}

package com.uvisual.beauty

import android.app.Application
import com.uvisual.archi.BaseApplication

class BeautyApplication : BaseApplication() {
    companion object {
        private var _application: Application? = null
        val application: Application
            get() = _application!!
    }


    override fun onCreate() {
        super.onCreate()
        _application = this
    }
}
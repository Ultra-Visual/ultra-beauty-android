package com.uvisual.beauty.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel:ViewModel() {
    val text = MutableStateFlow("Demo")

    fun update() {
        MainScope().launch {
            text.emit("Demo1")
        }
    }
}
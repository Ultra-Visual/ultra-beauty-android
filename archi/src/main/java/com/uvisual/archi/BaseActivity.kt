package com.uvisual.archi

import androidx.activity.ComponentActivity

abstract class BaseActivity : ComponentActivity() {

    abstract fun initViewModel()
}
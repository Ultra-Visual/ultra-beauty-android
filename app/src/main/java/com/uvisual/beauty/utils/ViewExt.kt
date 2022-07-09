package com.uvisual.beauty.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.doOnNextLayout

inline fun View.doOnLayout(crossinline action: (view: View) -> Unit) {
    if (ViewCompat.isLaidOut(this) && !isLayoutRequested) {
        action(this)
    } else {
        doOnNextLayout { action(it) }
    }
}
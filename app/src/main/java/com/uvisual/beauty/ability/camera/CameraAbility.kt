package com.uvisual.beauty.ability.camera

import android.content.Context

abstract class CameraAbility : ICameraAbility {
    companion object {
        fun getInstance(context: Context): ICameraAbility {
            return Camera2Ability(context)
        }
    }


    var onPreviewFrame: ((data: ByteArray, width: Int, height: Int) -> Unit)? = null

}
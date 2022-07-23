package com.uvisual.beauty.ability.camera

import android.app.Activity

abstract class CameraAbility : ICameraAbility {
    companion object {
        fun getInstance(activity: Activity): ICameraAbility {
            return Camera2Ability(activity)
        }
    }


    override var onPreviewFrame: ((data: ByteArray, width: Int, height: Int) -> Unit)? = null

}
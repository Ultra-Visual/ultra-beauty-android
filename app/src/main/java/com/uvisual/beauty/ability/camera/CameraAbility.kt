package com.uvisual.beauty.ability.camera

abstract class CameraAbility : ICameraAbility {
    companion object {
        fun getInstance(): ICameraAbility {
            return Camera2Ability()
        }
    }


    var onPreviewFrame: ((data: ByteArray, width: Int, height: Int) -> Unit)? = null

}
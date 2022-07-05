package com.uvisual.beauty.ability.camera

interface ICameraAbility {

    var onPreviewFrame: ((data: ByteArray, width: Int, height: Int) -> Unit)?

    fun onResume(width: Int, height: Int)
    fun onPause()
    fun switchCamera()
    fun getCameraOrientation(): Int
    fun hasMultipleCamera(): Boolean
}
package com.uvisual.beauty.ability.camera

interface ICameraAbility {
    fun onResume(width: Int, height: Int)
    fun onPause()
    fun switchCamera()
    fun getCameraOrientation(): Int
    fun hasMultipleCamera(): Boolean
}
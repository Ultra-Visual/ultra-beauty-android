package com.uvisual.beauty.ability.camera.filter

import android.opengl.GLES30
import android.util.Log
import com.uvisual.beauty.utils.ResReadUtil

class ContrastFilter :
    CameraFilter(
        fragment = ResReadUtil.readAsserts("shader/contrast/filter_contrast_fragment_shader.glsl")
    ) {
    companion object {
        private const val TAG = "ContrastFilter"
    }
    private var contrastLocation: Int = -1
    var contrast: Float = 1f
        set(value) {
            field = value
            Log.d(TAG, "setContrast: $value")
            setFloat(contrastLocation, value)
        }

    override fun onInit() {
        super.onInit()
        contrastLocation = GLES30.glGetUniformLocation(glProgram, "contrast")
        Log.d(TAG, "onInit: program = $glProgram. location = $contrastLocation")
    }

    override fun onInitialized() {
        super.onInitialized()
        contrast = 1.5f
        Log.d(TAG, "onInitialized: contrast = $contrast")
    }

}
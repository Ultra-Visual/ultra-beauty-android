package com.uvisual.beauty.ability.camera.preview

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.widget.FrameLayout
import com.uvisual.beauty.ability.camera.Rotation

class CameraPreviewView : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private val previewAbility: PreviewAbility by lazy {
        PreviewAbility(context)
    }

    private lateinit var surfaceView: GLSurfaceView

    private fun init(context: Context, attrs: AttributeSet?) {
        surfaceView = GLSurfaceView(context, attrs)
        surfaceView.setEGLContextClientVersion(3)
        previewAbility.setGlSurfaceView(surfaceView)
        addView(surfaceView)
    }

    fun updateFrame(frame: ByteArray, width: Int, height: Int) {
        previewAbility.updateFrame(frame, width, height)
    }

    fun setRotation(rotation: Rotation) {
        previewAbility.setRotation(rotation)
    }

}
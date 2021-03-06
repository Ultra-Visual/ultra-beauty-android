package com.uvisual.beauty.ability.camera.preview

import android.content.Context
import android.opengl.GLSurfaceView
import com.uvisual.beauty.ability.camera.CameraRender
import com.uvisual.beauty.ability.camera.Rotation

class PreviewAbility(private val context: Context) {
    private lateinit var glSurfaceView: GLSurfaceView

    private val render: CameraRender by lazy {
        CameraRender()
    }

    fun setGlSurfaceView(view: GLSurfaceView) {
        glSurfaceView = view
        glSurfaceView.setRenderer(render)
    }

    fun updateFrame(frame: ByteArray, width: Int, height: Int) {
        render.onPreviewFrame(frame, width, height)
    }

    fun setRotation(rotation: Rotation) {
        render.setRotation(rotation)
    }
}
package com.uvisual.beauty.ability.camera

import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.uvisual.beauty.ability.camera.filter.CameraFilter
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.withLock

class CameraRender : GLSurfaceView.Renderer, GLTextureView.Render {

    companion object {
        const val NO_IMAGE = -1

        val CUBE = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
        )
    }

    private var filter: CameraFilter

    private var textureId = NO_IMAGE

    private var surfaceTexture: SurfaceTexture? = null

    private var backgroundRed = 0.0f
    private var backgroundGreen = 0.0f
    private var backgroundBlue = 0.0f

    private var outputWidth = 0
    private var outputHeight = 0

    private val runOnDraw: Queue<() -> Unit> by lazy {
        return@lazy LinkedList<() -> Unit>()
    }

    private val runOnDrawEnd: Queue<() -> Unit> by lazy {
        return@lazy LinkedList<() -> Unit>()
    }

    private val surfaceChangedLock: ReentrantLock by lazy {
        ReentrantLock()
    }

    private val surfaceChangeCondition: Condition by lazy {
        surfaceChangedLock.newCondition()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(backgroundRed, backgroundGreen, backgroundBlue, 1.0f)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        filter.ifNeedInit()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        outputWidth = width
        outputHeight = height
        GLES30.glViewport(0, 0, width, height)
        GLES30.glUseProgram(filter.getProgram())
        filter.onOutputSizeChanged(width, height)
        adjustImageScaling()
        surfaceChangedLock.withLock {
            surfaceChangeCondition.signalAll()
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        runAll(runOnDraw)
    }

    private fun runAll(runOnDraw: Queue<() -> Unit>, lock: ReentrantLock = ReentrantLock()) {
        lock.withLock {
            while (runOnDraw.isNotEmpty()) {
                runOnDraw.poll()?.invoke()
            }
        }

    }


    private fun adjustImageScaling() {
        TODO("Not yet implemented")
    }


}
package com.uvisual.beauty.ability.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.uvisual.beauty.ability.camera.filter.CameraFilter
import com.uvisual.beauty.nativelib.YuvDecoder
import com.uvisual.beauty.utils.OpenGlUtil
import java.nio.IntBuffer
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

    private var filter: CameraFilter = CameraFilter()

    private var textureId = NO_IMAGE

    private var surfaceTexture: SurfaceTexture? = null

    private var backgroundRed = 0.0f
    private var backgroundGreen = 0.0f
    private var backgroundBlue = 0.0f

    private var outputWidth = 0
    private var outputHeight = 0

    private var imageWidth = 0
    private var imageHeight = 0

    private var glRgbBuffer: IntBuffer? = null

    private val runOnDraw: Queue<() -> Unit> by lazy {
        return@lazy LinkedList<() -> Unit>()
    }

    private val runOnDrawEnd: Queue<() -> Unit> by lazy {
        return@lazy LinkedList<() -> Unit>()
    }

    private val runOnDrawLock: ReentrantLock by lazy {
        return@lazy ReentrantLock()
    }

    private val runOnDrawEndLock: ReentrantLock by lazy {
        return@lazy ReentrantLock()
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
        runAll(runOnDraw, runOnDrawLock)
    }

    private fun runAll(runOnDraw: Queue<() -> Unit>, lock: ReentrantLock) {
        lock.withLock {
            while (runOnDraw.isNotEmpty()) {
                runOnDraw.poll()?.invoke()
            }
        }

    }

    fun onPreviewFrame(data: ByteArray, width: Int, height: Int) {
        if (glRgbBuffer == null) {
            glRgbBuffer = IntBuffer.allocate(width * height)
        }
        if (runOnDraw.isEmpty()) {
            runOnDraw {
                YuvDecoder.yuvToRgba(data, width, height, glRgbBuffer!!.array())
                textureId = OpenGlUtil.loadTexture(glRgbBuffer!!, width, height, textureId)

                if (imageWidth != width) {
                    imageWidth = width
                    imageHeight = height
                    adjustImageScaling()
                }

//                filter.onDrawFrame(data, width, height)
                GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, glRgbBuffer)
                GLES30.glUseProgram(0)
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
                GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D,
                    0,
                    GLES30.GL_RGBA,
                    width,
                    height,
                    0,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE,
                    glRgbBuffer
                )
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
                runAll(runOnDrawEnd, runOnDrawEndLock)
            }
        }
    }

    fun setFilter(filter: CameraFilter) {

        runOnDraw {
            val prevFilter = this.filter
            this.filter = filter
            prevFilter?.dispose()
            filter.ifNeedInit()
            GLES30.glUseProgram(this.filter.getProgram())
            this.filter.onOutputSizeChanged(outputWidth, outputHeight)
        }
        this.filter = filter
    }

    fun deleteImage() {
        runOnDraw {
            GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = NO_IMAGE
        }
    }

    fun setImageBitmap(bitmap: Bitmap) {
        setImageBitmap(bitmap, true)
    }

    fun setImageBitmap(bitmap: Bitmap, recycle: Boolean) {
        runOnDraw {
            var resizeBitmap: Bitmap? = null
            if (bitmap.width % 2 == 1) {
                resizeBitmap = Bitmap.createBitmap(bitmap.width + 1, bitmap.height, Bitmap.Config.ARGB_8888).apply {
                    density = bitmap.density
                    val canvas = Canvas(this)
                    canvas.drawARGB(0x00, 0x00, 0x00, 0x00)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                }
            }

            textureId = OpenGlUtil.loadTexture(resizeBitmap ?: bitmap, textureId, recycle)
            imageWidth = bitmap.width
            imageHeight = bitmap.height
            adjustImageScaling()
        }
    }

//    fun setScaleType(scaleType: ImageView.ScaleType) {
//        runOnDraw {
//            filter.setScaleType(scaleType)
//            adjustImageScaling()
//        }
//    }

    internal fun getFrameWidth(): Int {
        return outputWidth
    }

    internal fun getFrameHeight(): Int {

        return outputHeight
    }

    private fun adjustImageScaling() {
        var outputWidth = this.outputWidth
        var outputHeight = this.outputHeight

    }

    internal fun runOnDraw(run: () -> Unit) {
        runOnDrawLock.withLock {
            runOnDraw.add(run)
        }
    }

    internal fun runOnDrawEnd(run: () -> Unit) {
        runOnDrawEndLock.withLock {
            runOnDrawEnd.add(run)
        }
    }


}
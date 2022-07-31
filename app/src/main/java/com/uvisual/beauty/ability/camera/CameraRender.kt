package com.uvisual.beauty.ability.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import com.uvisual.beauty.ability.camera.filter.CameraFilter
import com.uvisual.beauty.nativelib.YuvDecoder
import com.uvisual.beauty.utils.OpenGlUtil
import com.uvisual.beauty.utils.TextureRotationUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.withLock
import kotlin.math.max
import kotlin.math.round

class CameraRender : GLSurfaceView.Renderer, GLTextureView.Render {
    companion object {
        const val NO_IMAGE = -1

        val TEXTURE_NO_ROTATION = floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,

            1.0f, 0.0f
        )

        val CUBE = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
        )
        private const val TAG = "CameraRender"
    }

    private var filter: CameraFilter = CameraFilter()

    private var textureId = NO_IMAGE

    private val glCubBuffer: FloatBuffer by lazy {
        val buffer = ByteBuffer.allocateDirect(CUBE.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(CUBE).position(0)
        return@lazy buffer
    }

    private val glTextureBuffer: FloatBuffer by lazy {
        return@lazy ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    }

    private var surfaceTexture: SurfaceTexture? = null

    private var backgroundRed = 0.0f
    private var backgroundGreen = 0.0f
    private var backgroundBlue = 0.0f

    private var outputWidth = 0
    private var outputHeight = 0

    private var imageWidth = 0
    private var imageHeight = 0

    private var glRgbBuffer: IntBuffer? = null

    private var rotation = Rotation.NORMAL
    private var flipHorizontal = false
    private var flipVertical = false

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
        Log.d(TAG, "onSurfaceChanged: width = $width, height = $height")
        outputWidth = width
        outputHeight = height
        val scale = 768 / 1024.toFloat()
        val recordHeight = width * scale
        Log.d(TAG, "onSurfaceChanged: width = $width, h = $recordHeight")
        val y = (height - recordHeight) / 2
        GLES30.glViewport(0, y.toInt(), width, recordHeight.toInt())
        GLES30.glUseProgram(filter.getProgram())
        filter.onOutputSizeChanged(width, height)
        adjustImageScaling()
        surfaceChangedLock.withLock {
            surfaceChangeCondition.signalAll()
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d(TAG, "onDrawFrame: ")
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        runAll(runOnDraw, runOnDrawLock)
        filter.onDraw(textureId, glCubBuffer, glTextureBuffer)
        runAll(runOnDrawEnd, runOnDrawEndLock)
        surfaceTexture?.updateTexImage()
    }

    fun setRotation(rotation: Rotation) {
        this.rotation = rotation
        adjustImageScaling()
    }

    private fun runAll(runOnDraw: Queue<() -> Unit>, lock: ReentrantLock) {
        Log.d(TAG, "runAll: ${runOnDraw.size}")
        lock.withLock {
            while (runOnDraw.isNotEmpty()) {
                Log.d(TAG, "runAll: not empty")
                runOnDraw.poll()?.invoke()
            }
        }
    }

    fun onPreviewFrame(data: ByteArray, width: Int, height: Int) {
        if (glRgbBuffer?.capacity() != width * height) {
            glRgbBuffer = IntBuffer.allocate(width * height)
        }
        if (runOnDraw.isEmpty()) {
            Log.d(TAG, "onPreviewFrame")
            runOnDraw {
                YuvDecoder.yuvToRgba(data, width, height, glRgbBuffer!!.array())
                textureId = OpenGlUtil.loadTexture(glRgbBuffer!!, width, height, textureId)
                if (imageWidth != width) {
                    imageWidth = width
                    imageHeight = height
                    adjustImageScaling()
                }
            }
        }
    }

    fun setFilter(filter: CameraFilter) {

        runOnDraw {
            val prevFilter = this.filter
            this.filter = filter
            prevFilter.dispose()
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

        val ratio1: Float = outputWidth.toFloat() / imageWidth.toFloat()
        val ratio2: Float = outputHeight.toFloat() / imageHeight.toFloat()
        val ratioMax: Float = max(ratio1, ratio2)
        val imageWidth = round(imageWidth * ratioMax)
        val imageHeight = round(imageHeight * ratioMax)

        val ratioWidth: Float = imageWidth / outputWidth.toFloat()
        val ratioHeight: Float = imageHeight / outputHeight.toFloat()

        val cube = CUBE
        var textureCords = TextureRotationUtil.getRotation(rotation, flipHorizontal, flipVertical)
        val distHorizontal = (1 - 1 / ratioWidth) / 2
        val distVertical = (1 - 1 / ratioHeight) / 2
        textureCords = floatArrayOf(
            addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
            addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
            addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
            addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical)
        )
        glCubBuffer.clear()
        glCubBuffer.put(cube).position(0)
        glTextureBuffer.clear()
        glTextureBuffer.put(textureCords).position(0)
    }

    private fun addDistance(coordinate: Float, distance: Float): Float {
        return if (coordinate == 0f) distance else 1 - distance
    }

    internal fun runOnDraw(run: () -> Unit) {
        Log.d(TAG, "runOnDraw: 1")
        runOnDrawLock.withLock {
            runOnDraw.add(run)
        }
        Log.d(TAG, "runOnDraw: 2")
    }

    internal fun runOnDrawEnd(run: () -> Unit) {
        runOnDrawEndLock.withLock {
            runOnDrawEnd.add(run)
        }
    }


}
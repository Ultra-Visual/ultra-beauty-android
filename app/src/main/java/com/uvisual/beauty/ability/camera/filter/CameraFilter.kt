package com.uvisual.beauty.ability.camera.filter

import android.opengl.GLES30
import android.util.Log
import com.uvisual.beauty.utils.OpenGlUtil
import com.uvisual.beauty.utils.ResReadUtil
import java.nio.FloatBuffer
import java.util.*

open class CameraFilter(
    private val vertex: String = ResReadUtil.readAsserts("shader/no_filter_vertex_shader.glsl"),
    private val fragment: String = ResReadUtil.readAsserts("shader/no_filter_fragment_shader.glsl")
//    private val vertex: String = ResReadUtil.read(R.raw.vertex_camera_shader),
//    private val fragment: String = ResReadUtil.read(R.raw.fragment_camera_shader)
) {
    protected var glProgram: Int = 0
        get() {
            Log.d(TAG, "getglProgram: $field")
            return field
        }
    private var glAttribPosition = 0
    private var glUniformTexture = 0
    private var glAttribTextureCoordinate = 0

    private var outputWidth: Int = 0
    private var outputHeight: Int = 0

    private val runOnDraw: LinkedList<() -> Unit> = LinkedList()

    private var isInitialized = false

    init {
        ifNeedInit()
    }

    fun ifNeedInit() {
//        if (!isInitialized) {
            init()
//        }
    }

    private fun init() {
        onInit()
        onInitialized()
    }

    open fun onInitialized() {

    }

    open fun onInit() {
        Log.d(TAG, "onInit: ")
        glProgram = OpenGlUtil.loadProgram(vertex, fragment)
        Log.d(TAG, "onInit: loadProgram: $fragment")
        Log.d(TAG, "onInit: loadProgram. $glProgram")
        glAttribPosition = GLES30.glGetAttribLocation(glProgram, "position")
        glUniformTexture = GLES30.glGetUniformLocation(glProgram, "inputImageTexture")
        glAttribTextureCoordinate = GLES30.glGetAttribLocation(glProgram, "inputTextureCoordinate")
        isInitialized = true
    }

    fun getProgram(): Int {
        Log.d(Companion.TAG, "getProgram: $glProgram")
        return glProgram
    }

    fun onOutputSizeChanged(width: Int, height: Int) {
        outputWidth = width
        outputHeight = height
    }

    protected fun setFloat(location: Int, float: Float) {
        runOnDraw {
            Log.d(TAG, "setFloat: location = $location, float = $float")
            GLES30.glUniform1f(location, float)
        }
    }

    @Synchronized
    protected fun runOnDraw(runnable: () -> Unit) {
        runOnDraw.add(runnable)
    }

    fun onDraw(textureId: Int, cubeBuffer: FloatBuffer, textureBuffer: FloatBuffer) {
        GLES30.glUseProgram(glProgram)
        runPendingOnDrawTasks()

        // todo check is init

        cubeBuffer.position(0)
        GLES30.glEnableVertexAttribArray(glAttribPosition)
        GLES30.glVertexAttribPointer(glAttribPosition, 2, GLES30.GL_FLOAT, false, 0, cubeBuffer)
        textureBuffer.position(0)
        GLES30.glEnableVertexAttribArray(glAttribTextureCoordinate)
        GLES30.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        if (textureId != OpenGlUtil.NO_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
            GLES30.glUniform1i(glUniformTexture, 0)
        }
        onDrawArraysPre()
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glDisableVertexAttribArray(glAttribPosition)
        GLES30.glDisableVertexAttribArray(glAttribTextureCoordinate)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }

    private fun onDrawArraysPre() {

    }

    protected fun runPendingOnDrawTasks() {
        while (!runOnDraw.isEmpty()) {
            runOnDraw.removeFirst().invoke()
        }
    }

    fun dispose() {
        isInitialized = false
        GLES30.glDeleteProgram(glProgram)
        onDispose()
    }

    fun onDispose() {
    }

    companion object {
        private const val TAG = "CameraFilter"
    }


}
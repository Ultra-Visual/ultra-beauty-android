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
    private var glProgram: Int = 0
    private var glAttribPosition = 0
    private var glUniformTexture = 0
    private var glAttribTextureCoordinate = 0

    private var outputWidth: Int = 0
    private var outputHeight: Int = 0

    private val runOnDraw: LinkedList<() -> Unit> = LinkedList()

    init {
        ifNeedInit()
    }

    fun ifNeedInit() {
        init()
    }

    fun init() {
        onInit()
    }

    fun onInit() {
        Log.d(TAG, "onInit: ")
        glProgram = OpenGlUtil.loadProgram(vertex, fragment)
        glAttribPosition = GLES30.glGetAttribLocation(glProgram, "position")
        glUniformTexture = GLES30.glGetUniformLocation(glProgram, "inputImageTexture")
        glAttribTextureCoordinate = GLES30.glGetAttribLocation(glProgram, "inputTextureCoordinate")
    }

    fun getProgram(): Int {
        Log.d(Companion.TAG, "getProgram: $glProgram")
        return glProgram
    }

    fun onOutputSizeChanged(width: Int, height: Int) {
        outputWidth = width
        outputHeight = height
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
    }

    companion object {
        private const val TAG = "CameraFilter"
    }


}
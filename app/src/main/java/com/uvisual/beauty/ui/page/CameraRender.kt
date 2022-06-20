package com.uvisual.beauty.ui.page

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import com.uvisual.beauty.R
import com.uvisual.beauty.utils.RenderUtil
import com.uvisual.beauty.utils.ResReadUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private const val TAG = "CameraRender"

class CameraRender(val onSurfaceCreated: () -> Unit) : GLSurfaceView.Renderer {
    private val transformMatrix = FloatArray(16)

    /**
     * 顶点坐标
     * (x,y,z)
     */
    private val POSITION_VERTEX = floatArrayOf(
        0f, 0f, 0f,  //顶点坐标V0
        1f, 1f, 0f,  //顶点坐标V1
        -1f, 1f, 0f,  //顶点坐标V2
        -1f, -1f, 0f,  //顶点坐标V3
        1f, -1f, 0f //顶点坐标V4
    )

    /**
     * 索引
     */
    private val VERTEX_INDEX = shortArrayOf(
        0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
        0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
        0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
        0, 4, 1 //V0,V4,V1 三个顶点组成一个三角形
    )

    /**
     * 纹理坐标
     * (s,t)
     */
    private val TEX_VERTEX = floatArrayOf(
        0.5f, 0.5f,  //纹理坐标V0
        1f, 1f,  //纹理坐标V1
        0f, 1f,  //纹理坐标V2
        0f, 0.0f,  //纹理坐标V3
        1f, 0.0f //纹理坐标V4
    )
    private val vertexBuffer: FloatBuffer by lazy {
        val buffer = ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(POSITION_VERTEX)
        buffer.position(0)
        return@lazy buffer
    }

    private val texVertexBuffer: FloatBuffer by lazy {
        val buffer = ByteBuffer.allocateDirect(TEX_VERTEX.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(TEX_VERTEX)
        buffer.position(0)
        return@lazy buffer
    }

    private val vertexIndexBuffer: ShortBuffer by lazy {
        val buffer = ByteBuffer.allocateDirect(VERTEX_INDEX.size * 4)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(VERTEX_INDEX)
        buffer.position(0)
        return@lazy buffer
    }

    private var textureId = 0
    var surfaceTexture: SurfaceTexture? = null

    /**
     * 矩阵索引
     */
    private var textureMatrixLocation = 0

    private var textureSamplerLocation = 0

    private var program = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        textureId = loadTexture()
        surfaceTexture = SurfaceTexture(textureId)
        //设置背景颜色
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
        val vertexShader =
            RenderUtil.compileShader(GLES30.GL_VERTEX_SHADER, ResReadUtil.read(R.raw.vertex_camera_shader))
        val fragmentShader =
            RenderUtil.compileShader(GLES30.GL_FRAGMENT_SHADER, ResReadUtil.read(R.raw.fragment_camera_shader))
        program = RenderUtil.linkProgram(vertexShader, fragmentShader)
        textureMatrixLocation = GLES30.glGetUniformLocation(program, "uTextureMatrix")
        textureSamplerLocation = GLES30.glGetUniformLocation(program, "yuvTexCoords")
        onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d(TAG, "onDrawFrame: ")
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(program)

        surfaceTexture!!.updateTexImage()
        surfaceTexture!!.getTransformMatrix(transformMatrix)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES30.glUniform1i(textureSamplerLocation, 0)

        GLES30.glUniformMatrix4fv(textureMatrixLocation, 1, false, transformMatrix, 0)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, texVertexBuffer)

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, VERTEX_INDEX.size, GLES30.GL_UNSIGNED_SHORT, vertexIndexBuffer)
    }

    /**
     * 加载外部纹理
     *
     * @return
     */
    fun loadTexture(): Int {
        val tex = IntArray(1)
        //创建一个纹理
        GLES30.glGenTextures(1, tex, 0)
        //绑定到外部纹理上
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        //设置纹理过滤参数
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_NEAREST.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        //解除纹理绑定
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return tex[0]
    }
}
package com.uvisual.beauty.utils

import android.opengl.GLES30

object RenderUtil {
    fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        return shader
    }

    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShaderId)
        GLES30.glAttachShader(program, fragmentShaderId)
        GLES30.glLinkProgram(program)
        return program
    }
}
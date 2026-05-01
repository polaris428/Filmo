package com.polaris.filmo.core.renderer

import android.opengl.GLES20

class ShaderProgram private constructor(
    private val programId: Int
) {
    fun use() {
        GLES20.glUseProgram(programId)
    }

    fun getAttributeLocation(name: String): Int =
        GLES20.glGetAttribLocation(programId, name)

    fun setInt(name: String, value: Int) {
        GLES20.glUniform1i(uniformLocation(name), value)
    }

    fun setFloat(name: String, value: Float) {
        GLES20.glUniform1f(uniformLocation(name), value)
    }

    fun setVec2(name: String, x: Float, y: Float) {
        GLES20.glUniform2f(uniformLocation(name), x, y)
    }

    fun setFilmConfig(config: FilmShaderConfig) {
        setFloat("uContrast", config.contrast)
        setFloat("uSaturation", config.saturation)
        setFloat("uNoiseStrength", config.noiseStrength)
        setFloat("uVignetteStrength", config.vignetteStrength)
        setFloat("uLightLeakStrength", config.lightLeakStrength)
        setFloat("uChromaticAberrationStrength", config.chromaticAberrationStrength)
    }

    fun setMat4(name: String, value: FloatArray) {
        GLES20.glUniformMatrix4fv(uniformLocation(name), 1, false, value, 0)
    }

    fun release() {
        GLES20.glDeleteProgram(programId)
    }

    private fun uniformLocation(name: String): Int =
        GLES20.glGetUniformLocation(programId, name)

    companion object {
        fun compile(vertexSource: String, fragmentSource: String): ShaderProgram {
            val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource)
            val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
            val program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)

            if (linkStatus[0] == 0) {
                val log = GLES20.glGetProgramInfoLog(program)
                GLES20.glDeleteProgram(program)
                error("Shader program link failed: $log")
            }

            return ShaderProgram(program)
        }

        private fun compileShader(type: Int, source: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)

            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val log = GLES20.glGetShaderInfoLog(shader)
                GLES20.glDeleteShader(shader)
                error("Shader compile failed: $log")
            }
            return shader
        }
    }
}

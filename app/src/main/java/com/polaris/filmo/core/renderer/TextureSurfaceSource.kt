package com.polaris.filmo.core.renderer

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.view.Surface

class TextureSurfaceSource private constructor(
    val textureId: Int,
    val surfaceTexture: SurfaceTexture,
    val surface: Surface
) {
    fun release() {
        surface.release()
        surfaceTexture.release()
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
    }

    companion object {
        fun create(): TextureSurfaceSource {
            val textureIds = IntArray(1)
            GLES20.glGenTextures(1, textureIds, 0)
            val textureId = textureIds[0]

            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )

            val surfaceTexture = SurfaceTexture(textureId)
            return TextureSurfaceSource(
                textureId = textureId,
                surfaceTexture = surfaceTexture,
                surface = Surface(surfaceTexture)
            )
        }
    }
}

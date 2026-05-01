package com.polaris.filmo.core.renderer

import android.content.Context
import android.opengl.GLSurfaceView

class FilmGLSurfaceView(
    context: Context,
    private val filmRenderer: FilmRenderer
) : GLSurfaceView(context) {

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
        setRenderer(filmRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onDetachedFromWindow() {
        queueEvent {
            filmRenderer.release()
        }
        super.onDetachedFromWindow()
    }
}

package com.polaris.filmo.core.renderer

import android.content.Context
import android.opengl.GLSurfaceView

class FilmGLSurfaceView(
    context: Context,
    filmRenderer: FilmRenderer
) : GLSurfaceView(context) {

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
        setRenderer(filmRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}

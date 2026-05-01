package com.polaris.filmo.core.renderer

import android.view.Surface
import com.polaris.filmo.domain.model.FilmType

interface FilmRendererController {
    fun setFilm(filmType: FilmType): Result<Unit>
    fun getInputSurface(): Surface?
    fun captureCurrentFrame(callback: (Result<CapturedFrame>) -> Unit)
}

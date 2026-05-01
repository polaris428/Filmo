package com.polaris.filmo.core.renderer

import com.polaris.filmo.domain.model.FilmType

data class FilmShaderConfig(
    val fragmentShaderAsset: String,
    val contrast: Float,
    val saturation: Float,
    val noiseStrength: Float,
    val vignetteStrength: Float,
    val lightLeakStrength: Float,
    val chromaticAberrationStrength: Float
) {
    companion object {
        fun forFilm(filmType: FilmType): FilmShaderConfig =
            when (filmType) {
                FilmType.Portra400 -> FilmShaderConfig(
                    fragmentShaderAsset = "portra.frag",
                    contrast = 1.04f,
                    saturation = 0.92f,
                    noiseStrength = 0.045f,
                    vignetteStrength = 0.15f,
                    lightLeakStrength = 0.00f,
                    chromaticAberrationStrength = 0.000f
                )

                FilmType.Velvia50 -> FilmShaderConfig(
                    fragmentShaderAsset = "velvia.frag",
                    contrast = 1.12f,
                    saturation = 1.28f,
                    noiseStrength = 0.025f,
                    vignetteStrength = 0.10f,
                    lightLeakStrength = 0.00f,
                    chromaticAberrationStrength = 0.000f
                )

                FilmType.IlfordHp5 -> FilmShaderConfig(
                    fragmentShaderAsset = "ilford.frag",
                    contrast = 1.22f,
                    saturation = 0.00f,
                    noiseStrength = 0.085f,
                    vignetteStrength = 0.22f,
                    lightLeakStrength = 0.00f,
                    chromaticAberrationStrength = 0.000f
                )

                FilmType.Lomography -> FilmShaderConfig(
                    fragmentShaderAsset = "lomography.frag",
                    contrast = 1.15f,
                    saturation = 1.16f,
                    noiseStrength = 0.075f,
                    vignetteStrength = 0.45f,
                    lightLeakStrength = 0.18f,
                    chromaticAberrationStrength = 0.003f
                )

                FilmType.ExpiredFilm -> FilmShaderConfig(
                    fragmentShaderAsset = "expired.frag",
                    contrast = 0.92f,
                    saturation = 0.72f,
                    noiseStrength = 0.110f,
                    vignetteStrength = 0.28f,
                    lightLeakStrength = 0.24f,
                    chromaticAberrationStrength = 0.002f
                )
            }
    }
}

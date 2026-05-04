package com.polaris.filmo.core.renderer

import com.polaris.filmo.domain.model.FilmAdjustment
import com.polaris.filmo.domain.model.FilmType

data class FilmShaderConfig(
    val fragmentShaderAsset: String,
    val temperature: Float,
    val tint: Float,
    val contrast: Float,
    val saturation: Float,
    val noiseStrength: Float,
    val vignetteStrength: Float,
    val lightLeakStrength: Float,
    val chromaticAberrationStrength: Float
) {
    fun withAdjustment(adjustment: FilmAdjustment): FilmShaderConfig =
        copy(
            temperature = adjustment.temperature,
            tint = adjustment.tint,
            contrast = contrast * adjustment.contrast,
            saturation = saturation * adjustment.saturation,
            noiseStrength = adjustment.grain,
            vignetteStrength = adjustment.vignette
        )

    companion object {
        fun forFilm(filmType: FilmType): FilmShaderConfig =
            when (filmType) {
                FilmType.Portra400 -> FilmShaderConfig(
                    fragmentShaderAsset = "portra.frag",
                    temperature = 0.20f,
                    tint = 0.06f,
                    contrast = 1.04f,
                    saturation = 0.92f,
                    noiseStrength = 0.045f,
                    vignetteStrength = 0.15f,
                    lightLeakStrength = 0.00f,
                    chromaticAberrationStrength = 0.000f
                )

                FilmType.Velvia50 -> FilmShaderConfig(
                    fragmentShaderAsset = "velvia.frag",
                    temperature = -0.06f,
                    tint = -0.04f,
                    contrast = 1.12f,
                    saturation = 1.28f,
                    noiseStrength = 0.025f,
                    vignetteStrength = 0.10f,
                    lightLeakStrength = 0.00f,
                    chromaticAberrationStrength = 0.000f
                )

                FilmType.IlfordHp5 -> FilmShaderConfig(
                    fragmentShaderAsset = "ilford.frag",
                    temperature = 0.00f,
                    tint = 0.00f,
                    contrast = 1.22f,
                    saturation = 0.00f,
                    noiseStrength = 0.085f,
                    vignetteStrength = 0.22f,
                    lightLeakStrength = 0.00f,
                    chromaticAberrationStrength = 0.000f
                )

                FilmType.Lomography -> FilmShaderConfig(
                    fragmentShaderAsset = "lomography.frag",
                    temperature = 0.10f,
                    tint = 0.12f,
                    contrast = 1.15f,
                    saturation = 1.16f,
                    noiseStrength = 0.075f,
                    vignetteStrength = 0.45f,
                    lightLeakStrength = 0.18f,
                    chromaticAberrationStrength = 0.003f
                )

                FilmType.ExpiredFilm -> FilmShaderConfig(
                    fragmentShaderAsset = "expired.frag",
                    temperature = 0.18f,
                    tint = -0.10f,
                    contrast = 0.92f,
                    saturation = 0.72f,
                    noiseStrength = 0.110f,
                    vignetteStrength = 0.28f,
                    lightLeakStrength = 0.24f,
                    chromaticAberrationStrength = 0.002f
                )

                FilmType.AnalogScan -> FilmShaderConfig(
                    fragmentShaderAsset = "analog_scan.frag",
                    temperature = -0.08f,
                    tint = -0.16f,
                    contrast = 1.08f,
                    saturation = 0.72f,
                    noiseStrength = 0.140f,
                    vignetteStrength = 0.30f,
                    lightLeakStrength = 0.04f,
                    chromaticAberrationStrength = 0.004f
                )

                FilmType.DateStamp -> FilmShaderConfig(
                    fragmentShaderAsset = "date_stamp.frag",
                    temperature = 0.14f,
                    tint = 0.03f,
                    contrast = 1.02f,
                    saturation = 0.90f,
                    noiseStrength = 0.060f,
                    vignetteStrength = 0.24f,
                    lightLeakStrength = 0.00f,
                    chromaticAberrationStrength = 0.001f
                )
            }
    }
}

package com.polaris.filmo.domain.model

data class FilmAdjustment(
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val grain: Float = 0.25f,
    val vignette: Float = 0.35f
) {
    fun withParameter(parameter: FilmAdjustmentParameter, value: Float): FilmAdjustment =
        when (parameter) {
            FilmAdjustmentParameter.Temperature -> copy(temperature = value)
            FilmAdjustmentParameter.Tint -> copy(tint = value)
            FilmAdjustmentParameter.Contrast -> copy(contrast = value)
            FilmAdjustmentParameter.Saturation -> copy(saturation = value)
            FilmAdjustmentParameter.Grain -> copy(grain = value)
            FilmAdjustmentParameter.Vignette -> copy(vignette = value)
        }
}

enum class FilmAdjustmentParameter {
    Temperature,
    Tint,
    Contrast,
    Saturation,
    Grain,
    Vignette
}

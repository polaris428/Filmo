package com.polaris.filmo.domain.model

enum class FilmType(
    val id: String,
    val displayName: String,
    val description: String,
    val thumbnailResName: String,
    val accentColorToken: String,
    val defaultAdjustment: FilmAdjustment
) {
    Portra400(
        id = "portra_400",
        displayName = "Portra 400",
        description = "Warm portrait tone",
        thumbnailResName = "film_portra_preview",
        accentColorToken = "film.portra",
        defaultAdjustment = FilmAdjustment(
            temperature = 0.20f,
            tint = 0.06f,
            contrast = 1.00f,
            saturation = 1.00f,
            grain = 0.22f,
            vignette = 0.18f
        )
    ),
    Velvia50(
        id = "velvia_50",
        displayName = "Velvia 50",
        description = "Vivid landscape tone",
        thumbnailResName = "film_velvia_preview",
        accentColorToken = "film.velvia",
        defaultAdjustment = FilmAdjustment(
            temperature = -0.06f,
            tint = -0.04f,
            contrast = 1.06f,
            saturation = 1.12f,
            grain = 0.14f,
            vignette = 0.14f
        )
    ),
    IlfordHp5(
        id = "ilford_hp5",
        displayName = "Ilford HP5",
        description = "Gritty monochrome tone",
        thumbnailResName = "film_ilford_preview",
        accentColorToken = "film.ilford",
        defaultAdjustment = FilmAdjustment(
            temperature = 0.00f,
            tint = 0.00f,
            contrast = 1.08f,
            saturation = 1.00f,
            grain = 0.48f,
            vignette = 0.26f
        )
    ),
    Lomography(
        id = "lomography",
        displayName = "Lomography",
        description = "Bold lomo tone",
        thumbnailResName = "film_lomography_preview",
        accentColorToken = "film.lomography",
        defaultAdjustment = FilmAdjustment(
            temperature = 0.10f,
            tint = 0.12f,
            contrast = 1.08f,
            saturation = 1.08f,
            grain = 0.38f,
            vignette = 0.48f
        )
    ),
    ExpiredFilm(
        id = "expired_film",
        displayName = "Expired Film",
        description = "Faded vintage tone",
        thumbnailResName = "film_expired_preview",
        accentColorToken = "film.expired",
        defaultAdjustment = FilmAdjustment(
            temperature = 0.18f,
            tint = -0.10f,
            contrast = 0.92f,
            saturation = 0.88f,
            grain = 0.52f,
            vignette = 0.34f
        )
    ),
    AnalogScan(
        id = "analog_scan",
        displayName = "Analog Scan",
        description = "Old camera scanline tone",
        thumbnailResName = "film_analog_scan_preview",
        accentColorToken = "film.analog_scan",
        defaultAdjustment = FilmAdjustment(
            temperature = -0.08f,
            tint = -0.16f,
            contrast = 1.04f,
            saturation = 0.78f,
            grain = 0.58f,
            vignette = 0.30f
        )
    ),
    DateStamp(
        id = "date_stamp",
        displayName = "Date Stamp",
        description = "Warm film with analog date imprint",
        thumbnailResName = "film_date_stamp_preview",
        accentColorToken = "film.date_stamp",
        defaultAdjustment = FilmAdjustment(
            temperature = 0.14f,
            tint = 0.03f,
            contrast = 0.98f,
            saturation = 0.94f,
            grain = 0.32f,
            vignette = 0.24f
        )
    )
}

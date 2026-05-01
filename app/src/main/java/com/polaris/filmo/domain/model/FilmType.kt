package com.polaris.filmo.domain.model

enum class FilmType(
    val id: String,
    val displayName: String,
    val description: String,
    val thumbnailResName: String,
    val accentColorToken: String
) {
    Portra400(
        id = "portra_400",
        displayName = "Portra 400",
        description = "Warm portrait tone",
        thumbnailResName = "film_portra_preview",
        accentColorToken = "film.portra"
    ),
    Velvia50(
        id = "velvia_50",
        displayName = "Velvia 50",
        description = "Vivid landscape tone",
        thumbnailResName = "film_velvia_preview",
        accentColorToken = "film.velvia"
    ),
    IlfordHp5(
        id = "ilford_hp5",
        displayName = "Ilford HP5",
        description = "Gritty monochrome tone",
        thumbnailResName = "film_ilford_preview",
        accentColorToken = "film.ilford"
    ),
    Lomography(
        id = "lomography",
        displayName = "Lomography",
        description = "Bold lomo tone",
        thumbnailResName = "film_lomography_preview",
        accentColorToken = "film.lomography"
    ),
    ExpiredFilm(
        id = "expired_film",
        displayName = "Expired Film",
        description = "Faded vintage tone",
        thumbnailResName = "film_expired_preview",
        accentColorToken = "film.expired"
    )
}

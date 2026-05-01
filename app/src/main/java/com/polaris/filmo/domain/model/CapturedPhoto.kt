package com.polaris.filmo.domain.model

data class CapturedPhoto(
    val id: String,
    val uri: String,
    val filmType: FilmType,
    val createdAt: Long,
    val width: Int,
    val height: Int
)

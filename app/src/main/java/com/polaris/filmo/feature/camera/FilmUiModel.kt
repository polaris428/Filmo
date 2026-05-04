package com.polaris.filmo.feature.camera

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.polaris.filmo.R
import com.polaris.filmo.core.design.FilmoColors
import com.polaris.filmo.domain.model.FilmType

data class FilmUiModel(
    val type: FilmType,
    val id: String,
    val displayName: String,
    val description: String,
    @param:DrawableRes val thumbnailRes: Int,
    val accentColor: Color
)

fun FilmType.toUiModel(context: Context): FilmUiModel {
    val resources = context.resources
    val packageName = context.packageName
    val nameRes = resources.getIdentifier("film_name_$id", "string", packageName)
    val descriptionRes = resources.getIdentifier("film_desc_$id", "string", packageName)
    val thumbnailRes = resources.getIdentifier(thumbnailResName, "drawable", packageName)

    return FilmUiModel(
        type = this,
        id = id,
        displayName = if (nameRes != 0) resources.getString(nameRes) else displayName,
        description = if (descriptionRes != 0) resources.getString(descriptionRes) else description,
        thumbnailRes = if (thumbnailRes != 0) thumbnailRes else R.drawable.film_preview_placeholder,
        accentColor = accentColor()
    )
}

fun allFilmUiModels(context: Context): List<FilmUiModel> =
    FilmType.entries.map { it.toUiModel(context) }

private fun FilmType.accentColor(): Color =
    when (this) {
        FilmType.Portra400 -> FilmoColors.FilmPortra
        FilmType.Velvia50 -> FilmoColors.FilmVelvia
        FilmType.IlfordHp5 -> FilmoColors.FilmIlford
        FilmType.Lomography -> FilmoColors.FilmLomography
        FilmType.ExpiredFilm -> FilmoColors.FilmExpired
        FilmType.AnalogScan -> FilmoColors.FilmAnalogScan
        FilmType.DateStamp -> FilmoColors.FilmDateStamp
    }

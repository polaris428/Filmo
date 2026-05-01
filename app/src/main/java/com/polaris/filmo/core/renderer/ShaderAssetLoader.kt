package com.polaris.filmo.core.renderer

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShaderAssetLoader @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun loadShader(relativePath: String): String =
        context.assets.open("shader/$relativePath").bufferedReader().use { it.readText() }

    fun loadFilmFragmentShader(relativePath: String): String =
        loadShader(relativePath).withCommonFilmLibrary(loadShader(COMMON_FILM_SHADER))

    private fun String.withCommonFilmLibrary(commonSource: String): String {
        val lines = lineSequence().toList()
        val precisionIndex = lines.indexOfLast { it.trimStart().startsWith("precision ") }
        val extensionIndex = lines.indexOfLast { it.trimStart().startsWith("#extension ") }
        val insertIndex = when {
            precisionIndex >= 0 -> precisionIndex + 1
            extensionIndex >= 0 -> extensionIndex + 1
            else -> 0
        }

        return buildString {
            lines.take(insertIndex).forEach { appendLine(it) }
            appendLine()
            appendLine(commonSource)
            appendLine()
            lines.drop(insertIndex).forEach { appendLine(it) }
        }
    }

    private companion object {
        const val COMMON_FILM_SHADER = "common_film.glsl"
    }
}

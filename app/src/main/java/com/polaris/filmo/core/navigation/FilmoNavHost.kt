package com.polaris.filmo.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.polaris.filmo.core.design.FilmoColors
import com.polaris.filmo.feature.camera.CameraIntent
import com.polaris.filmo.feature.camera.CameraScreen
import com.polaris.filmo.feature.camera.CameraViewModel
import com.polaris.filmo.feature.filmselection.FilmSelectionScreen
import com.polaris.filmo.feature.gallery.GalleryScreen
import com.polaris.filmo.feature.gallery.detail.PhotoDetailScreen

private const val FILM_SELECTION_RESULT_KEY = "filmSelectionResult"

object FilmoRoutes {
    const val Camera = "camera"
    const val Gallery = "gallery"
    const val FilmSelection = "filmSelection/{selectedFilmId}"
    const val PhotoDetail = "photoDetail/{photoId}"

    fun filmSelection(selectedFilmId: String) = "filmSelection/${Uri.encode(selectedFilmId)}"
    fun photoDetail(photoId: String) = "photoDetail/${Uri.encode(photoId)}"
}

@Composable
fun FilmoNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = FilmoRoutes.Camera,
        modifier = Modifier
            .fillMaxSize()
            .background(FilmoColors.Background)
    ) {
        composable(FilmoRoutes.Camera) { backStackEntry ->
            val cameraViewModel: CameraViewModel = hiltViewModel()
            val selectedFilmResult by backStackEntry.savedStateHandle
                .getStateFlow<String?>(FILM_SELECTION_RESULT_KEY, null)
                .collectAsState()

            LaunchedEffect(selectedFilmResult) {
                val filmId = selectedFilmResult ?: return@LaunchedEffect
                cameraViewModel.handleIntent(CameraIntent.OnSelectFilm(filmId))
                backStackEntry.savedStateHandle.remove<String>(FILM_SELECTION_RESULT_KEY)
            }

            CameraScreen(
                onOpenGallery = {
                    navController.navigate(FilmoRoutes.Gallery)
                },
                onOpenFilmSelection = { selectedFilmId ->
                    navController.navigate(FilmoRoutes.filmSelection(selectedFilmId))
                },
                viewModel = cameraViewModel
            )
        }
        composable(
            route = FilmoRoutes.FilmSelection,
            arguments = listOf(navArgument("selectedFilmId") { type = NavType.StringType })
        ) {
            FilmSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSelectFilm = { filmId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(FILM_SELECTION_RESULT_KEY, filmId)
                    navController.popBackStack()
                }
            )
        }
        composable(FilmoRoutes.Gallery) {
            GalleryScreen(
                onNavigateToPhotoDetail = { photoId ->
                    navController.navigate(FilmoRoutes.photoDetail(photoId))
                },
                onNavigateToCamera = {
                    navController.popBackStack(FilmoRoutes.Camera, inclusive = false)
                }
            )
        }
        composable(
            route = FilmoRoutes.PhotoDetail,
            arguments = listOf(navArgument("photoId") { type = NavType.StringType })
        ) {
            PhotoDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

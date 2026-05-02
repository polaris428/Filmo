package com.polaris.filmo.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.polaris.filmo.core.design.FilmoColors
import com.polaris.filmo.feature.camera.CameraScreen
import com.polaris.filmo.feature.gallery.GalleryScreen
import com.polaris.filmo.feature.gallery.detail.PhotoDetailScreen

object FilmoRoutes {
    const val Camera = "camera"
    const val Gallery = "gallery"
    const val PhotoDetail = "photoDetail/{photoId}"

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
        composable(FilmoRoutes.Camera) {
            CameraScreen(
                onOpenGallery = {
                    navController.navigate(FilmoRoutes.Gallery)
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

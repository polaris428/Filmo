package com.polaris.filmo.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.polaris.filmo.R
import com.polaris.filmo.core.design.FilmoColors
import com.polaris.filmo.core.design.FilmoSpacing
import com.polaris.filmo.core.design.FilmoTextStyle
import com.polaris.filmo.feature.camera.CameraScreen

object FilmoRoutes {
    const val Camera = "camera"
    const val Gallery = "gallery"
    const val PhotoDetail = "photoDetail/{photoId}"

    fun photoDetail(photoId: String) = "photoDetail/$photoId"
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
            CameraScreen()
        }
        composable(FilmoRoutes.Gallery) {
            PlaceholderScreen(title = stringResource(R.string.gallery_title))
        }
        composable(
            route = FilmoRoutes.PhotoDetail,
            arguments = listOf(navArgument("photoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId").orEmpty()
            PlaceholderScreen(
                title = stringResource(R.string.route_photo_detail),
                caption = photoId
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    caption: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(FilmoSpacing.Space24),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FilmoSpacing.Space8),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = FilmoColors.TextPrimary,
                style = FilmoTextStyle.Title
            )
            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    color = FilmoColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

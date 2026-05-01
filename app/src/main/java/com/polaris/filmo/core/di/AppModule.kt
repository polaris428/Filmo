package com.polaris.filmo.core.di

import com.polaris.filmo.core.camera.CameraController
import com.polaris.filmo.core.camera.CameraXController
import com.polaris.filmo.core.media.MediaStorePhotoDataSource
import com.polaris.filmo.data.datasource.PhotoLocalDataSource
import com.polaris.filmo.data.repository.PhotoRepositoryImpl
import com.polaris.filmo.domain.repository.PhotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindCameraController(
        cameraXController: CameraXController
    ): CameraController

    @Binds
    @Singleton
    abstract fun bindPhotoLocalDataSource(
        mediaStorePhotoDataSource: MediaStorePhotoDataSource
    ): PhotoLocalDataSource

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        photoRepositoryImpl: PhotoRepositoryImpl
    ): PhotoRepository
}

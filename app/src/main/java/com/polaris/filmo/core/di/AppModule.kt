package com.polaris.filmo.core.di

import com.polaris.filmo.core.camera.CameraController
import com.polaris.filmo.core.camera.CameraXController
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
}

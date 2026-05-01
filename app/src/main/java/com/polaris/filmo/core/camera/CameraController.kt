package com.polaris.filmo.core.camera

import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner

interface CameraController {
    suspend fun startPreview(
        surfaceProvider: Preview.SurfaceProvider,
        lifecycleOwner: LifecycleOwner
    ): Result<Unit>

    suspend fun switchCamera(targetLensFacing: LensFacing): Result<Unit>

    suspend fun focusAt(x: Float, y: Float): Result<Unit>

    fun stopPreview()
}

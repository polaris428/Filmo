package com.polaris.filmo.core.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class CameraXController @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CameraController {
    private var cameraProvider: ProcessCameraProvider? = null

    override suspend fun startPreview(
        surfaceProvider: Preview.SurfaceProvider,
        lifecycleOwner: LifecycleOwner
    ): Result<Unit> = runCatching {
        val provider = getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(surfaceProvider)
        }

        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview
        )
    }.map { Unit }

    override fun stopPreview() {
        cameraProvider?.unbindAll()
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider {
        cameraProvider?.let { return it }

        val future = ProcessCameraProvider.getInstance(context)
        return suspendCancellableCoroutine { continuation ->
            future.addListener(
                {
                    val provider = future.get()
                    cameraProvider = provider
                    continuation.resume(provider)
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    }
}

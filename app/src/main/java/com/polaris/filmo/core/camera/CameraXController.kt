package com.polaris.filmo.core.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class CameraXController @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CameraController {
    private var cameraProvider: ProcessCameraProvider? = null
    private var surfaceProvider: Preview.SurfaceProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var activeLensFacing: LensFacing = LensFacing.Back
    private var camera: Camera? = null

    override suspend fun startPreview(
        surfaceProvider: Preview.SurfaceProvider,
        lifecycleOwner: LifecycleOwner
    ): Result<Unit> = runCatching {
        this.surfaceProvider = surfaceProvider
        this.lifecycleOwner = lifecycleOwner
        bindPreview(activeLensFacing)
    }

    override suspend fun switchCamera(targetLensFacing: LensFacing): Result<Unit> = runCatching {
        if (activeLensFacing == targetLensFacing) return@runCatching
        bindPreview(targetLensFacing)
        activeLensFacing = targetLensFacing
    }

    override suspend fun focusAt(x: Float, y: Float): Result<Unit> = runCatching {
        val camera = requireNotNull(camera) { "Camera is not ready." }
        val pointFactory = SurfaceOrientedMeteringPointFactory(
            NORMALIZED_METERING_SIZE,
            NORMALIZED_METERING_SIZE
        )
        val meteringPoint = pointFactory.createPoint(
            x.coerceIn(0f, NORMALIZED_METERING_SIZE),
            y.coerceIn(0f, NORMALIZED_METERING_SIZE)
        )
        val action = FocusMeteringAction.Builder(
            meteringPoint,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
        )
            .setAutoCancelDuration(FOCUS_AUTO_CANCEL_SECONDS, TimeUnit.SECONDS)
            .build()

        camera.cameraControl.startFocusAndMetering(action).await()
    }

    override fun stopPreview() {
        cameraProvider?.unbindAll()
        camera = null
    }

    private suspend fun bindPreview(lensFacing: LensFacing) {
        val surfaceProvider = requireNotNull(surfaceProvider) {
            "Preview surface provider is not ready."
        }
        val lifecycleOwner = requireNotNull(lifecycleOwner) {
            "Lifecycle owner is not ready."
        }
        val provider = getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(surfaceProvider)
        }
        val selector = lensFacing.toCameraSelector()
        check(provider.hasCamera(selector)) {
            "Requested camera is not available."
        }

        provider.unbindAll()
        camera = provider.bindToLifecycle(
            lifecycleOwner,
            selector,
            preview
        )
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

    private fun LensFacing.toCameraSelector(): CameraSelector =
        when (this) {
            LensFacing.Back -> CameraSelector.DEFAULT_BACK_CAMERA
            LensFacing.Front -> CameraSelector.DEFAULT_FRONT_CAMERA
        }

    private suspend fun <T> com.google.common.util.concurrent.ListenableFuture<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addListener(
                {
                    try {
                        continuation.resume(get())
                    } catch (throwable: Throwable) {
                        continuation.resumeWithException(throwable)
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
            continuation.invokeOnCancellation {
                cancel(true)
            }
        }

    private companion object {
        const val NORMALIZED_METERING_SIZE = 1f
        const val FOCUS_AUTO_CANCEL_SECONDS = 3L
    }
}

package com.polaris.filmo.core.renderer

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Size
import android.view.Surface
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.core.content.ContextCompat
import com.polaris.filmo.domain.model.FilmType
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Singleton
class FilmRenderer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val shaderAssetLoader: ShaderAssetLoader
) : GLSurfaceView.Renderer,
    Preview.SurfaceProvider,
    FilmRendererController,
    RenderFrameCapturer {

    private val vertexBuffer = floatBufferOf(
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f,
        -1f, 1f, 0f, 1f,
        1f, 1f, 1f, 1f
    )
    private val textureTransform = FloatArray(16).also {
        Matrix.setIdentityM(it, 0)
    }

    @Volatile
    private var requestedFilm: FilmType = FilmType.Portra400

    private var appliedFilm: FilmType? = null
    private var textureSurfaceSource: TextureSurfaceSource? = null
    private var shaderProgram: ShaderProgram? = null
    private var activeShaderConfig: FilmShaderConfig = FilmShaderConfig.forFilm(requestedFilm)
    private var pendingSurfaceRequest: SurfaceRequest? = null
    private var lastSurfaceSize: Size? = null
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var positionScaleX: Float = 1f
    private var positionScaleY: Float = 1f
    private var startTimeNanos: Long = 0L

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        startTimeNanos = System.nanoTime()
        textureSurfaceSource = TextureSurfaceSource.create()
        pendingSurfaceRequest?.let(::provideCameraSurface)
        pendingSurfaceRequest = null
        ensureShaderProgram()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        updateTextureCropScale()
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        val source = textureSurfaceSource ?: return
        val program = ensureShaderProgram() ?: return

        runCatching {
            source.surfaceTexture.updateTexImage()
            source.surfaceTexture.getTransformMatrix(textureTransform)
        }

        program.use()
        program.setMat4("uTextureTransform", textureTransform)
        program.setVec2("uPositionScale", positionScaleX, positionScaleY)
        program.setInt("uCameraTexture", 0)
        program.setFloat("uTime", elapsedSeconds())
        program.setFilmConfig(activeShaderConfig)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, source.textureId)

        val positionLocation = program.getAttributeLocation("aPosition")
        val texCoordLocation = program.getAttributeLocation("aTexCoord")
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glVertexAttribPointer(
            positionLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            VERTEX_STRIDE_BYTES,
            vertexBuffer
        )
        vertexBuffer.position(2)
        GLES20.glEnableVertexAttribArray(texCoordLocation)
        GLES20.glVertexAttribPointer(
            texCoordLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            VERTEX_STRIDE_BYTES,
            vertexBuffer
        )

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT)
        GLES20.glDisableVertexAttribArray(positionLocation)
        GLES20.glDisableVertexAttribArray(texCoordLocation)
    }

    override fun onSurfaceRequested(request: SurfaceRequest) {
        val source = textureSurfaceSource
        if (source == null) {
            pendingSurfaceRequest = request
            return
        }
        provideCameraSurface(request)
    }

    override fun setFilm(filmType: FilmType): Result<Unit> = runCatching {
        requestedFilm = filmType
    }

    override fun getInputSurface(): Surface? =
        textureSurfaceSource?.surface

    override fun captureCurrentFrame(callback: (Result<CapturedFrame>) -> Unit) {
        callback(Result.failure(NotImplementedError("FBO capture is reserved for the capture plan.")))
    }

    fun release() {
        shaderProgram?.release()
        shaderProgram = null
        textureSurfaceSource?.release()
        textureSurfaceSource = null
        pendingSurfaceRequest = null
        appliedFilm = null
    }

    private fun provideCameraSurface(request: SurfaceRequest) {
        val source = textureSurfaceSource ?: run {
            pendingSurfaceRequest = request
            return
        }
        val resolution = request.resolution
        if (lastSurfaceSize != resolution) {
            source.surfaceTexture.setDefaultBufferSize(resolution.width, resolution.height)
            lastSurfaceSize = resolution
            updateTextureCropScale()
        }
        request.provideSurface(
            source.surface,
            ContextCompat.getMainExecutor(context)
        ) {
            // Surface ownership stays with the renderer until the GL surface is released.
        }
    }

    private fun ensureShaderProgram(): ShaderProgram? {
        if (shaderProgram != null && appliedFilm == requestedFilm) {
            return shaderProgram
        }

        return runCatching {
            val config = FilmShaderConfig.forFilm(requestedFilm)
            val nextProgram = ShaderProgram.compile(
                vertexSource = shaderAssetLoader.loadShader("camera.vert"),
                fragmentSource = shaderAssetLoader.loadFilmFragmentShader(config.fragmentShaderAsset)
            )
            shaderProgram?.release()
            shaderProgram = nextProgram
            appliedFilm = requestedFilm
            activeShaderConfig = config
            nextProgram
        }.getOrElse {
            shaderProgram
        }
    }

    private fun elapsedSeconds(): Float {
        if (startTimeNanos == 0L) return 0f
        return (System.nanoTime() - startTimeNanos) / NANOS_PER_SECOND
    }

    private fun updateTextureCropScale() {
        val surfaceSize = lastSurfaceSize ?: return
        if (viewWidth <= 0 || viewHeight <= 0) return

        val viewAspect = viewWidth.toFloat() / viewHeight.toFloat()
        val sourceAspect = effectiveSourceAspect(surfaceSize)
        if (sourceAspect > viewAspect) {
            positionScaleX = 1f
            positionScaleY = viewAspect / sourceAspect
        } else {
            positionScaleX = sourceAspect / viewAspect
            positionScaleY = 1f
        }
    }

    private fun effectiveSourceAspect(size: Size): Float {
        val sourceAspect = size.width.toFloat() / size.height.toFloat()
        val viewAspect = viewWidth.toFloat() / viewHeight.toFloat()
        return if ((viewAspect < 1f && sourceAspect > 1f) ||
            (viewAspect > 1f && sourceAspect < 1f)
        ) {
            size.height.toFloat() / size.width.toFloat()
        } else {
            sourceAspect
        }
    }

    private companion object {
        const val VERTEX_COUNT = 4
        const val FLOAT_SIZE_BYTES = 4
        const val COMPONENTS_PER_VERTEX = 4
        const val VERTEX_STRIDE_BYTES = COMPONENTS_PER_VERTEX * FLOAT_SIZE_BYTES
        const val NANOS_PER_SECOND = 1_000_000_000f

        fun floatBufferOf(vararg values: Float): FloatBuffer =
            ByteBuffer
                .allocateDirect(values.size * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .apply {
                    put(values)
                    position(0)
                }
    }
}

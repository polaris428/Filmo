# 03. OpenGL 렌더링 파이프라인 만들기

## 목표

CameraX에서 들어오는 카메라 프레임을 OpenGL ES 2.0 텍스처로 렌더링하고, 선택된 필름 Fragment Shader를 실시간으로 적용할 수 있는 기반을 만든다.

## 구현 범위

- `GLSurfaceView` 기반 렌더링 화면
- 외부 카메라 텍스처 연결
- vertex shader와 공통 fragment shader 구조
- shader compile/link 관리
- 필름 셰이더 교체
- 렌더링 결과 캡처를 위한 FBO 기반 확장 지점

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/core/renderer/CameraPreviewView.kt` | Compose `AndroidView`로 `GLSurfaceView` 연결 |
| `app/core/renderer/FilmGLSurfaceView.kt` | OpenGL 전용 SurfaceView 래퍼 작성 |
| `app/core/renderer/FilmRenderer.kt` | `GLSurfaceView.Renderer` 구현 |
| `app/core/renderer/ShaderProgram.kt` | vertex/fragment shader 컴파일, 링크, uniform/attribute 조회 |
| `app/core/renderer/ShaderAssetLoader.kt` | asset의 `.frag`, `.vert` 파일 로드 |
| `app/core/renderer/TextureSurfaceSource.kt` | `SurfaceTexture`와 OES texture 생성 |
| `app/core/renderer/RenderFrameCapturer.kt` | 렌더링 결과 캡처용 FBO/readPixels 확장 지점 |
| `app/src/main/assets/shader/camera.vert` | 공통 vertex shader |
| `app/src/main/assets/shader/base_camera.frag` | 필름 없는 기본 fragment shader |

## 상세 개발 절차

1. `CameraPreviewView`를 만든다.
   - Compose에서는 `AndroidView`로 `FilmGLSurfaceView`를 생성한다.
   - `selectedFilmType` 변경 시 renderer의 `setFilm()`을 호출할 수 있게 한다.
2. `FilmGLSurfaceView`를 만든다.
   - `setEGLContextClientVersion(2)`를 호출한다.
   - `preserveEGLContextOnPause = true`로 설정한다.
   - render mode는 실시간 프리뷰를 위해 `RENDERMODE_CONTINUOUSLY`를 기본으로 둔다.
3. `TextureSurfaceSource`에서 OES texture id를 생성한다.
   - `GLES11Ext.GL_TEXTURE_EXTERNAL_OES`를 사용한다.
   - texture wrap은 `CLAMP_TO_EDGE`로 설정한다.
   - min/mag filter는 `LINEAR`로 설정한다.
4. `SurfaceTexture(textureId)`를 생성하고 CameraX가 사용할 `Surface`로 제공할 수 있게 한다.
5. `FilmRenderer.onSurfaceCreated()`에서 다음을 초기화한다.
   - OES texture
   - `SurfaceTexture`
   - 공통 vertex buffer
   - 기본 shader program
6. `FilmRenderer.onDrawFrame()`에서 다음 순서로 렌더링한다.
   - `surfaceTexture.updateTexImage()`
   - transform matrix uniform 갱신
   - 현재 선택된 shader program bind
   - OES texture bind
   - full-screen quad draw
7. `ShaderProgram`은 다음 API를 제공한다.
   - `compile(vertexSource, fragmentSource)`
   - `use()`
   - `setInt(name, value)`
   - `setFloat(name, value)`
   - `setMat4(name, value)`
   - `release()`
8. 필름 변경 시 `FilmRenderer.setFilm(filmType)`는 다음을 수행한다.
   - 기존 program과 같은 필름이면 아무 작업도 하지 않는다.
   - 해당 필름의 fragment shader asset을 로드한다.
   - 새 `ShaderProgram`을 compile/link한다.
   - 성공하면 다음 frame부터 새 program을 사용한다.
   - 실패하면 기존 program을 유지하고 실패를 반환한다.
9. 렌더링 결과 캡처를 위해 `RenderFrameCapturer` 인터페이스를 먼저 둔다.
   - 실제 FBO 캡처는 촬영 플랜과 연결한다.
   - `readPixels()`는 GL thread에서 실행해야 한다는 제약을 문서화한다.

## OpenGL 계약

```kotlin
interface FilmRendererController {
    fun setFilm(filmType: FilmType): Result<Unit>
    fun getInputSurface(): Surface
    fun captureCurrentFrame(callback: (Result<CapturedFrame>) -> Unit)
}
```

## 공통 Vertex Shader

```glsl
attribute vec4 aPosition;
attribute vec2 aTexCoord;

uniform mat4 uTextureTransform;

varying vec2 vTexCoord;

void main() {
    gl_Position = aPosition;
    vec4 transformed = uTextureTransform * vec4(aTexCoord, 0.0, 1.0);
    vTexCoord = transformed.xy;
}
```

## 기본 Fragment Shader

```glsl
#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES uCameraTexture;
varying vec2 vTexCoord;

void main() {
    gl_FragColor = texture2D(uCameraTexture, vTexCoord);
}
```

## 검증 기준

- 카메라 프레임이 `GLSurfaceView`에 실시간으로 표시된다.
- 화면 회전 또는 앱 pause/resume 후 GL context가 복구된다.
- 필름 변경 시 shader program이 교체된다.
- shader compile 실패 시 기존 필름 렌더링이 유지된다.
- 프리뷰 렌더링이 빈 화면이나 검은 화면으로 고정되지 않는다.

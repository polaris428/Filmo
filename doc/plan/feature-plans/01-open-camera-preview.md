# 01. 앱 진입 후 카메라 보기

## 목표

사용자가 앱을 실행하면 카메라 화면에 진입하고, 기본 필름 효과가 적용된 실시간 프리뷰를 볼 수 있게 한다.

## 구현 범위

- 카메라 화면 MVI 기본 구조
- 프리뷰 상태 모델
- CameraX 프리뷰 시작 요청
- OpenGL 렌더러 표시 영역 연결
- 기본 필름 적용 상태

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/feature/camera/CameraScreen.kt` | 카메라 화면 Compose 작성 |
| `app/feature/camera/CameraViewModel.kt` | 화면 진입 이벤트 처리 |
| `app/feature/camera/CameraState.kt` | 프리뷰, 필름, 로딩, 오류 상태 정의 |
| `app/feature/camera/CameraIntent.kt` | `OnEnterScreen`, `OnRetryPreview` 정의 |
| `app/feature/camera/CameraEffect.kt` | 오류 토스트 등 일회성 이벤트 정의 |
| `app/core/camera/CameraController.kt` | 프리뷰 시작/중지 인터페이스 정의 |
| `app/core/renderer/FilmRenderer.kt` | 기본 필름 렌더링 진입점 정의 |
| `app/core/renderer/CameraPreviewView.kt` | Compose에서 렌더러를 붙이는 AndroidView 래퍼 작성 |

## 상세 개발 절차

1. `CameraState`를 먼저 만든다.
   - `previewState: PreviewState`
   - `selectedFilm: FilmType`
   - `isInitializing: Boolean`
   - `errorMessage: String?`
2. `PreviewState` sealed interface를 정의한다.
   - `Idle`
   - `Loading`
   - `Ready`
   - `PermissionRequired`
   - `Error(message: String)`
3. 기본 필름은 `FilmType.Portra400`으로 고정한다.
4. `CameraIntent.OnEnterScreen`이 들어오면 ViewModel은 상태를 `Loading`으로 바꾼다.
5. ViewModel은 권한 확인이 필요한 경우 `PermissionRequired`로 상태를 바꾼다.
6. 권한이 이미 있으면 `CameraController.startPreview()`를 호출한다.
7. 프리뷰 시작 성공 시 `PreviewState.Ready`로 바꾼다.
8. 실패 시 `PreviewState.Error(message)`로 바꾸고 `OnRetryPreview`에서 같은 절차를 다시 실행한다.
9. `CameraScreen`은 `PreviewState`에 따라 다음을 그린다.
   - `Loading`: 전체 화면 배경 + 중앙 `LoadingIndicator`
   - `Ready`: `CameraPreviewView`
   - `PermissionRequired`: 권한 안내는 02 플랜에서 연결
   - `Error`: `ErrorState` 또는 `OverlayPanel`
10. `CameraPreviewView`는 화면 전체를 채우며, 내부 구현은 `AndroidView`로 `GLSurfaceView` 또는 프리뷰 브리지를 붙인다.

## 상태/이벤트 계약

```kotlin
sealed interface CameraIntent {
    data object OnEnterScreen : CameraIntent
    data object OnRetryPreview : CameraIntent
}

sealed interface PreviewState {
    data object Idle : PreviewState
    data object Loading : PreviewState
    data object Ready : PreviewState
    data object PermissionRequired : PreviewState
    data class Error(val message: String) : PreviewState
}
```

## 디자인 적용

- `CameraPreviewView`는 전체 화면을 채운다.
- 로딩은 `LoadingIndicator(size = medium)`을 중앙에 둔다.
- 오류는 `OverlayPanel`을 중앙에 배치하고 주요 액션은 `PrimaryButton("다시 시도")`로 둔다.
- 상단/하단 컨트롤은 이 플랜에서 빈 자리만 확보하고 실제 동작은 후속 플랜에서 연결한다.

## 검증 기준

- 권한이 있는 상태에서 앱 진입 시 `PreviewState.Ready`가 된다.
- 프리뷰 시작 전에는 `Loading`이 표시된다.
- 프리뷰 시작 실패 시 오류 패널과 재시도 버튼이 표시된다.
- 재시도 버튼을 누르면 `CameraController.startPreview()`가 다시 호출된다.
- 기본 필름 상태가 `Portra400`으로 설정된다.

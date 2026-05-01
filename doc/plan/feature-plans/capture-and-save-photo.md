# 셔터로 촬영하고 저장하기

## 목표

사용자가 셔터를 누르면 현재 프리뷰에 보이는 필름 효과가 적용된 이미지를 캡처하고, 기기 갤러리에 저장한 뒤 앱 내 갤러리에서 다시 조회할 수 있는 사진 정보로 남긴다.

## 구현 범위

- 셔터 버튼 상태
- 렌더링 결과 캡처
- 촬영/저장 중 중복 입력 방지
- 셔터음, 진동, 플래시 피드백
- 사진 저장 유스케이스
- MediaStore 저장 데이터소스
- 저장 상태 UI
- 저장 성공 후 앱 내 갤러리 목록 반영을 위한 `CapturedPhoto` 반환
- 저장 실패 재시도

렌더링 결과 캡처는 [OpenGL 렌더링 파이프라인 만들기](./build-opengl-rendering-pipeline.md)의 `RenderFrameCapturer`와 같은 GL 렌더링 경로를 사용한다.

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/src/main/java/com/polaris/filmo/domain/model/CapturedPhoto.kt` | 저장된 사진 모델 정의 |
| `app/src/main/java/com/polaris/filmo/domain/repository/PhotoRepository.kt` | 저장 인터페이스 정의. 조회/삭제는 갤러리 후속 플랜에서 확장 |
| `app/src/main/java/com/polaris/filmo/domain/usecase/SavePhotoUseCase.kt` | 저장 유스케이스 작성 |
| `app/src/main/java/com/polaris/filmo/data/repository/PhotoRepositoryImpl.kt` | repository 구현 |
| `app/src/main/java/com/polaris/filmo/data/datasource/PhotoLocalDataSource.kt` | 로컬 저장 추상화 |
| `app/src/main/java/com/polaris/filmo/core/media/MediaStorePhotoDataSource.kt` | MediaStore 저장 구현 |
| `app/src/main/java/com/polaris/filmo/core/renderer/FilmRenderer.kt` | 현재 GL 렌더링 결과를 `glReadPixels`로 캡처 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraState.kt` | `isCapturing`, `captureFeedbackState`, `saveStatus`, `lastSavedPhoto` 추가 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraIntent.kt` | `OnClickShutter`, `OnRetrySave` 추가 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraEffect.kt` | 셔터음/진동/오류 메시지 Effect 추가 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraScreen.kt` | `ShutterButton`, `CaptureFlash`, `StatusToast` 연결 |

## 상세 개발 절차

1. `CapturedPhoto` 모델을 정의한다.
   - `id`
   - `uri`
   - `filmType`
   - `createdAt`
   - `width`
   - `height`
2. `PhotoRepository.savePhoto(frame, filmType)` 인터페이스를 정의한다.
3. `SavePhotoUseCase`는 repository에 저장을 위임하고 `CapturedPhoto`를 반환한다.
4. `MediaStorePhotoDataSource`는 다음 규칙으로 파일을 저장한다.
   - display name: `Filmo_yyyyMMdd_HHmmss`
   - mime type: `image/jpeg`
   - relative path: `Pictures/Filmo`
5. `CameraState.isCapturing` 기본값은 false다.
6. 셔터 클릭 시 `OnClickShutter`를 보낸다.
7. ViewModel은 다음 조건이면 요청을 무시한다.
   - `isCapturing = true`
   - `previewState != Ready`
   - 권한이 없음
8. 촬영 가능하면 `isCapturing = true`, `saveStatus = Saving`으로 바꾼다.
9. `CameraEffect.PlayShutterFeedback`을 발생시킨다.
10. UI는 셔터음, 진동, `CaptureFlash`를 실행한다.
11. ViewModel은 `RenderFrameCapturer.captureCurrentFrame()`을 호출한다.
12. 캡처 성공 시 같은 coroutine 흐름에서 `SavePhotoUseCase(frame, selectedFilm)`를 호출한다.
13. 저장 성공 시 `saveStatus = Success`, `lastSavedPhoto = CapturedPhoto`, `isCapturing = false`로 갱신한다.
14. 저장 실패 시 `saveStatus = Failure(reason, canRetry = true)`, `isCapturing = false`로 되돌린다.
15. 실패 재시도는 마지막 캡처 결과가 남아 있는 경우에만 실행한다.
16. 저장 성공 후 2초 뒤 `saveStatus`를 null로 초기화한다.
17. 저장 성공한 `CapturedPhoto`는 `lastSavedPhoto`에 남기고, 갤러리 조회 결과 반영은 `view-gallery-list.md`에서 MediaStore 목록 조회를 구현하며 연결한다.

## 상태/이벤트 계약

```kotlin
sealed interface SaveStatus {
    data object Saving : SaveStatus
    data class Success(val photo: CapturedPhoto) : SaveStatus
    data class Failure(val reason: String, val canRetry: Boolean) : SaveStatus
}

sealed interface CameraEffect {
    data object PlayShutterFeedback : CameraEffect
    data class ShowMessage(val message: String) : CameraEffect
}
```

## 디자인 적용

- 셔터는 `ShutterButton`을 사용한다.
- 촬영/저장 중에는 `capturing` 상태로 표시하고 재클릭을 막는다.
- 촬영 순간 `CaptureFlash`를 전체 화면에 120ms 표시한다.
- 저장 중/성공/실패는 `StatusToast`로 표시한다.
- 저장 성공 시 성공 상태 토스트를 표시한다. `DevelopThumbnail(done)` 애니메이션은 갤러리 목록/상세 흐름이 생긴 뒤 확장한다.
- 저장 실패 시 `StatusToast(error)`와 재시도 액션을 표시한다.

## 구현 상태

- `CapturedPhoto`, `PhotoRepository`, `SavePhotoUseCase`, `PhotoLocalDataSource`, `PhotoRepositoryImpl`, `MediaStorePhotoDataSource`를 추가했다.
- `FilmRenderer.captureCurrentFrame()`은 현재 GL surface 크기의 RGBA 픽셀을 `glReadPixels`로 캡처한다.
- `MediaStorePhotoDataSource`는 캡처 프레임을 세로 방향으로 보정해 JPEG로 인코딩하고 `Pictures/Filmo`에 저장한다.
- `CameraState`는 `isCapturing`, `captureFeedbackState`, `saveStatus`, `lastSavedPhoto`를 관리한다.
- `CameraScreen`은 셔터 버튼, 촬영 플래시, 저장 상태 토스트, 실패 재시도 액션을 표시한다.
- 앱 내 갤러리 목록에서 저장 사진을 조회하는 기능은 아직 없으며 [view-gallery-list.md](./view-gallery-list.md)에서 진행한다.

## 검증 기준

- 셔터 클릭 시 캡처와 저장 흐름이 한 번만 실행된다.
- 촬영/저장 중 연속 클릭해도 중복 캡처 또는 중복 저장이 발생하지 않는다.
- 캡처 실패 시 셔터 버튼이 다시 활성화된다.
- 저장 실패 시 앱이 멈추지 않고 재시도할 수 있다.
- 저장 성공 시 파일이 `Pictures/Filmo`에 생성된다.
- 저장된 파일은 기기 갤러리에서 확인 가능하다.
- 저장 성공 후 `lastSavedPhoto`가 갱신된다. 앱 갤러리 목록 조회는 후속 플랜에서 검증한다.
- 저장된 결과에는 현재 선택된 필름 효과가 포함된다.
- 촬영 피드백이 클릭 직후 표시된다.

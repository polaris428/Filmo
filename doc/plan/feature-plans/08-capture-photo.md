# 08. 셔터로 촬영하기

## 목표

사용자가 셔터를 누르면 현재 프리뷰에 보이는 필름 효과가 적용된 이미지를 캡처하고 촬영 피드백을 제공한다.

## 구현 범위

- 셔터 버튼 상태
- 렌더링 결과 캡처
- 촬영 중 중복 입력 방지
- 셔터 피드백
- 캡처 결과를 저장 플랜으로 전달

렌더링 결과 캡처는 [03. OpenGL 렌더링 파이프라인 만들기](./03-build-opengl-rendering-pipeline.md)의 `RenderFrameCapturer`와 같은 GL 렌더링 경로를 사용한다.

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/core/media/RenderedFrameCapture.kt` | 현재 렌더링 결과 캡처 인터페이스 작성 |
| `app/feature/camera/CameraState.kt` | `isCapturing`, `captureFeedbackState` 추가 |
| `app/feature/camera/CameraIntent.kt` | `OnClickShutter` 추가 |
| `app/feature/camera/CameraEffect.kt` | 셔터음/진동/오류 메시지 Effect 추가 |
| `app/feature/camera/CameraScreen.kt` | `ShutterButton`, `CaptureFlash`, `DevelopThumbnail` 연결 |

## 상세 개발 절차

1. `RenderedFrameCapture.captureCurrentFrame()` 인터페이스를 정의한다.
2. 반환 타입은 성공/실패를 구분한다.
   - 성공: 캡처된 bitmap 또는 byte array
   - 실패: 오류 원인
3. `CameraState.isCapturing` 기본값은 false다.
4. 셔터 클릭 시 `OnClickShutter`를 보낸다.
5. ViewModel은 다음 조건이면 요청을 무시한다.
   - `isCapturing = true`
   - `previewState != Ready`
   - 권한이 없음
6. 촬영 가능하면 `isCapturing = true`로 바꾼다.
7. `CameraEffect.PlayShutterFeedback`을 발생시킨다.
8. UI는 셔터음, 진동, `CaptureFlash`를 실행한다.
9. ViewModel은 `RenderedFrameCapture.captureCurrentFrame()`을 호출한다.
10. 캡처 성공 시 저장 플랜에서 사용할 임시 캡처 결과를 생성한다.
11. 캡처 실패 시 `isCapturing = false`로 되돌리고 오류 메시지를 표시한다.
12. 캡처 성공 후 저장이 시작되면 `isCapturing`은 저장 완료 또는 실패 시점까지 유지한다.

## 상태/이벤트 계약

```kotlin
sealed interface CaptureResult {
    data class Success(val image: CapturedFrame) : CaptureResult
    data class Failure(val reason: String) : CaptureResult
}

sealed interface CameraEffect {
    data object PlayShutterFeedback : CameraEffect
    data class ShowMessage(val message: String) : CameraEffect
}
```

## 디자인 적용

- 셔터는 `ShutterButton`을 사용한다.
- 촬영 중에는 `capturing` 상태로 표시하고 재클릭을 막는다.
- 촬영 순간 `CaptureFlash`를 전체 화면에 120ms 표시한다.
- 캡처 성공 후 `DevelopThumbnail`을 하단 좌측 갤러리 버튼 근처에 표시한다.

## 검증 기준

- 셔터 클릭 시 캡처 인터페이스가 한 번만 호출된다.
- 촬영 중 연속 클릭해도 중복 캡처가 발생하지 않는다.
- 캡처 실패 시 셔터 버튼이 다시 활성화된다.
- 캡처 성공 결과에는 현재 선택된 필름 효과가 포함된다.
- 촬영 피드백이 클릭 직후 표시된다.

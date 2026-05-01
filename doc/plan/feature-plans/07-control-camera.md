# 07. 카메라 조작하기

## 목표

사용자가 전면/후면 카메라를 전환하고, 프리뷰의 원하는 위치를 탭해 초점을 맞출 수 있게 한다.

## 구현 범위

- 렌즈 방향 상태
- 카메라 전환 버튼
- 탭 포커스 좌표 처리
- 포커스 링 피드백
- 전환 중 중복 입력 방지

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/core/camera/CameraController.kt` | `switchCamera()`, `focusAt()` 추가 |
| `app/feature/camera/CameraState.kt` | `lensFacing`, `isSwitchingCamera`, `focusPoint` 추가 |
| `app/feature/camera/CameraIntent.kt` | `OnSwitchCamera`, `OnTapPreview(x, y)` 추가 |
| `app/feature/camera/CameraScreen.kt` | `CircleIconButton`, `FocusRing` 연결 |
| `app/feature/camera/CameraEffect.kt` | 포커스 실패 메시지 추가 |

## 상세 개발 절차

1. `LensFacing` enum을 정의한다.
   - `Back`
   - `Front`
2. `CameraState.lensFacing` 기본값은 `Back`으로 둔다.
3. 카메라 전환 버튼 클릭 시 `OnSwitchCamera`를 보낸다.
4. `isSwitchingCamera = true`일 때는 추가 전환 요청을 무시한다.
5. ViewModel은 현재 `lensFacing`의 반대 값을 계산한다.
6. `CameraController.switchCamera(targetLensFacing)`을 호출한다.
7. 성공하면 `lensFacing`을 target으로 바꾸고 `isSwitchingCamera = false`로 만든다.
8. 실패하면 기존 lensFacing을 유지하고 오류 메시지를 발생시킨다.
9. 프리뷰 영역 탭 시 화면 좌표를 `OnTapPreview(x, y)`로 전달한다.
10. ViewModel은 `CameraController.focusAt(x, y)`를 호출한다.
11. focus 요청 직후 `focusPoint = Offset(x, y)`로 설정한다.
12. UI는 `FocusRing`을 표시하고 애니메이션 종료 후 `focusPoint`를 null로 되돌린다.

## 상태/이벤트 계약

```kotlin
enum class LensFacing { Back, Front }

sealed interface CameraIntent {
    data object OnSwitchCamera : CameraIntent
    data class OnTapPreview(val x: Float, val y: Float) : CameraIntent
}
```

## 디자인 적용

- 카메라 전환은 상단 우측 `CircleIconButton(Cameraswitch)`로 구현한다.
- 갤러리 진입 버튼과 같은 크기, 같은 배경을 사용한다.
- 포커스 링은 탭 위치 중심에 64dp 크기로 표시한다.
- 포커스 링은 프리뷰 영역에서만 표시하고 필름/셔터 영역 탭은 무시한다.

## 검증 기준

- 카메라 전환 버튼을 누르면 전면/후면이 전환된다.
- 전환 중 버튼을 여러 번 눌러도 세션이 깨지지 않는다.
- 카메라 전환 후 선택된 필름 상태가 유지된다.
- 프리뷰를 탭하면 해당 위치에 포커스 링이 표시된다.
- 포커스 실패 시 앱이 멈추지 않고 메시지를 표시한다.

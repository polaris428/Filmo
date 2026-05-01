# 02. 카메라 권한 처리

## 목표

사용자가 카메라 권한을 허용하면 프리뷰를 시작하고, 거부하면 다음 행동을 명확히 안내한다.

## 구현 범위

- 카메라 권한 상태 확인
- 권한 요청 Effect
- 권한 허용 후 프리뷰 시작
- 권한 거부/다시 묻지 않음 안내

## 파일 작업


| 경로 | 작업 |
| --- | --- |
| `app/feature/camera/CameraState.kt` | `CameraPermissionState` 추가 |
| `app/feature/camera/CameraIntent.kt` | 권한 결과 Intent 추가 |
| `app/feature/camera/CameraEffect.kt` | 권한 요청, 설정 이동 Effect 추가 |
| `app/feature/camera/CameraScreen.kt` | permission launcher와 안내 UI 연결 |
| `app/core/camera/CameraPermissionChecker.kt` | 권한 상태 확인 유틸 작성 |

## 상세 개발 절차

1. `CameraPermissionState`를 sealed interface로 정의한다.
   - `Unknown`
   - `Granted`
   - `Denied(canRequestAgain: Boolean)`
2. `OnEnterScreen` 처리 시 `CameraPermissionChecker.hasCameraPermission()`을 먼저 호출한다.
3. 권한이 있으면 01 플랜의 프리뷰 시작 절차로 이동한다.
4. 권한이 없고 요청 가능하면 `CameraEffect.RequestCameraPermission`을 발생시킨다.
5. `CameraScreen`에서 `rememberLauncherForActivityResult(RequestPermission())`를 등록한다.
6. `RequestCameraPermission` Effect를 수신하면 launcher를 실행한다.
7. 권한 결과는 `CameraIntent.OnPermissionResult(granted, canRequestAgain)`으로 ViewModel에 전달한다.
8. `granted = true`면 상태를 `Granted`로 바꾸고 프리뷰를 시작한다.
9. `granted = false`이고 `canRequestAgain = true`면 `PermissionPanel(requestable)`을 표시한다.
10. `granted = false`이고 `canRequestAgain = false`면 `PermissionPanel(deniedForever)`을 표시한다.
11. 설정 열기 버튼을 누르면 `CameraEffect.OpenAppSettings`를 발생시킨다.
12. 설정에서 돌아오면 `OnEnterScreen` 또는 `OnResume`으로 권한 상태를 다시 확인한다.

## 상태/이벤트 계약

```kotlin
sealed interface CameraIntent {
    data class OnPermissionResult(
        val granted: Boolean,
        val canRequestAgain: Boolean
    ) : CameraIntent
    data object OnClickPermissionPrimaryAction : CameraIntent
}

sealed interface CameraEffect {
    data object RequestCameraPermission : CameraEffect
    data object OpenAppSettings : CameraEffect
}
```

## 디자인 적용

- 권한 안내는 `OverlayPanel`을 사용한다.
- 요청 가능 상태의 주요 버튼 문구는 `권한 허용`이다.
- 다시 묻지 않음 상태의 주요 버튼 문구는 `설정 열기`다.
- 설명 문구는 `doc/design/screen-designs.md`의 권한 안내 화면 문구를 그대로 사용한다.

## 검증 기준

- 최초 실행 시 권한이 없으면 권한 요청이 뜬다.
- 권한 허용 후 자동으로 프리뷰 시작 로직이 실행된다.
- 권한 거부 후 빈 화면이 아니라 안내 패널이 표시된다.
- 다시 묻지 않음 상태에서는 설정 열기 액션이 표시된다.
- 설정에서 권한을 켜고 돌아오면 프리뷰가 시작된다.

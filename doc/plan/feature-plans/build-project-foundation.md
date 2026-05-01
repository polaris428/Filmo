# 프로젝트 기반 만들기

## 목표

기능 개발 전에 모든 작업자가 같은 패키지 구조, 의존성, 테마, 네비게이션, 권한 기준 위에서 개발할 수 있게 프로젝트 기반을 만든다.

이 플랜을 시작하기 전에 [Filmo 베이스 개발 프롬프트](../../prompt/base-development-prompt.md)를 먼저 읽고 적용한다.

## 구현 범위

- Gradle 의존성 정리
- 패키지 디렉터리 생성
- Hilt 설정
- Compose theme/design token 구현
- 문자열 리소스와 다국어 기준 정리
- 앱 네비게이션 골격
- AndroidManifest 권한 선언

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/build.gradle.kts` | CameraX, Compose, Hilt, Navigation 의존성 추가 |
| `app/src/main/AndroidManifest.xml` | 카메라 권한, 하드웨어 feature 선언 |
| `app/src/main/java/com/polaris/filmo/FilmoApp.kt` | Hilt Application 클래스 추가 |
| `app/src/main/java/com/polaris/filmo/MainActivity.kt` | Compose entry, theme, navigation host 연결 |
| `app/src/main/java/com/polaris/filmo/core/design/FilmoTheme.kt` | 색상/타이포그래피/shape 토큰 구현 |
| `app/src/main/java/com/polaris/filmo/core/design/FilmoColors.kt` | 디자인 가이드라인 color token 구현 |
| `app/src/main/java/com/polaris/filmo/core/design/FilmoTypography.kt` | Roboto 기반 typography 구현 |
| `app/src/main/java/com/polaris/filmo/core/design/FilmoSpacing.kt` | spacing, size token 구현 |
| `app/src/main/java/com/polaris/filmo/core/design/FilmoShapes.kt` | radius token 구현 |
| `app/src/main/java/com/polaris/filmo/core/design/FilmoMotion.kt` | motion duration token 구현 |
| `app/src/main/java/com/polaris/filmo/core/navigation/FilmoNavHost.kt` | Camera, Gallery, PhotoDetail route 정의 |
| `app/src/main/java/com/polaris/filmo/core/di/AppModule.kt` | 공통 DI module 자리 생성 |
| `app/src/main/res/values/strings.xml` | 기본 문자열 리소스 정의 |
| `app/src/main/res/values-ko/strings.xml` | 한국어 문자열 리소스 정의 |

## 상세 개발 절차

1. `doc/plan/architecture.md`의 `core/domain/data/feature` 구조대로 패키지를 먼저 만든다.
2. `app/build.gradle.kts`에 다음 계열 의존성을 추가한다.
   - Compose
   - CameraX
   - Hilt
   - Navigation Compose
   - lifecycle-viewmodel-compose
3. `AndroidManifest.xml`에 `android.permission.CAMERA`를 선언한다.
4. 카메라가 필수 기능이면 `uses-feature android.hardware.camera.any`를 선언한다.
5. Hilt를 사용하도록 Application 클래스를 만들고 Manifest에 등록한다.
6. `FilmoTheme`는 `doc/design/design-guidelines.md`의 토큰을 그대로 옮긴다.
7. 색상은 `FilmoColors`에 `Color` 값으로 정의한다.
   - `background = #0E0F0F`
   - `surface = #171817`
   - `primary = #E8D8B8`
   - `error = #E35D5B`
   - 필름 accent 5종
8. 타이포그래피는 `FilmoTypography`에 `Display`, `Title`, `Body`, `Label`, `Caption`, `Micro` 역할로 정의한다.
9. 간격과 크기는 `FilmoSpacing`에 dp 값으로 정의한다.
10. radius는 `FilmoShapes`에 shape 또는 corner radius 값으로 정의한다.
11. motion duration은 `FilmoMotion`에 millisecond 값으로 정의한다.
12. 화면에 노출되는 모든 문자는 코드에 직접 쓰지 않고 `strings.xml`에 정의한다.
13. 기본 리소스는 `values/strings.xml`, 한국어 리소스는 `values-ko/strings.xml`에 둔다.
14. `FilmoNavHost`는 다음 route를 가진다.
   - `camera`
   - `gallery`
   - `photoDetail/{photoId}`
15. `MainActivity`는 `FilmoTheme`로 감싼 뒤 `FilmoNavHost`를 표시한다.
16. 아직 실제 화면이 없으면 각 route는 빈 placeholder 화면으로 연결한다.

## 테마 구현 계약

```kotlin
object FilmoColors {
    val Background = Color(0xFF0E0F0F)
    val Surface = Color(0xFF171817)
    val Primary = Color(0xFFE8D8B8)
    val Error = Color(0xFFE35D5B)
}

object FilmoSpacing {
    val Space16 = 16.dp
    val Space24 = 24.dp
}

object FilmoMotion {
    const val Fast = 120
    const val Normal = 220
    const val Slow = 420
}
```

## 문자열 리소스 계약

```xml
<resources>
    <string name="app_name">Filmo</string>
    <string name="camera_permission_title">Camera permission required</string>
    <string name="camera_permission_message">Camera access is needed to preview and capture film photos.</string>
    <string name="action_allow_permission">Allow permission</string>
    <string name="action_open_settings">Open settings</string>
    <string name="action_retry">Retry</string>
    <string name="action_cancel">Cancel</string>
    <string name="action_delete">Delete</string>
    <string name="gallery_title">Gallery</string>
</resources>
```

## 상태/이벤트 계약

```kotlin
object FilmoRoutes {
    const val Camera = "camera"
    const val Gallery = "gallery"
    const val PhotoDetail = "photoDetail/{photoId}"
}
```

## 디자인 적용

- 모든 화면은 `FilmoTheme` 안에서만 그린다.
- 디자인 컴포넌트는 `app/core/design/component` 아래에 둔다.
- 화면별 임시 UI도 최종 색상 토큰을 사용한다.
- 화면 문구는 `stringResource(R.string...)`를 사용한다.
- 접근성 라벨도 `strings.xml`에 정의한다.

## 검증 기준

- 앱이 빌드된다.
- 앱 실행 시 `camera` route로 진입한다.
- Hilt Application 등록 오류가 없다.
- `FilmoTheme`가 모든 Compose 화면에 적용된다.
- Manifest에 카메라 권한이 선언되어 있다.
- 화면에 노출되는 하드코딩 문자열이 없다.
- `values/strings.xml`과 `values-ko/strings.xml`이 존재한다.


# Filmo 베이스 개발 프롬프트

이 문서는 Filmo의 모든 개발 작업 전에 반드시 읽고 지켜야 하는 공통 규칙이다. 개별 기능 플랜보다 우선 적용한다.

## 작업 시작 전 확인

1. [아키텍처 문서](../plan/architecture.md)를 확인한다.
2. [디자인 가이드라인](../design/design-guidelines.md)을 확인한다.
3. [디자인 컴포넌트 명세](../design/design-components.md)를 확인한다.
4. 현재 작업에 해당하는 [기능 단위 구현 플랜](../plan/feature-plans/README.md)을 확인한다.
5. 화면 문구가 필요한 작업이면 이 문서의 문자열 리소스 규칙을 먼저 적용한다.

## 아키텍처 규칙

- 패키지 구조는 `core`, `domain`, `data`, `feature`를 따른다.
- `domain`은 Android framework에 의존하지 않는다.
- `feature`는 Compose 화면, ViewModel, MVI 상태/이벤트/Effect만 담당한다.
- `data`는 repository 구현과 datasource 조합을 담당한다.
- `core`는 CameraX, OpenGL, MediaStore, 디자인 시스템, navigation, DI 같은 플랫폼/공통 구현을 담당한다.
- ViewModel에서 `Context`, `Resources`, Android View에 직접 의존하지 않는다.

## MVI 규칙

- 화면마다 `State`, `Intent`, `Effect`, `ViewModel`을 분리한다.
- 사용자 입력은 `Intent`로 ViewModel에 전달한다.
- 화면을 그리는 정보는 `State`에만 둔다.
- 토스트, 권한 요청, 설정 이동, 네비게이션 같은 일회성 동작은 `Effect`로 처리한다.
- 중복 클릭 방지가 필요한 동작은 상태값으로 막는다.

## Compose UI 규칙

- 모든 화면은 `FilmoTheme` 안에서 그린다.
- 색상, 간격, radius, typography, motion 값은 직접 쓰지 말고 `core/design` 토큰을 사용한다.
- 재사용 가능한 UI는 `core/design/component`에 둔다.
- 화면별 조합은 `feature/*` 화면 파일에서 처리한다.
- 디자인 컴포넌트 명세에 있는 컴포넌트를 우선 사용한다.
- 아이콘만 있는 버튼은 접근성 라벨을 반드시 가진다.

## Theme 규칙

- `doc/design/design-guidelines.md`의 디자인 토큰을 코드로 옮긴다.
- Theme 관련 파일은 아래처럼 나눈다.
  - `FilmoTheme.kt`
  - `FilmoColors.kt`
  - `FilmoTypography.kt`
  - `FilmoSpacing.kt`
  - `FilmoShapes.kt`
  - `FilmoMotion.kt`
- 화면에서 hex color, raw dp, raw sp를 반복해서 직접 쓰지 않는다.
- 예외적으로 한 번만 쓰는 미세 조정값도 가능하면 토큰화 여부를 먼저 검토한다.

## 문자열 리소스 규칙

- 사용자에게 보이는 모든 문자는 `strings.xml`에 기록한다.
- 한국어 문구는 `app/src/main/res/values-ko/strings.xml`에 기록한다.
- 기본 문구는 `app/src/main/res/values/strings.xml`에 기록한다.
- Compose에서는 `stringResource(R.string...)`를 사용한다.
- 접근성 라벨도 `strings.xml`에 기록한다.
- ViewModel은 문자열 문장을 직접 만들지 않는다.
- ViewModel은 메시지 종류나 상태만 전달하고, 실제 문구 변환은 UI에서 처리한다.
- 필름 이름과 설명도 코드 문자열이 아니라 string resource id로 관리한다.

## 문자열 네이밍 규칙

| 종류 | Prefix | 예시 |
| --- | --- | --- |
| 화면 제목 | `title_` | `title_gallery` |
| 액션 | `action_` | `action_retry` |
| 권한 | `permission_` | `permission_camera_title` |
| 오류 | `error_` | `error_preview_failed` |
| 상태 | `status_` | `status_saving_photo` |
| 접근성 | `a11y_` | `a11y_switch_camera` |
| 필름 이름 | `film_name_` | `film_name_portra_400` |
| 필름 설명 | `film_desc_` | `film_desc_portra_400` |
| 다이얼로그 | `dialog_` | `dialog_delete_photo_title` |

## 필수 문자열 예시

```xml
<resources>
    <string name="app_name">Filmo</string>
    <string name="title_gallery">Gallery</string>
    <string name="permission_camera_title">Camera permission required</string>
    <string name="permission_camera_message">Camera access is needed to preview and capture film photos.</string>
    <string name="action_allow_permission">Allow permission</string>
    <string name="action_open_settings">Open settings</string>
    <string name="action_retry">Retry</string>
    <string name="action_cancel">Cancel</string>
    <string name="action_delete">Delete</string>
    <string name="status_saving_photo">Saving photo</string>
    <string name="status_photo_saved">Photo saved</string>
    <string name="error_preview_failed">Could not start camera preview</string>
    <string name="error_save_failed">Could not save photo</string>
</resources>
```

## OpenGL/GLSL 규칙

- 필름 효과는 GLSL Fragment Shader에서 처리한다.
- 프리뷰와 저장 이미지는 같은 렌더링 경로를 사용해야 한다.
- shader compile 실패 시 앱을 중단하지 말고 이전 shader 또는 기본 shader로 fallback한다.
- 필름별 색감 차이는 색보정 matrix, 채도, 대비, 노이즈, 비네팅, 빛샘, 색수차 파라미터로 만든다.
- GL 리소스는 lifecycle에 맞춰 release한다.

## 권한 및 저장 규칙

- 카메라 권한은 런타임 권한으로 요청한다.
- 권한 거부 상태에서는 빈 화면을 보여주지 않는다.
- 다시 묻지 않음 상태에서는 설정 이동 액션을 제공한다.
- 저장 파일은 MediaStore를 통해 `Pictures/Filmo`에 저장한다.
- 저장 실패 시 재시도 가능 상태를 제공한다.

## 완료 전 체크리스트

- 하드코딩된 사용자 노출 문자열이 없는가?
- 새 문구를 `values/strings.xml`과 `values-ko/strings.xml`에 모두 추가했는가?
- 새 UI가 `FilmoTheme`와 디자인 토큰을 사용하는가?
- 접근성 라벨이 필요한 아이콘 버튼에 라벨이 있는가?
- ViewModel이 Android `Context`나 문자열 리소스에 직접 의존하지 않는가?
- 기능 플랜의 검증 기준을 모두 통과하는가?

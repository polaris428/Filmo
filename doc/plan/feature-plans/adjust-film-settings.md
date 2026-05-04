# 필름 설정 조정하기

## 목표

사용자가 선택한 필름 프리셋을 기반으로 색감 파라미터를 직접 조정하고, 변경 결과를 카메라 프리뷰와 촬영 결과에 실시간으로 반영한다.

이 기능은 단순 UI 필터가 아니라 OpenGL 셰이더 uniform을 사용자 입력으로 제어하는 기능이다. 포트폴리오에서는 `CameraX -> OES Texture -> GLSL shader -> 실시간 튜닝 -> 캡처 저장` 흐름을 보여주는 기술 포인트로 사용한다.

## 구현 범위

- 필름 조정값 도메인 모델
- 필름별 기본 조정값
- 카메라 화면의 필름 설정 버튼
- 필름 설정 bottom sheet
- 실시간 shader uniform 반영
- 현재 조정값을 적용한 촬영/저장
- 필름 변경/초기화 시 상태 정책

필름 선택 자체는 [필름 선택하기](./select-film.md)와 [필름 선택을 페이지로 이동하기](./move-film-selection-to-page.md)의 흐름을 유지한다. 셰이더 구조는 [필름 GLSL 셰이더 구현하기](./implement-film-glsl-shaders.md)와 [OpenGL 렌더링 파이프라인 만들기](./build-opengl-rendering-pipeline.md)를 확장한다.

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/src/main/java/com/polaris/filmo/domain/model/FilmAdjustment.kt` | 사용자 조정 파라미터 모델 추가 |
| `app/src/main/java/com/polaris/filmo/domain/model/FilmType.kt` | 필름별 기본 조정값 연결 |
| `app/src/main/java/com/polaris/filmo/core/renderer/FilmShaderConfig.kt` | shader uniform 파라미터 확장 |
| `app/src/main/java/com/polaris/filmo/core/renderer/FilmRenderer.kt` | `setFilmAdjustment()` 또는 `setFilm(film, adjustment)` 제공 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraState.kt` | `filmAdjustment`, `isFilmSettingsVisible` 추가 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraIntent.kt` | 설정 열기/닫기/값 변경/초기화 intent 추가 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraViewModel.kt` | 조정값 상태 관리와 렌더러 반영 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraScreen.kt` | 필름 설정 버튼과 bottom sheet UI 추가 |
| `app/src/main/res/values/strings.xml` | 필름 설정 관련 문자열 추가 |
| `app/src/main/res/values-ko/strings.xml` | 한국어 문자열 추가 |

## 조정 파라미터

초기 구현은 효과가 명확하고 설명하기 쉬운 6개 값으로 제한한다.

| 파라미터 | 범위 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `temperature` | `-1.0f..1.0f` | 필름별 기본값 | 따뜻함/차가움 조정 |
| `tint` | `-1.0f..1.0f` | 필름별 기본값 | 녹색/마젠타 색조 조정 |
| `contrast` | `0.0f..2.0f` | 필름별 기본값 | 명암 대비 |
| `saturation` | `0.0f..2.0f` | 필름별 기본값 | 채도 |
| `grain` | `0.0f..1.0f` | 필름별 기본값 | 필름 그레인 강도 |
| `vignette` | `0.0f..1.0f` | 필름별 기본값 | 주변부 어두워짐 |

추후 확장 후보:

- `fade`
- `halation`
- `lightLeak`
- `chromaticAberration`
- `sharpness`

## 상세 개발 절차

1. `FilmAdjustment` data class를 추가한다.
2. 각 값은 렌더러에서 바로 쓰기 쉬운 `Float` 범위로 정의한다.
3. `FilmType`에 `defaultAdjustment`를 추가한다.
4. `CameraState`에 현재 필름 조정값을 저장한다.
5. 앱 첫 진입 시 선택 필름의 기본 조정값을 사용한다.
6. 필름을 변경하면 새 필름의 기본 조정값으로 전환한다.
7. 카메라 메뉴 영역의 필름 버튼 옆에 설정 아이콘 버튼을 추가한다.
8. 설정 버튼을 누르면 bottom sheet를 연다.
9. bottom sheet에는 현재 필름명과 6개 slider를 표시한다.
10. slider를 움직이면 `OnChangeFilmAdjustment(parameter, value)` intent를 보낸다.
11. ViewModel은 상태를 즉시 갱신하고 렌더러에 새 조정값을 전달한다.
12. 렌더러는 다음 프레임부터 shader uniform으로 조정값을 반영한다.
13. `Reset`을 누르면 현재 필름의 기본 조정값으로 되돌린다.
14. 셔터를 누르면 현재 조정값이 적용된 GL 프레임을 캡처한다.

## 상태/이벤트 계약

```kotlin
data class FilmAdjustment(
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val grain: Float = 0.25f,
    val vignette: Float = 0.35f
)

data class CameraState(
    val selectedFilm: FilmType = FilmType.Portra400,
    val filmAdjustment: FilmAdjustment = FilmType.Portra400.defaultAdjustment,
    val isFilmSettingsVisible: Boolean = false
)

sealed interface CameraIntent {
    data object OnOpenFilmSettings : CameraIntent
    data object OnCloseFilmSettings : CameraIntent
    data object OnResetFilmAdjustment : CameraIntent
    data class OnChangeFilmAdjustment(
        val parameter: FilmAdjustmentParameter,
        val value: Float
    ) : CameraIntent
}
```

## 렌더러 계약

```kotlin
interface FilmRendererController {
    suspend fun setFilm(filmType: FilmType)
    suspend fun setFilmAdjustment(adjustment: FilmAdjustment)
}
```

대안으로 `setFilm(filmType, adjustment)` 하나로 합칠 수 있다. 현재 코드의 렌더러 호출 패턴을 확인한 뒤, 필름 전환과 조정값 변경이 분리되어 있으면 별도 메서드가 더 명확하다.

## 셰이더 적용 방향

- `uTemperature`
- `uTint`
- `uContrast`
- `uSaturation`
- `uGrainAmount`
- `uVignetteAmount`

기존 필름별 셰이더가 이미 색보정/노이즈/비네팅을 갖고 있다면, 사용자 조정값은 필름 기본값에 곱하거나 더하는 override 값으로 처리한다.

예:

```text
finalContrast = filmBaseContrast * userContrast
finalSaturation = filmBaseSaturation * userSaturation
finalGrain = clamp(filmBaseGrain + userGrainOffset, 0.0, 1.0)
```

## 디자인 적용

- 카메라 메뉴 영역에서 필름 선택 버튼과 설정 버튼을 같은 행에 둔다.
- 설정 버튼은 텍스트가 아니라 아이콘 버튼으로 제공한다.
- bottom sheet는 카메라 프리뷰를 가리지 않도록 메뉴 영역 위에서 올라오는 형태를 우선 검토한다.
- slider label은 짧게 유지한다.
- 현재 값은 숫자보다 slider thumb 위치로 이해되게 한다.
- `Reset`은 보조 버튼으로 배치한다.

## 검증 기준

- 설정 버튼을 누르면 현재 선택된 필름의 설정 sheet가 열린다.
- slider 변경 시 프리뷰 색감이 즉시 바뀐다.
- 촬영 결과에 현재 조정값이 반영된다.
- 필름을 변경하면 해당 필름의 기본 조정값으로 전환된다.
- reset을 누르면 현재 필름의 기본 조정값으로 돌아간다.
- 빠르게 slider를 움직여도 UI thread가 멈추지 않는다.
- 설정 sheet가 열려 있어도 촬영/저장 상태가 깨지지 않는다.
- 앱을 백그라운드로 보냈다가 돌아와도 렌더러와 상태가 일치한다.

## 포트폴리오 어필 포인트

- 실시간 카메라 프레임을 GPU shader uniform으로 조정한다.
- 필름 프리셋과 사용자 조정값을 분리해 확장 가능한 렌더링 모델을 만든다.
- 촬영 결과는 UI 후처리가 아니라 실제 GL 렌더링 결과를 저장한다.
- 단순 색상 필터가 아니라 `temperature`, `tint`, `contrast`, `saturation`, `grain`, `vignette`를 렌더링 파이프라인에서 제어한다.


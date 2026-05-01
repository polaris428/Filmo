# 06. 필름 선택하기

## 목표

사용자가 필름을 선택하면 프리뷰에 즉시 반영되고, 선택된 필름 상태가 화면과 렌더러에 일관되게 유지된다.

## 구현 범위

- 필름 도메인 모델
- 필름 목록 유스케이스
- 필름 선택 UI
- 렌더러 셰이더 전환 요청
- 선택 실패 처리

셰이더 파일과 OpenGL 렌더링 파이프라인 구현은 [03. OpenGL 렌더링 파이프라인 만들기](./03-build-opengl-rendering-pipeline.md), [04. 필름 GLSL 셰이더 구현하기](./04-implement-film-glsl-shaders.md)에서 처리한다. 필름 썸네일과 표시 문구는 [05. 필름 프리뷰 자산 만들기](./05-build-film-preview-assets.md)에서 준비한다.

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/src/main/java/com/polaris/filmo/domain/model/FilmType.kt` | 필름 enum 또는 sealed class 정의 |
| `app/src/main/java/com/polaris/filmo/domain/usecase/GetFilmListUseCase.kt` | 필름 목록 반환 |
| `app/src/main/java/com/polaris/filmo/feature/camera/FilmUiModel.kt` | UI 표시용 모델 정의 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraState.kt` | `films`, `selectedFilmId`, `isApplyingFilm` 추가 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraIntent.kt` | `OnSelectFilm(filmId)` 추가 |
| `app/src/main/java/com/polaris/filmo/core/renderer/FilmRenderer.kt` | `setFilm(filmType)` 인터페이스 제공 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraScreen.kt` | `FilmStrip`, `FilmChip` 연결 |

## 상세 개발 절차

1. `FilmType`에 5개 필름을 정의한다.
   - `Portra400`
   - `Velvia50`
   - `IlfordHp5`
   - `Lomography`
   - `ExpiredFilm`
2. 각 필름은 다음 값을 가진다.
   - `id`
   - `displayName`
   - `description`
   - `shaderAssetName`
   - `accentColorToken`
3. `GetFilmListUseCase`는 정렬된 필름 목록을 반환한다.
4. `CameraViewModel` 초기화 시 `GetFilmListUseCase()`를 호출해 `CameraState.films`에 넣는다.
5. 첫 선택값은 `Portra400.id`로 둔다.
6. 사용자가 `FilmChip`을 누르면 `OnSelectFilm(filmId)`를 보낸다.
7. 이미 선택된 필름이면 아무 작업도 하지 않는다.
8. 새 필름이면 `isApplyingFilm = true`로 바꾼다.
9. ViewModel은 `FilmRenderer.setFilm(filmType)`를 호출한다.
10. 성공하면 `selectedFilmId = filmId`, `isApplyingFilm = false`로 갱신한다.
11. 실패하면 이전 선택값을 유지하고 `CameraEffect.ShowMessage("필름을 적용하지 못했어요")`를 발생시킨다.
12. 빠른 연속 선택은 마지막 요청만 최종 상태로 반영한다.

## 상태/이벤트 계약

```kotlin
data class FilmUiModel(
    val id: String,
    val name: String,
    val description: String,
    val thumbnailResName: String,
    val accentColorToken: String
)

data class CameraState(
    val films: List<FilmUiModel> = emptyList(),
    val selectedFilmId: String = "portra_400",
    val isApplyingFilm: Boolean = false
)
```

## 디자인 적용

- 필름 목록은 `FilmStrip`을 사용한다.
- 개별 항목은 `FilmChip`을 사용한다.
- 선택된 항목은 2dp `color.primary` 테두리와 체크 표시를 사용한다.
- 필름 설명은 한 줄만 표시하고 말줄임 처리한다.
- 하단 셔터 영역과 겹치지 않게 배치한다.

## 검증 기준

- 필름 5종이 정해진 순서로 표시된다.
- 앱 첫 진입 시 Portra 400이 선택되어 있다.
- 필름 선택 시 `FilmRenderer.setFilm()`이 호출된다.
- 렌더러 적용 성공 후 선택 UI가 갱신된다.
- 렌더러 적용 실패 시 이전 필름 선택이 유지된다.

## 구현 결과

- `GetFilmListUseCase`는 `FilmType.entries`를 정렬 순서 그대로 반환한다.
- `CameraState`는 `films`, `selectedFilm`, `selectedFilmId`, `isApplyingFilm`을 가진다.
- `CameraIntent.OnSelectFilm(filmId)`가 추가되었고, ViewModel은 빠른 연속 선택 시 이전 선택 job을 취소한다.
- 선택 성공 시 `FilmRenderer.setFilm()`을 호출하고, 실패 시 이전 필름으로 되돌린 뒤 `ShowError` effect를 보낸다.
- `CameraScreen`은 preview ready 상태에서 하단 `FilmStrip`을 표시하고, 각 항목은 썸네일/이름/설명/선택 표시/적용 중 indicator를 가진다.

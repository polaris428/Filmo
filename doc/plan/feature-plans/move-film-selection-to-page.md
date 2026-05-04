# 필름 선택을 별도 페이지로 분리하기

## 목표

카메라 화면 하단에 직접 노출된 필름 선택 UI를 제거하고, 별도 필름 선택 페이지에서 필름을 고른 뒤 카메라 프리뷰로 돌아오게 한다.

카메라 프리뷰 재바인딩은 허용한다. 단, 복귀 과정에서 로딩 오버레이, 지속되는 검은 화면, 깨진 프레임이 사용자에게 노출되지 않게 한다.

## 구현 범위

- 카메라 화면의 하단 `FilmStrip` 제거
- 필름 선택 페이지 라우트 추가
- 카메라 화면에서 필름 선택 페이지로 이동하는 compact 버튼 추가
- 필름 선택 페이지 MVI 구조
- Navigation saved state handle 기반 선택 결과 전달
- 선택 완료 후 카메라 화면 복귀 및 기존 `CameraIntent.OnSelectFilm(filmId)` 재사용

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/src/main/java/com/polaris/filmo/core/navigation/FilmoNavHost.kt` | `FilmSelection` 라우트, result 수신, camera ViewModel 전달 연결 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraScreen.kt` | 하단 `FilmStrip` 제거, 필름 선택 페이지 진입 버튼 추가 |
| `app/src/main/java/com/polaris/filmo/feature/camera/CameraViewModel.kt` | 기존 `OnSelectFilm(filmId)` 로직 재사용 |
| `app/src/main/java/com/polaris/filmo/feature/filmselection/FilmSelectionScreen.kt` | 필름 선택 페이지 작성 |
| `app/src/main/java/com/polaris/filmo/feature/filmselection/FilmSelectionViewModel.kt` | 필름 목록/현재 선택 상태 관리 |
| `app/src/main/java/com/polaris/filmo/feature/filmselection/FilmSelectionState.kt` | 목록, 선택값, 오류 상태 정의 |
| `app/src/main/java/com/polaris/filmo/feature/filmselection/FilmSelectionIntent.kt` | 진입, 필름 클릭, 뒤로가기 intent 정의 |
| `app/src/main/java/com/polaris/filmo/feature/filmselection/FilmSelectionEffect.kt` | 선택 완료/뒤로가기 effect 정의 |

## 선택 결과 전달 계약

필름 선택 결과는 Navigation saved state handle을 사용한다. 필름 선택 화면은 카메라 ViewModel이나 renderer를 직접 참조하지 않는다. 실제 필름 적용은 카메라 화면으로 돌아온 뒤 `CameraViewModel`이 수행한다.

```kotlin
const val FILM_SELECTION_RESULT_KEY = "filmSelectionResult"
```

카메라에서 필름 선택 화면으로 이동할 때 현재 선택값은 route argument로 전달한다.

```kotlin
navController.navigate(FilmoRoutes.filmSelection(currentFilmId))
```

필름 선택 화면에서 선택 완료 시 결과를 이전 back stack entry에 저장하고 뒤로 돌아간다.

```kotlin
navController.previousBackStackEntry
    ?.savedStateHandle
    ?.set(FILM_SELECTION_RESULT_KEY, filmId)

navController.popBackStack()
```

카메라 route는 자신의 saved state handle에서 결과를 수신한다. 수신 후에는 같은 결과가 재처리되지 않도록 즉시 제거한다.

```kotlin
val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
val selectedFilmId by savedStateHandle
    ?.getStateFlow<String?>(FILM_SELECTION_RESULT_KEY, null)
    ?.collectAsStateWithLifecycle()

LaunchedEffect(selectedFilmId) {
    if (selectedFilmId != null) {
        cameraViewModel.handleIntent(CameraIntent.OnSelectFilm(selectedFilmId))
        savedStateHandle.remove<String>(FILM_SELECTION_RESULT_KEY)
    }
}
```

결과 수신은 `FilmoNavHost`의 camera destination 안에서 처리한다. `CameraScreen`은 Navigation API를 직접 알지 않는다.

## 상세 개발 절차

1. `FilmoRoutes`에 `FilmSelection = "filmSelection/{selectedFilmId}"` 라우트를 추가한다.
2. `FilmoRoutes.filmSelection(selectedFilmId)` helper를 추가한다.
3. 카메라 destination에서 `CameraViewModel`을 `hiltViewModel()`로 가져온다.
4. 카메라 destination에서 `FILM_SELECTION_RESULT_KEY`를 observe하고, 새 값이 있으면 `CameraIntent.OnSelectFilm(filmId)`를 보낸 뒤 결과를 제거한다.
5. `CameraScreen`에는 `onOpenFilmSelection(selectedFilmId)` 콜백을 추가한다.
6. 카메라 화면의 기존 하단 `FilmStrip`/`FilmChip` 노출을 제거한다.
7. 카메라 화면에는 현재 선택된 필름명과 필름 선택 페이지 진입 버튼만 표시한다.
8. 필름 선택 버튼 클릭 시 `onOpenFilmSelection(state.selectedFilmId)`를 호출한다.
9. `FilmSelectionScreen`은 route argument로 받은 `selectedFilmId`와 전체 필름 목록을 표시한다.
10. 현재 선택된 필름은 테두리, accent color, 체크 표시로 구분한다.
11. 사용자가 필름을 선택하면 `FilmSelectionEffect.SelectFilmAndNavigateBack(filmId)`를 발생시킨다.
12. `FilmoNavHost`는 effect callback에서 결과를 saved state handle에 저장하고 `popBackStack()`을 호출한다.
13. 카메라 ViewModel은 기존 `OnSelectFilm(filmId)` 로직으로 renderer에 필름 적용을 요청한다.
14. 렌더러 적용 성공 시 카메라 화면의 현재 필름 표시를 갱신한다.
15. 렌더러 적용 실패 시 이전 필름을 유지하고 오류 메시지를 표시한다.
16. 뒤로가기만 누른 경우에는 결과를 저장하지 않고 카메라 화면으로 돌아간다.

## 상태/이벤트 계약

```kotlin
data class FilmSelectionState(
    val films: List<FilmType> = emptyList(),
    val selectedFilmId: String = "portra_400",
    val errorMessage: String? = null
)

sealed interface FilmSelectionIntent {
    data object OnBackClick : FilmSelectionIntent
    data class OnClickFilm(val filmId: String) : FilmSelectionIntent
}

sealed interface FilmSelectionEffect {
    data object NavigateBack : FilmSelectionEffect
    data class SelectFilmAndNavigateBack(val filmId: String) : FilmSelectionEffect
}
```

카메라 쪽 수신 계약은 기존 intent를 재사용한다.

```kotlin
sealed interface CameraIntent {
    data class OnSelectFilm(val filmId: String) : CameraIntent
}
```

## 디자인 적용

- 필름 선택 페이지는 카메라 프리뷰와 분리된 독립 화면으로 구성한다.
- 상단에는 뒤로가기 버튼과 `Film` 제목을 표시한다.
- 필름 목록은 2열 그리드로 표시한다.
- 각 필름 항목은 썸네일, 이름, 설명, 선택 상태를 포함한다.
- 카메라 화면에는 현재 필름을 확인할 수 있는 compact 버튼만 남긴다.
- compact 버튼은 셔터 버튼, 저장 상태 toast, 카메라 전환 버튼과 겹치지 않게 배치한다.

## 검증 기준

- 카메라 화면 하단에 기존 필름 스트립이 표시되지 않는다.
- 카메라 화면에서 필름 선택 페이지로 이동할 수 있다.
- 필름 선택 페이지에서 현재 선택된 필름이 표시된다.
- 필름을 선택하면 카메라 화면으로 돌아오고 프리뷰 필터가 변경된다.
- 뒤로가기만 하면 기존 필름 선택이 유지된다.
- 필름 선택/복귀 시 카메라 preview 재바인딩은 허용한다.
- 복귀 과정에서 로딩 오버레이, 지속되는 검은 화면, 깨진 프레임이 사용자에게 노출되지 않는다.
- 필름 선택/복귀 후 촬영과 저장이 정상 동작한다.

## 구현 메모

- 기존 `CameraViewModel.OnSelectFilm(filmId)` 로직을 재사용한다.
- 결과 key는 `filmSelectionResult` 하나로 고정하고, 수신 후 반드시 remove해서 같은 선택이 재적용되지 않게 한다.
- 필름 선택 화면에는 renderer 적용 책임을 두지 않는다.
- 필름 선택 화면은 카메라 ViewModel을 직접 참조하지 않는다.
- 카메라 화면 복귀 시 기존 `Ready` 상태를 `Loading`으로 내리지 않는다.
- 새 GL surface가 준비되면 CameraX preview만 재바인딩한다.
- 재바인딩 실패 시에만 `Error` 상태로 전환한다.

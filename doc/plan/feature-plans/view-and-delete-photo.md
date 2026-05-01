# 사진 상세 보기와 삭제

## 목표

사용자가 갤러리에서 선택한 사진을 크게 보고, 삭제 확인 후 앱과 기기 갤러리에서 제거할 수 있게 한다.

## 구현 범위

- 사진 상세 화면
- 사진 로드 상태
- 삭제 확인 다이얼로그
- 삭제 유스케이스
- 삭제 성공 후 목록 갱신

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/domain/usecase/DeletePhotoUseCase.kt` | 사진 삭제 유스케이스 작성 |
| `app/domain/repository/PhotoRepository.kt` | `deletePhoto(photoId)` 추가 |
| `app/core/media/MediaStorePhotoDataSource.kt` | MediaStore 삭제 구현 |
| `app/feature/gallery/detail/PhotoDetailScreen.kt` | 상세 화면 작성 |
| `app/feature/gallery/detail/PhotoDetailViewModel.kt` | 상세/삭제 상태 관리 |
| `app/feature/gallery/detail/PhotoDetailState.kt` | 로딩/상세/삭제 상태 정의 |
| `app/feature/gallery/detail/PhotoDetailIntent.kt` | 뒤로가기, 삭제 Intent 정의 |
| `app/feature/gallery/detail/PhotoDetailEffect.kt` | 뒤로가기, 메시지 Effect 정의 |

## 상세 개발 절차

1. 상세 화면은 `photoId` 또는 `uri`를 navigation argument로 받는다.
2. `PhotoDetailState`를 정의한다.
   - `isLoading`
   - `photo: PhotoUiModel?`
   - `isDeleteDialogVisible`
   - `isDeleting`
   - `errorMessage`
3. 화면 진입 시 `photoId`로 사진 정보를 조회한다.
4. 조회 성공 시 전체 화면에 사진을 표시한다.
5. 조회 실패 시 `ErrorState`를 표시하고 목록으로 돌아갈 수 있게 한다.
6. 삭제 아이콘 클릭 시 `isDeleteDialogVisible = true`로 바꾼다.
7. 다이얼로그 취소는 `isDeleteDialogVisible = false`만 수행한다.
8. 삭제 확인 시 `isDeleting = true`로 바꾸고 `DeletePhotoUseCase(photoId)`를 호출한다.
9. 삭제 성공 시 다이얼로그를 닫고 `PhotoDetailEffect.NavigateBackWithRefresh`를 발생시킨다.
10. 삭제 실패 시 다이얼로그는 유지하고 오류 메시지를 표시한다.
11. 목록 화면은 `NavigateBackWithRefresh`를 받으면 목록을 다시 조회한다.

## 상태/이벤트 계약

```kotlin
sealed interface PhotoDetailIntent {
    data object OnEnterScreen : PhotoDetailIntent
    data object OnBackClick : PhotoDetailIntent
    data object OnDeleteClick : PhotoDetailIntent
    data object OnCancelDelete : PhotoDetailIntent
    data object OnConfirmDelete : PhotoDetailIntent
}

sealed interface PhotoDetailEffect {
    data object NavigateBack : PhotoDetailEffect
    data object NavigateBackWithRefresh : PhotoDetailEffect
    data class ShowMessage(val message: String) : PhotoDetailEffect
}
```

## 디자인 적용

- 상세 화면 배경은 `color.background`다.
- 사진은 원본 비율을 유지하고 화면 안에 맞춘다.
- 상단은 `AppTopBar` 또는 오버레이 `CircleIconButton` 두 개를 사용한다.
- 삭제 확인은 `ConfirmDialog`를 사용한다.
- 삭제 버튼은 `DangerButton` 또는 `color.error` 스타일을 사용한다.

## 검증 기준

- 목록에서 선택한 사진이 상세 화면에 표시된다.
- 사진 로드 실패 시 오류 상태가 표시된다.
- 삭제 클릭 시 확인 다이얼로그가 열린다.
- 삭제 성공 후 목록으로 돌아가고 해당 사진이 사라진다.
- 삭제 실패 시 사용자가 다시 시도할 수 있다.


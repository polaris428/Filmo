# 촬영 사진 목록 보기

## 목표

사용자가 Filmo로 촬영한 사진을 앱 내 갤러리에서 최신순 그리드로 확인할 수 있게 한다.

## 구현 범위

- 갤러리 화면 MVI 구조
- 촬영 사진 목록 조회
- 로딩/빈 상태/오류 상태
- 사진 썸네일 그리드
- 상세 화면 이동 이벤트

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/domain/usecase/GetCapturedPhotosUseCase.kt` | 저장된 사진 목록 조회 |
| `app/feature/gallery/GalleryScreen.kt` | 갤러리 화면 작성 |
| `app/feature/gallery/GalleryViewModel.kt` | 목록 조회 상태 관리 |
| `app/feature/gallery/GalleryState.kt` | 로딩/빈/목록/오류 상태 정의 |
| `app/feature/gallery/GalleryIntent.kt` | 진입, 새로고침, 사진 클릭 Intent 정의 |
| `app/feature/gallery/GalleryEffect.kt` | 상세 이동 Effect 정의 |
| `app/feature/gallery/PhotoUiModel.kt` | 썸네일 UI 모델 정의 |

## 상세 개발 절차

1. `PhotoRepository.getCapturedPhotos()` 인터페이스를 정의하거나 기존 인터페이스에 추가한다.
2. `GetCapturedPhotosUseCase`는 repository에서 사진 목록을 받아 최신순으로 정렬한다.
3. `GalleryState`를 정의한다.
   - `isLoading`
   - `photos`
   - `errorMessage`
   - derived: `isEmpty`
4. `GalleryIntent.OnEnterScreen`에서 목록 조회를 시작한다.
5. 조회 시작 시 `isLoading = true`, `errorMessage = null`로 바꾼다.
6. 조회 성공 시 `photos`를 `PhotoUiModel`로 변환한다.
7. 결과가 비어 있으면 `EmptyState`를 표시한다.
8. 조회 실패 시 `ErrorState`와 재시도 버튼을 표시한다.
9. 사진 클릭 시 `GalleryEffect.NavigateToPhotoDetail(photoId)`를 발생시킨다.
10. 외부에서 삭제된 uri는 조회 단계에서 제외하거나 로드 실패 상태로 표시한다.

## 상태/이벤트 계약

```kotlin
data class GalleryState(
    val isLoading: Boolean = false,
    val photos: List<PhotoUiModel> = emptyList(),
    val errorMessage: String? = null
)

data class PhotoUiModel(
    val id: String,
    val uri: String,
    val createdAtText: String
)
```

## 디자인 적용

- 상단은 `AppTopBar(title = "Gallery")`를 사용한다.
- 목록은 `PhotoThumbnail`을 3열 그리드로 배치한다.
- 로딩은 `SkeletonBox` 3열을 표시한다.
- 빈 상태는 `EmptyState`를 사용하고 액션은 `카메라로 돌아가기`다.
- 오류 상태는 `ErrorState`를 사용하고 액션은 `다시 시도`다.

## 검증 기준

- 사진 목록이 최신순으로 표시된다.
- 사진이 없으면 빈 상태가 표시된다.
- 조회 실패 시 재시도할 수 있다.
- 사진 클릭 시 상세 이동 Effect가 발생한다.
- 저장 직후 갤러리에 들어가면 새 사진이 첫 번째에 표시된다.


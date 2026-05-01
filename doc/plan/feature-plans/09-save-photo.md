# 09. 사진 저장하기

## 목표

캡처된 이미지를 기기 갤러리에 저장하고, 앱 내 갤러리에서 다시 조회할 수 있는 사진 정보로 남긴다.

## 구현 범위

- 사진 저장 유스케이스
- MediaStore 저장 데이터소스
- 저장 상태 UI
- 저장 성공 후 갤러리 목록 반영
- 저장 실패 재시도

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/domain/model/CapturedPhoto.kt` | 저장된 사진 모델 정의 |
| `app/domain/repository/PhotoRepository.kt` | 저장/조회/삭제 인터페이스 정의 |
| `app/domain/usecase/SavePhotoUseCase.kt` | 저장 유스케이스 작성 |
| `app/data/repository/PhotoRepositoryImpl.kt` | repository 구현 |
| `app/data/datasource/PhotoLocalDataSource.kt` | 로컬 저장 추상화 |
| `app/core/media/MediaStorePhotoDataSource.kt` | MediaStore 저장 구현 |
| `app/feature/camera/CameraState.kt` | `saveStatus`, `lastSavedPhoto` 추가 |

## 상세 개발 절차

1. `CapturedPhoto` 모델을 정의한다.
   - `id`
   - `uri`
   - `filmType`
   - `createdAt`
   - `width`
   - `height`
2. `PhotoRepository.savePhoto(frame, filmType)` 인터페이스를 정의한다.
3. `SavePhotoUseCase`는 repository에 저장을 위임하고 `CapturedPhoto`를 반환한다.
4. `MediaStorePhotoDataSource`는 다음 규칙으로 파일을 저장한다.
   - display name: `Filmo_yyyyMMdd_HHmmss`
   - mime type: `image/jpeg`
   - relative path: `Pictures/Filmo`
5. 저장 시작 시 `saveStatus = Saving`으로 바꾼다.
6. 저장 성공 시 `saveStatus = Success`, `lastSavedPhoto = CapturedPhoto`로 갱신한다.
7. 저장 실패 시 `saveStatus = Failure(reason)`로 바꾸고 `isCapturing = false`로 되돌린다.
8. 실패 토스트의 재시도 버튼은 마지막 캡처 결과가 남아 있는 경우에만 노출한다.
9. 저장 성공 후 2초 뒤 `saveStatus`를 null로 초기화한다.
10. 저장 성공한 `CapturedPhoto`는 갤러리 조회 결과에 포함되어야 한다.

## 상태/이벤트 계약

```kotlin
sealed interface SaveStatus {
    data object Saving : SaveStatus
    data class Success(val photo: CapturedPhoto) : SaveStatus
    data class Failure(val reason: String, val canRetry: Boolean) : SaveStatus
}
```

## 디자인 적용

- 저장 중/성공/실패는 `StatusToast`로 표시한다.
- 저장 중에는 `ShutterButton`을 `capturing` 상태로 유지한다.
- 저장 성공 시 `DevelopThumbnail(done)` 애니메이션을 실행한다.
- 저장 실패 시 `StatusToast(error)`와 재시도 액션을 표시한다.

## 검증 기준

- 저장 성공 시 파일이 `Pictures/Filmo`에 생성된다.
- 저장된 파일은 기기 갤러리에서 확인 가능하다.
- 저장 성공 후 앱 갤러리 목록에서 조회 가능하다.
- 저장 실패 시 앱이 멈추지 않고 재시도할 수 있다.
- 저장 중 중복 저장이 발생하지 않는다.

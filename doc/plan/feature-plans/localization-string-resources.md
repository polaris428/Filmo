# 문자열 리소스와 다국어 정리

## 목표

앱에 노출되는 모든 문구를 `strings.xml`로 분리하고, 한국어/기본 언어 리소스를 관리해 다국어 대응이 가능한 상태로 만든다.

## 구현 범위

- 화면 노출 문구 리소스화
- 접근성 라벨 리소스화
- 필름 이름/설명 리소스화
- 오류/상태 메시지 리소스화
- 기본 리소스와 한국어 리소스 분리

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/src/main/res/values/strings.xml` | 기본 문자열 리소스 |
| `app/src/main/res/values-ko/strings.xml` | 한국어 문자열 리소스 |
| `app/domain/model/FilmType.kt` | 필름 표시 문자열을 res id 기반으로 변경 |
| `app/feature/camera/FilmUiModel.kt` | 필름 이름/설명 string res id 또는 resolved text 처리 |
| `app/feature/camera/CameraScreen.kt` | 모든 문구를 `stringResource`로 교체 |
| `app/feature/gallery/GalleryScreen.kt` | 모든 문구를 `stringResource`로 교체 |
| `app/feature/gallery/detail/PhotoDetailScreen.kt` | 모든 문구를 `stringResource`로 교체 |

## 상세 개발 절차

1. 코드에 직접 작성된 사용자 노출 문자열을 모두 찾는다.
2. 각 문자열에 stable resource name을 부여한다.
3. `values/strings.xml`에 기본 언어 문구를 작성한다.
4. `values-ko/strings.xml`에 한국어 문구를 작성한다.
5. Compose 화면에서는 `stringResource(R.string.xxx)`로 문구를 가져온다.
6. ViewModel은 Android `Context`에 의존하지 않도록 문자열을 직접 만들지 않는다.
7. ViewModel에서 메시지 종류만 Effect로 전달하고, 화면에서 string resource로 변환한다.
8. 필름 이름과 설명은 `FilmType`에 문자열 자체가 아니라 `@StringRes nameRes`, `@StringRes descriptionRes`를 둔다.
9. 접근성 라벨도 별도 string resource로 정의한다.
10. 포맷 문자열은 placeholder를 사용한다.
    - 예: `<string name="photo_taken_at">%1$s에 촬영한 사진</string>`

## 리소스 네이밍 규칙

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

## 필수 문자열 목록

| Key | 기본값 | 한국어 |
| --- | --- | --- |
| `app_name` | Filmo | Filmo |
| `title_gallery` | Gallery | 갤러리 |
| `permission_camera_title` | Camera permission required | 카메라 권한이 필요해요 |
| `permission_camera_message` | Camera access is needed to preview and capture film photos. | 필름 효과를 입힌 프리뷰와 촬영을 위해 카메라 접근이 필요합니다. |
| `permission_camera_settings_title` | Enable camera permission in settings | 설정에서 카메라 권한을 켜주세요 |
| `permission_camera_settings_message` | Camera permission is disabled, so the preview cannot start. | 권한이 꺼져 있어 프리뷰를 시작할 수 없습니다. |
| `action_allow_permission` | Allow permission | 권한 허용 |
| `action_open_settings` | Open settings | 설정 열기 |
| `action_retry` | Retry | 다시 시도 |
| `action_cancel` | Cancel | 취소 |
| `action_delete` | Delete | 삭제 |
| `action_back_to_camera` | Back to camera | 카메라로 돌아가기 |
| `status_saving_photo` | Saving photo | 사진 저장 중 |
| `status_photo_saved` | Photo saved | 사진 저장 완료 |
| `error_preview_failed` | Could not start camera preview | 카메라 프리뷰를 시작하지 못했어요 |
| `error_save_failed` | Could not save photo | 사진을 저장하지 못했어요 |
| `empty_gallery_title` | No photos yet | 아직 촬영한 사진이 없어요 |
| `empty_gallery_message` | Choose a film and take your first photo. | 필름을 고르고 첫 사진을 남겨보세요. |
| `dialog_delete_photo_title` | Delete this photo? | 사진을 삭제할까요? |
| `dialog_delete_photo_message` | This photo will be removed from the app gallery. | 삭제한 사진은 앱 갤러리에서 사라집니다. |

## 검증 기준

- 화면에 노출되는 하드코딩 문자열이 없다.
- 접근성 라벨이 string resource로 관리된다.
- `values/strings.xml`과 `values-ko/strings.xml`에 같은 key가 존재한다.
- 기기 언어가 한국어일 때 한국어 문구가 표시된다.
- 기기 언어가 한국어가 아닐 때 기본 문구가 표시된다.


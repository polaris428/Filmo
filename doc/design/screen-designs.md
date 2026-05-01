# 화면 디자인 명세

이 문서는 재사용 컴포넌트를 조합해 각 화면을 어떻게 구성할지 정의한다. 개별 부품의 상세 명세는 [디자인 컴포넌트 명세](./design-components.md)를 따른다.

## 카메라 화면

### 구성

- 카메라 프리뷰
- 상단 아이콘 버튼 영역
- 필름 선택 `FilmStrip`
- `ShutterButton`
- `StatusToast`
- `FocusRing`
- `CaptureFlash`
- `DevelopThumbnail`

### 배치

- 프리뷰는 전체 화면을 채운다.
- 상단 좌측에는 갤러리 열기, 상단 우측에는 카메라 전환을 둔다.
- 필름 선택은 하단에 두되 셔터 버튼과 겹치지 않는다.
- 셔터 버튼은 하단 중앙에 고정한다.
- 저장 상태 토스트는 셔터 버튼 위에 표시한다.

## 권한 안내 화면

### 구성

- 어두운 배경 또는 프리뷰 대체 배경
- `OverlayPanel`
- `PrimaryButton`
- optional `SecondaryButton`

### 문구

| 상태 | 제목 | 설명 | 주요 액션 |
| --- | --- | --- | --- |
| 권한 요청 가능 | 카메라 권한이 필요해요 | 필름 효과를 입힌 프리뷰와 촬영을 위해 카메라 접근이 필요합니다. | 권한 허용 |
| 설정 이동 필요 | 설정에서 카메라 권한을 켜주세요 | 권한이 꺼져 있어 프리뷰를 시작할 수 없습니다. | 설정 열기 |

## 갤러리 목록 화면

### 구성

- `AppTopBar`
- 사진 그리드
- `PhotoThumbnail`
- `EmptyState`
- `ErrorState`
- `SkeletonBox`

### 배치

- 배경은 `color.background`를 사용한다.
- 상단 앱바 높이는 56dp다.
- 사진 그리드는 기본 3열이다.
- 그리드 간격은 `size.galleryGap`이다.
- 빈 상태는 화면 중앙에 배치한다.

## 사진 상세 화면

### 구성

- 전체 화면 사진
- `AppTopBar`
- 삭제 `CircleIconButton`
- `ConfirmDialog`
- `StatusToast`

### 배치

- 사진은 원본 비율을 유지하며 화면 안에 맞춘다.
- 배경은 `color.background`를 사용한다.
- 뒤로 가기는 상단 좌측, 삭제는 상단 우측에 둔다.
- 삭제 확인은 `ConfirmDialog`로 표시한다.

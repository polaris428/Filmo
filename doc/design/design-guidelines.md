# 디자인 가이드라인

## 디자인 컨셉

Filmo의 UI 컨셉은 `Quiet Film Camera`다. 실제 카메라 앱처럼 프리뷰가 화면의 중심이고, UI는 촬영을 방해하지 않는 얇은 조작 레이어로 존재한다. 필름 감성은 장식적인 배경이나 큰 그래픽이 아니라 프리뷰 색감, 썸네일, 촬영 피드백에서 드러난다.

## 키워드

- 실시간 카메라
- 필름 시뮬레이션
- 어두운 조작 레이어
- 낮은 장식성
- 빠른 촬영 흐름
- 아날로그 피드백

## 폰트 시스템

Android 기본 구현은 시스템 폰트를 사용한다.

| 역할 | Android 폰트 | Weight | Size | Line height | 용도 |
| --- | --- | --- | --- | --- | --- |
| Display | `Roboto` | Medium 500 | 28sp | 34sp | 빈 상태, 권한 안내 제목 |
| Title | `Roboto` | Medium 500 | 20sp | 26sp | 갤러리 제목, 다이얼로그 제목 |
| Body | `Roboto` | Regular 400 | 16sp | 24sp | 안내 문구, 다이얼로그 설명 |
| Label | `Roboto` | Medium 500 | 14sp | 20sp | 버튼 텍스트, 필름 이름 |
| Caption | `Roboto` | Regular 400 | 12sp | 16sp | 필름 설명, 보조 상태 |
| Micro | `Roboto` | Medium 500 | 11sp | 14sp | 썸네일 보조 라벨 |

## 타이포그래피 규칙

- 카메라 화면에서는 `Label`, `Caption`, `Micro` 중심으로 사용한다.
- 갤러리와 권한 안내 화면에서만 `Title`과 `Display`를 사용한다.
- 필름 이름은 한 줄로 표시하고 길면 말줄임 처리한다.
- 버튼 텍스트는 1줄을 넘기지 않는다.
- 자간은 기본값을 사용한다.

## 컬러 시스템

### Core

| 토큰 | 값 | 용도 |
| --- | --- | --- |
| `color.background` | `#0E0F0F` | 갤러리, 상세 화면 기본 배경 |
| `color.surface` | `#171817` | 다이얼로그, 권한 안내 패널 |
| `color.surfaceOverlay` | `#00000099` | 프리뷰 위 하단/상단 오버레이 |
| `color.surfaceOverlayLight` | `#00000066` | 아이콘 버튼 배경 |
| `color.divider` | `#FFFFFF1F` | 경계선, 구분선 |

### Text

| 토큰 | 값 | 용도 |
| --- | --- | --- |
| `color.textPrimary` | `#F4F1EA` | 주요 텍스트 |
| `color.textSecondary` | `#C8C0B4` | 설명 텍스트 |
| `color.textTertiary` | `#8F887F` | 비활성, 보조 라벨 |
| `color.textOnLight` | `#111111` | 밝은 버튼 위 텍스트 |

### Brand

| 토큰 | 값 | 용도 |
| --- | --- | --- |
| `color.primary` | `#E8D8B8` | 주요 액션, 선택 강조 |
| `color.primaryPressed` | `#D6C29C` | 주요 액션 pressed |
| `color.primaryMuted` | `#A9946E` | 약한 강조 |
| `color.focus` | `#F5E6C8` | 포커스 링 |

### Feedback

| 토큰 | 값 | 용도 |
| --- | --- | --- |
| `color.success` | `#7BBF8A` | 저장 성공 |
| `color.warning` | `#E2B75B` | 권한/주의 안내 |
| `color.error` | `#E35D5B` | 오류, 삭제 |
| `color.errorPressed` | `#C94A48` | 삭제 pressed |

### Film Accent

필름별 accent는 UI 전체 테마가 아니라 필름 선택 항목의 작은 표시와 썸네일 보정에만 사용한다.

| 필름 | 토큰 | 값 |
| --- | --- | --- |
| Kodak Portra 400 | `film.portra` | `#DDAA78` |
| Fuji Velvia 50 | `film.velvia` | `#5FA36A` |
| Ilford HP5 | `film.ilford` | `#B8B8B8` |
| Lomography | `film.lomography` | `#C24F6D` |
| Expired Film | `film.expired` | `#BFA15D` |

## 컬러 사용 규칙

- 카메라 화면의 UI 배경은 `surfaceOverlay` 계열을 기본으로 사용한다.
- 주요 촬영 액션은 `primary`를 사용한다.
- 선택된 필름은 `primary` 테두리와 필름 accent를 함께 사용한다.
- 삭제 액션은 반드시 `error`를 사용한다.
- 한 화면에서 필름 accent를 2개 이상 큰 면적으로 사용하지 않는다.

## 간격 시스템

| 토큰 | 값 | 용도 |
| --- | --- | --- |
| `space.2` | 2dp | 미세 간격 |
| `space.4` | 4dp | 아이콘/텍스트 내부 간격 |
| `space.8` | 8dp | 작은 요소 간격 |
| `space.12` | 12dp | 버튼 내부 padding |
| `space.16` | 16dp | 화면 기본 margin |
| `space.20` | 20dp | 하단 컨트롤 간격 |
| `space.24` | 24dp | 섹션 간격 |
| `space.32` | 32dp | 큰 빈 상태 간격 |

## 크기 시스템

| 토큰 | 값 | 용도 |
| --- | --- | --- |
| `size.touchTarget` | 48dp | 최소 터치 영역 |
| `size.iconButton` | 44dp | 카메라 상단 아이콘 버튼 |
| `size.icon` | 24dp | 기본 아이콘 |
| `size.shutterOuter` | 76dp | 셔터 외곽 |
| `size.shutterInner` | 58dp | 셔터 내부 |
| `size.filmThumb` | 56dp x 64dp | 필름 썸네일 |
| `size.galleryGap` | 2dp | 갤러리 그리드 간격 |

## Radius 시스템

| 토큰 | 값 | 용도 |
| --- | --- | --- |
| `radius.none` | 0dp | 프리뷰, 사진 |
| `radius.xs` | 4dp | 썸네일, 작은 배지 |
| `radius.sm` | 8dp | 필름 항목, 토스트 |
| `radius.md` | 12dp | 다이얼로그, 권한 안내 패널 |
| `radius.full` | 999dp | 아이콘 버튼, 셔터 버튼 |

## Elevation 및 Overlay

- 카메라 화면에서는 물리적인 그림자보다 반투명 오버레이를 우선한다.
- 상단 컨트롤 배경은 `surfaceOverlayLight`를 사용한다.
- 하단 필름 영역은 `surfaceOverlay`를 사용한다.
- 다이얼로그는 `surface` 배경과 24dp 바깥 여백을 가진다.

## 아이콘 시스템

Android Material Icons 또는 Compose Material Icons를 기준으로 한다.

| 액션 | 아이콘 |
| --- | --- |
| 카메라 전환 | `Cameraswitch` |
| 갤러리 | `PhotoLibrary` |
| 뒤로 가기 | `ArrowBack` |
| 삭제 | `Delete` |
| 설정 | `Settings` |
| 재시도 | `Refresh` |
| 선택됨 | `Check` |

## 모션 시스템

| 토큰 | 값 | 용도 |
| --- | --- | --- |
| `motion.fast` | 120ms | 버튼 pressed, 선택 표시 |
| `motion.normal` | 220ms | 필름 선택 전환, 토스트 |
| `motion.slow` | 420ms | 인화 애니메이션 |
| `motion.focusRing` | 700ms | 탭 포커스 링 |

## 화면별 적용

### 카메라 화면

- 배경은 카메라 프리뷰 자체다.
- 상단 컨트롤은 `iconButton`, `surfaceOverlayLight`, `textPrimary`를 사용한다.
- 하단 필름 선택 영역은 `surfaceOverlay`, `space.16`, `radius.md`를 사용한다.
- 셔터 버튼은 `shutterOuter`, `shutterInner`, `primary`를 사용한다.

### 권한 안내

- 배경은 `background` 또는 흐린 프리뷰 대체 화면을 사용한다.
- 패널은 `surface`, `radius.md`, `space.24`를 사용한다.
- 제목은 `Title`, 설명은 `Body`, 버튼은 `Label`을 사용한다.

### 갤러리

- 배경은 `background`를 사용한다.
- 그리드는 3열, 간격 `galleryGap`을 기본으로 한다.
- 상단 앱바는 `surface` 또는 `background`와 동일 톤을 사용한다.

### 사진 상세

- 배경은 `background`를 사용한다.
- 사진은 `radius.none`으로 원본 비율을 유지한다.
- 삭제 버튼은 `error`를 사용한다.

## 접근성 기준

- 텍스트와 배경 대비는 최소 4.5:1을 목표로 한다.
- 아이콘 버튼은 접근성 라벨을 가진다.
- 선택 상태는 색상만으로 표현하지 않는다.
- 촬영 중, 저장 성공, 저장 실패는 스크린 리더 상태 문구를 제공한다.

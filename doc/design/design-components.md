# 디자인 컴포넌트 명세

이 문서는 여러 화면에서 반복해서 사용할 재사용 UI 컴포넌트를 정의한다. 화면별 배치는 [화면 디자인 명세](./screen-designs.md)에서 다룬다.

## 공통 규칙

- 모든 컴포넌트는 [디자인 가이드라인](./design-guidelines.md)의 토큰을 사용한다.
- 클릭 가능한 컴포넌트는 최소 `size.touchTarget`을 확보한다.
- 아이콘만 있는 컴포넌트는 접근성 라벨을 필수로 가진다.
- 선택, 오류, 비활성 상태는 색상만으로 구분하지 않는다.
- 텍스트는 좁은 영역에서 잘리지 않도록 `maxLines`와 말줄임 기준을 가진다.

## PrimaryButton

주요 액션에 사용하는 버튼이다.

### 사용 예

- 권한 허용
- 설정 열기
- 재시도
- 카메라로 돌아가기

### 상태

| 상태 | 표현 |
| --- | --- |
| enabled | `color.primary` 배경, `color.textOnLight` 텍스트 |
| pressed | `color.primaryPressed` 배경 |
| loading | 텍스트 대신 작은 progress indicator |
| disabled | alpha 0.45, 클릭 불가 |

### 명세

| 속성 | 값 |
| --- | --- |
| height | 48dp |
| min width | 96dp |
| horizontal padding | `space.16` |
| radius | `radius.full` |
| typography | `Label` |

## SecondaryButton

취소, 보조 동작에 사용하는 버튼이다.

### 사용 예

- 나중에
- 취소
- 닫기

### 명세

| 속성 | 값 |
| --- | --- |
| height | 48dp |
| background | transparent |
| content color | `color.textPrimary` |
| pressed background | `color.surfaceOverlayLight` |
| typography | `Label` |
| radius | `radius.full` |

## DangerButton

삭제처럼 되돌리기 어려운 동작에 사용하는 버튼이다.

### 사용 예

- 사진 삭제

### 상태

| 상태 | 표현 |
| --- | --- |
| enabled | `color.error` 배경, `color.textPrimary` 텍스트 |
| pressed | `color.errorPressed` 배경 |
| loading | progress indicator |
| disabled | alpha 0.45 |

### 명세

| 속성 | 값 |
| --- | --- |
| height | 48dp |
| horizontal padding | `space.16` |
| radius | `radius.full` |
| typography | `Label` |

## CircleIconButton

프리뷰 위나 앱바에서 사용하는 원형 아이콘 버튼이다.

### 사용 예

- 카메라 전환
- 갤러리 열기
- 뒤로 가기
- 삭제
- 설정
- 재시도

### 상태

| 상태 | 표현 |
| --- | --- |
| enabled | 반투명 원형 배경 + 아이콘 |
| pressed | 배경 alpha 증가 |
| selected | `color.primary` 테두리 추가 |
| disabled | 아이콘 `color.textTertiary`, 클릭 불가 |

### 명세

| 속성 | 값 |
| --- | --- |
| size | `size.iconButton` |
| icon size | `size.icon` |
| background | `color.surfaceOverlayLight` |
| content color | `color.textPrimary` |
| selected border | `color.primary`, 1.5dp |
| radius | `radius.full` |

## OverlayPanel

카메라 프리뷰 위에 권한, 오류, 안내를 보여줄 때 사용하는 패널이다.

### 사용 예

- 권한 안내
- 프리뷰 오류
- 저장 실패 안내

### 구성

- title
- message
- primary action
- optional secondary action

### 명세

| 속성 | 값 |
| --- | --- |
| background | `color.surface` |
| title | `Title`, `color.textPrimary` |
| message | `Body`, `color.textSecondary` |
| padding | `space.24` |
| radius | `radius.md` |
| max width | screen width - `space.32` |

## AppTopBar

일반 화면 상단에 사용하는 앱바다.

### 사용 예

- 갤러리 목록
- 사진 상세

### 구성

- leading icon button
- title
- optional trailing icon button

### 명세

| 속성 | 값 |
| --- | --- |
| height | 56dp |
| horizontal padding | `space.16` |
| background | `color.background` |
| title | `Title`, `color.textPrimary` |
| icon | `CircleIconButton` 또는 투명 아이콘 버튼 |

## FilmChip

필름 하나를 표시하고 선택하는 항목이다.

### 구성

- 필름 썸네일
- 필름 이름
- 짧은 설명
- 선택 표시

### 상태

| 상태 | 표현 |
| --- | --- |
| default | 썸네일 + 이름 + 설명 |
| selected | `color.primary` 2dp 테두리 + 체크 아이콘 |
| applying | 선택 항목 위 작은 progress indicator |
| disabled | alpha 0.45 |

### 명세

| 속성 | 값 |
| --- | --- |
| width | 88dp |
| min height | 80dp |
| thumbnail size | `size.filmThumb` |
| radius | `radius.sm` |
| title | `Label`, `color.textPrimary`, maxLines 1 |
| description | `Micro`, `color.textSecondary`, maxLines 1 |
| selected border | `color.primary`, 2dp |

### 접근성

- default: “{필름 이름} 필름 선택”
- selected: “{필름 이름} 필름 선택됨”

## FilmStrip

`FilmChip`을 가로로 나열하는 재사용 목록 컴포넌트다.

### 사용 예

- 카메라 화면 하단 필름 선택
- 추후 필름 프리셋 선택 화면

### 명세

| 속성 | 값 |
| --- | --- |
| orientation | horizontal |
| item gap | `space.8` |
| content padding | horizontal `space.16`, vertical `space.12` |
| background | `color.surfaceOverlay` |
| radius | `radius.md` |

## ShutterButton

촬영 실행에 사용하는 특수 액션 버튼이다.

### 상태

| 상태 | 표현 |
| --- | --- |
| enabled | 흰 외곽 원 + `color.primary` 내부 원 |
| pressed | 내부 원 `color.primaryPressed`, scale 0.94 |
| capturing | 내부 progress indicator |
| disabled | alpha 0.45 |

### 명세

| 속성 | 값 |
| --- | --- |
| outer size | `size.shutterOuter` |
| inner size | `size.shutterInner` |
| outer ring | 4dp, `color.textPrimary` |
| inner color | `color.primary` |
| radius | `radius.full` |

### 접근성

- enabled: “사진 촬영”
- capturing: “사진 촬영 중”

## StatusToast

짧은 상태 메시지를 보여주는 토스트형 컴포넌트다.

### 사용 예

- 저장 중
- 저장 완료
- 저장 실패
- 필름 적용 실패

### 상태

| 상태 | 색 |
| --- | --- |
| neutral | `color.textSecondary` |
| success | `color.success` |
| warning | `color.warning` |
| error | `color.error` |

### 명세

| 속성 | 값 |
| --- | --- |
| background | `color.surface` |
| text | `Body`, `color.textPrimary` |
| icon size | 18dp |
| padding | vertical `space.12`, horizontal `space.16` |
| radius | `radius.sm` |
| max width | screen width - `space.32` |

## ConfirmDialog

사용자 확인이 필요한 작업에 사용하는 공통 다이얼로그다.

### 사용 예

- 사진 삭제 확인
- 저장 실패 후 재시도 확인

### 구성

- title
- message
- cancel action
- confirm action

### 명세

| 속성 | 값 |
| --- | --- |
| background | `color.surface` |
| radius | `radius.md` |
| padding | `space.24` |
| title | `Title`, `color.textPrimary` |
| message | `Body`, `color.textSecondary` |
| actions alignment | end |

## EmptyState

표시할 콘텐츠가 없을 때 사용하는 공통 컴포넌트다.

### 사용 예

- 촬영 사진 없음
- 검색 결과 없음

### 구성

- optional icon
- title
- message
- primary action

### 명세

| 속성 | 값 |
| --- | --- |
| title | `Display`, `color.textPrimary` |
| message | `Body`, `color.textSecondary` |
| spacing | `space.12` |
| horizontal padding | `space.32` |
| action | `PrimaryButton` |

## ErrorState

오류 상태를 표시하고 복구 액션을 제공하는 공통 컴포넌트다.

### 사용 예

- 카메라 초기화 실패
- 갤러리 로드 실패
- 사진 로드 실패

### 구성

- title
- message
- primary action

### 명세

| 속성 | 값 |
| --- | --- |
| title | `Title`, `color.textPrimary` |
| message | `Body`, `color.textSecondary` |
| action | `PrimaryButton` |
| spacing | `space.16` |

## LoadingIndicator

작은 로딩 상태를 표시하는 컴포넌트다.

### 사용 예

- 프리뷰 초기화
- 필름 적용 중
- 저장 중
- 삭제 중

### 명세

| 속성 | 값 |
| --- | --- |
| size small | 18dp |
| size medium | 28dp |
| stroke | 2dp |
| color | `color.primary` |

## SkeletonBox

이미지나 목록 로딩 전 공간을 유지하는 컴포넌트다.

### 사용 예

- 갤러리 썸네일 로딩
- 필름 썸네일 로딩

### 명세

| 속성 | 값 |
| --- | --- |
| background | `color.surface` |
| highlight | `color.divider` |
| radius | `radius.xs` |
| motion | `motion.normal` 반복 |

## PhotoThumbnail

사진 썸네일을 표시하는 이미지 컴포넌트다.

### 사용 예

- 갤러리 목록 항목
- 촬영 직후 결과 썸네일

### 상태

| 상태 | 표현 |
| --- | --- |
| loading | `SkeletonBox` |
| loaded | crop 이미지 |
| error | 오류 아이콘 플레이스홀더 |

### 명세

| 속성 | 값 |
| --- | --- |
| aspect ratio | 1:1 기본 |
| radius | `radius.none` 또는 `radius.xs` |
| error background | `color.surface` |
| error icon | `color.textTertiary` |

## FocusRing

탭 포커스 위치를 표시하는 일회성 피드백 컴포넌트다.

### 명세

| 속성 | 값 |
| --- | --- |
| size | 64dp |
| stroke | 2dp, `color.focus` |
| start scale | 1.15 |
| end scale | 1.0 |
| duration | `motion.focusRing` |

## CaptureFlash

촬영 순간 화면 플래시를 표현하는 오버레이 컴포넌트다.

### 명세

| 속성 | 값 |
| --- | --- |
| coverage | full screen |
| color | `#FFFFFF` |
| max alpha | 0.28 |
| duration | `motion.fast` |

## DevelopThumbnail

촬영 직후 인화 느낌을 주는 작은 결과 썸네일이다.

### 상태

| 상태 | 표현 |
| --- | --- |
| developing | 어두운 썸네일이 점점 밝아짐 |
| done | 갤러리 진입 위치로 축소 |
| failed | 사라지고 `StatusToast(error)` 표시 |

### 명세

| 속성 | 값 |
| --- | --- |
| size | 64dp |
| radius | `radius.xs` |
| start brightness | 0.25 |
| end brightness | 1.0 |
| duration | `motion.slow` |

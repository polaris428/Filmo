# Filmo 아키텍처 문서

## 프로젝트 개요

| 항목 | 내용 |
| --- | --- |
| 프로젝트명 | Filmo |
| 플랫폼 | Android, minSdk 26 |
| 아키텍처 | Feature-based Clean Architecture + MVI |
| 핵심 기술 | CameraX, OpenGL ES 2.0, GLSL |
| 서버 | 없음, 로컬 전용 |
| 예상 기간 | 4~5주 |

## 핵심 기술 스택

| 분류 | 기술 |
| --- | --- |
| 카메라 | CameraX Preview, ImageCapture, ImageAnalysis |
| 렌더링 | OpenGL ES 2.0, GLSurfaceView |
| 셰이더 | GLSL Fragment Shader |
| 이미지 저장 | MediaStore API |
| UI | Jetpack Compose |
| 아키텍처 | Feature-based Clean Architecture + MVI |
| 의존성 주입 | Hilt |

## 레이어 구조

```text
app/
├── core/
│   ├── camera/
│   │   ├── CameraController.kt        // CameraX 생명주기/렌즈/포커스 제어
│   │   └── CameraFrameSource.kt       // 프레임 스트림 전달
│   ├── renderer/
│   │   ├── FilmRenderer.kt            // GLSurfaceView.Renderer 구현
│   │   ├── ShaderProgram.kt           // 셰이더 컴파일/링크
│   │   ├── ShaderAssetLoader.kt       // shader asset 로드
│   │   └── shader/
│   │       ├── portra.frag
│   │       ├── velvia.frag
│   │       ├── ilford.frag
│   │       ├── lomography.frag
│   │       └── expired.frag
│   ├── media/
│   │   ├── MediaStorePhotoDataSource.kt // 기기 갤러리 저장/삭제
│   │   └── RenderedFrameCapture.kt      // 렌더링 결과 캡처
│   └── di/
│       ├── CameraModule.kt
│       ├── RendererModule.kt
│       └── RepositoryModule.kt
├── domain/
│   ├── model/
│   │   ├── FilmType.kt         // 필름 종류 enum
│   │   └── CapturedPhoto.kt    // 촬영된 사진 모델
│   ├── repository/
│   │   └── PhotoRepository.kt
│   └── usecase/
│       ├── GetFilmListUseCase.kt
│       ├── GetCapturedPhotosUseCase.kt
│       ├── SavePhotoUseCase.kt
│       └── DeletePhotoUseCase.kt
├── data/
│   ├── datasource/
│   │   └── PhotoLocalDataSource.kt
│   └── repository/
│       └── PhotoRepositoryImpl.kt
└── feature/
    ├── camera/
    │   ├── CameraScreen.kt
    │   ├── CameraViewModel.kt
    │   ├── CameraState.kt
    │   ├── CameraIntent.kt
    │   └── CameraEffect.kt
    └── gallery/
        ├── GalleryScreen.kt
        ├── GalleryViewModel.kt
        ├── GalleryState.kt
        ├── GalleryIntent.kt
        └── GalleryEffect.kt
```

## 레이어 책임

| 레이어 | 책임 |
| --- | --- |
| feature | Compose 화면, MVI 상태/이벤트/일회성 효과, 사용자 입력 처리 |
| domain | 앱 규칙, 필름/사진 모델, 유스케이스, repository 인터페이스 |
| data | domain repository 구현, 로컬 데이터 소스 조합 |
| core | CameraX, OpenGL, MediaStore, Hilt module 등 Android 플랫폼 어댑터 |

`feature`는 OpenGL, CameraX, MediaStore의 구체 구현을 직접 다루지 않는다. 화면과 ViewModel은 유스케이스 또는 core의 얇은 컨트롤러 인터페이스를 통해 필요한 동작만 요청한다.

## Feature 구조

```text
feature/
├── camera/
│   ├── CameraScreen.kt
│   ├── CameraViewModel.kt
│   ├── CameraState.kt
│   ├── CameraIntent.kt
│   └── CameraEffect.kt
└── gallery/
    ├── GalleryScreen.kt
    ├── GalleryViewModel.kt
    ├── GalleryState.kt
    ├── GalleryIntent.kt
    └── GalleryEffect.kt
```

- `State`: 화면을 그리는 데 필요한 단일 상태
- `Intent`: 사용자의 입력 또는 화면 이벤트
- `Effect`: 권한 요청, 토스트, 네비게이션처럼 한 번만 처리할 이벤트
- `ViewModel`: Intent를 받아 유스케이스를 실행하고 State/Effect를 갱신

## Core 구조

```text
core/
├── camera/
├── renderer/
├── media/
└── di/
```

- `camera`: CameraX 프리뷰, 렌즈 전환, 탭 포커스, 프레임 스트림 전달
- `renderer`: GLSurfaceView, OpenGL ES 2.0, ShaderProgram, 필름별 Fragment Shader 관리
- `media`: MediaStore 저장/삭제, 렌더링 결과 캡처
- `di`: Hilt 모듈 구성

## 의존성 방향

```text
feature -> domain <- data
feature -> core
data -> core
```

- `domain`은 Android 플랫폼 코드에 의존하지 않는다.
- `data`는 `domain`의 repository 인터페이스를 구현한다.
- `core`는 플랫폼 구현을 담되 앱 규칙을 갖지 않는다.
- `feature`는 화면 구현상 필요한 `core` 컨트롤러를 사용할 수 있지만, 저장/조회 규칙은 `domain` 유스케이스를 통해 처리한다.

## 렌더링 구조

CameraX의 프리뷰 또는 프레임 스트림을 OpenGL 렌더링 레이어로 전달하고, 선택된 필름 타입에 맞는 Fragment Shader를 적용한다.

- `core/camera`가 CameraX ImageAnalysis로 프레임 단위 스트림 처리
- `core/renderer`가 GLSurfaceView에서 실시간 프리뷰 렌더링
- `FilmRenderer`가 필름 타입에 맞는 셰이더 프로그램 선택
- `ShaderProgram`이 셰이더 컴파일과 링크 처리
- 필름별 색보정, 노이즈, 비네팅, 빛샘 효과를 Fragment Shader에서 처리

## 촬영 및 저장 구조

셔터 입력 시 현재 선택된 필름 효과가 적용된 결과물을 캡처하고 MediaStore API를 통해 기기 갤러리에 저장한다.

- `core/media`가 ImageCapture 또는 렌더링 결과 캡처로 촬영 이미지 생성
- `SavePhotoUseCase`가 저장 요청을 처리
- `PhotoRepositoryImpl`이 `PhotoLocalDataSource`를 통해 MediaStore API 연동
- 저장된 사진은 앱 내 갤러리에서도 조회 가능해야 함

## 개발 순서

| 주차 | 내용 |
| --- | --- |
| 1주차 | CameraX 프리뷰 + GLSurfaceView 연결 |
| 2주차 | GLSL 셰이더 구현, 필름 5종 |
| 3주차 | 실시간 렌더링 최적화 + 필름 선택 UI |
| 4주차 | 촬영 + 저장 파이프라인 + 갤러리 |
| 5주차 | 인화 애니메이션 + 버그 수정 + README |

## 포트폴리오 어필 포인트

- CameraX ImageAnalysis로 프레임 단위 스트림 처리
- GLSL Fragment Shader 직접 작성
- 필름별 색보정 행렬 직접 설계
- 실시간 렌더링 성능 최적화, 60fps 유지 목표
- GLSurfaceView + CameraX 연동 구조 설계

# Filmo 아키텍처 문서

## 프로젝트 개요

| 항목 | 내용 |
| --- | --- |
| 프로젝트명 | Filmo |
| 플랫폼 | Android, minSdk 24 |
| 아키텍처 | Feature-based Clean Architecture + MVI |
| 핵심 기술 | CameraX, OpenGL ES 2.0, GLSL |
| 서버 | 없음, 로컬 전용 |
| 예상 기간 | 4~5주 |

## 핵심 기술 스택

| 분류 | 기술 |
| --- | --- |
| 카메라 | CameraX Preview |
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
│   │   ├── CameraController.kt        // CameraX 프리뷰 제어 인터페이스
│   │   ├── CameraXController.kt       // CameraX Preview 생명주기 연결
│   │   └── CameraPermissionChecker.kt // 카메라 권한 확인
│   ├── renderer/
│   │   ├── FilmRenderer.kt            // GLSurfaceView.Renderer 구현
│   │   ├── FilmGLSurfaceView.kt       // OpenGL SurfaceView 래퍼
│   │   ├── CameraPreviewView.kt       // Compose와 GLSurfaceView 연결
│   │   ├── FilmShaderConfig.kt        // 필름별 shader asset/uniform 설정
│   │   ├── ShaderProgram.kt           // 셰이더 컴파일/링크
│   │   ├── ShaderAssetLoader.kt       // shader asset 로드
│   │   ├── TextureSurfaceSource.kt    // OES texture와 SurfaceTexture 생성
│   │   ├── RenderFrameCapturer.kt     // 렌더링 결과 캡처 확장 지점
│   │   └── CapturedFrame.kt           // 캡처 결과 모델
│   └── di/
│       └── AppModule.kt
├── data/
│   ├── datasource/
│   │   └── PhotoLocalDataSource.kt
│   └── repository/
│       └── PhotoRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── FilmType.kt         // 필름 종류 enum
│   │   └── CapturedPhoto.kt    // 저장된 사진 모델
│   ├── repository/
│   │   └── PhotoRepository.kt
│   └── usecase/
│       ├── GetFilmListUseCase.kt
│       └── SavePhotoUseCase.kt
└── feature/
    ├── camera/
    │   ├── CameraScreen.kt
    │   ├── CameraViewModel.kt
    │   ├── CameraState.kt
    │   ├── CameraIntent.kt
    │   └── CameraEffect.kt
```

`media`, `data`, 촬영/저장 유스케이스는 구현되었다. `gallery` feature와 사진 목록 조회/삭제 흐름은 갤러리 관련 플랜에서 추가한다.

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
└── camera/
    ├── CameraScreen.kt
    ├── CameraViewModel.kt
    ├── CameraState.kt
    ├── CameraIntent.kt
    └── CameraEffect.kt
```

`gallery` feature는 아직 구현 전이며 갤러리 관련 플랜에서 추가한다.

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

- `camera`: CameraX Preview 생명주기 연결, 카메라 권한 확인
- `renderer`: GLSurfaceView, OpenGL ES 2.0, OES texture, ShaderProgram, 필름별 Fragment Shader 관리, 프리뷰 비율 보정
- `media`: MediaStore 저장. 삭제는 사진 상세/삭제 플랜에서 추가
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

CameraX Preview의 `SurfaceProvider`를 `FilmRenderer`가 구현하고, CameraX 프리뷰 프레임을 OES texture로 받아 OpenGL 렌더링 레이어에서 표시한다. 선택된 필름 타입에 맞는 Fragment Shader를 적용한다.

- `core/camera`가 CameraX Preview를 생명주기에 bind
- `core/renderer`가 GLSurfaceView에서 실시간 프리뷰 렌더링
- `TextureSurfaceSource`가 CameraX에 제공할 `SurfaceTexture`/`Surface`를 생성
- `FilmRenderer`가 필름 타입에 맞는 셰이더 프로그램 선택
- `ShaderProgram`이 셰이더 컴파일과 링크 처리
- `camera.vert`의 `uPositionScale`로 카메라 원본 비율을 유지하며 화면 안에 맞춤
- 필름별 색보정, 노이즈, 비네팅, 빛샘 효과를 Fragment Shader에서 처리

## 촬영 및 저장 구조

셔터 입력 시 현재 선택된 필름 효과가 적용된 결과물을 캡처하고 MediaStore API를 통해 기기 갤러리에 저장한다. `FilmRenderer`가 현재 GL surface의 RGBA 픽셀을 `glReadPixels`로 읽고, `MediaStorePhotoDataSource`가 JPEG로 변환해 `Pictures/Filmo`에 저장한다.

- `core/renderer`가 `RenderFrameCapturer`를 통해 readPixels 기반 렌더링 결과 캡처를 제공
- `SavePhotoUseCase`가 저장 요청을 처리
- `PhotoRepositoryImpl`이 `PhotoLocalDataSource`를 통해 MediaStore API 연동
- 저장된 사진의 앱 내 목록 조회는 갤러리 플랜에서 `PhotoRepository` 조회 API를 확장해 연결

## 개발 순서

| 주차 | 내용 |
| --- | --- |
| 1주차 | CameraX 프리뷰 + GLSurfaceView 연결 |
| 2주차 | GLSL 셰이더 구현, 필름 5종 |
| 3주차 | 실시간 렌더링 최적화 + 필름 선택 UI |
| 4주차 | 촬영 + 저장 파이프라인 + 갤러리 |
| 5주차 | 인화 애니메이션 + 버그 수정 + README |

## 포트폴리오 어필 포인트

- CameraX Preview SurfaceProvider와 OpenGL OES texture 직접 연결
- GLSL Fragment Shader 직접 작성
- 필름별 색보정 행렬 직접 설계
- 실시간 렌더링 성능 최적화, 60fps 유지 목표
- GLSurfaceView + CameraX 연동 구조 설계
- 카메라 원본 비율 유지 렌더링


# Filmo 플랜 README

Filmo는 필름 카메라 느낌의 실시간 프리뷰와 촬영 결과물을 제공하는 Android 앱이다.

이 문서는 플랜의 단일 관리 지점이다. 기능 플랜 목록, 구현 순서, 현재 상태, 검증 내역은 여기에서만 갱신한다.

## 문서 구성

- [아키텍처 문서](./architecture.md): 프로젝트 구조, 기술 스택, 렌더링/저장 파이프라인, 개발 순서
- [기능 명세 문서](./feature-spec.md): 필름 종류, 화면별 기능, 촬영/갤러리 요구사항, 셰이더 효과
- [기능 플랜 폴더](./feature-plans): 사용자 액션별 세부 구현 플랜
- [디자인 문서](../design/README.md): 디자인 가이드라인, 화면 규칙, 컴포넌트 명세
- [베이스 개발 프롬프트](../prompt/base-development-prompt.md): 모든 작업에서 지켜야 할 공통 개발 규칙

## 프로젝트 개요

| 항목 | 내용 |
| --- | --- |
| 프로젝트명 | Filmo |
| 플랫폼 | Android, minSdk 24 |
| 아키텍처 | Feature-based Clean Architecture + MVI |
| 핵심 기술 | CameraX, OpenGL ES 2.0, GLSL |
| 서버 | 없음, 로컬 전용 |
| 예상 기간 | 4~5주 |

## 플랜 관리 방식

- 개별 플랜 파일명에는 순번을 붙이지 않고 기능명을 kebab-case로 작성한다.
- 구현 순서는 아래 플랜 목록을 위에서 아래로 읽는 방식으로만 관리한다.
- 다음 작업은 위에서부터 처음으로 완료되지 않은 플랜이다.
- 중간에 새 기능이 들어오면 새 플랜 파일을 추가하고 아래 표의 원하는 위치에 행만 삽입한다.
- 기존 플랜 파일명은 기능명이 바뀌지 않는 한 변경하지 않는다.

## 플랜 목록

| 액션 | 상태 | 문서 | 관련 도메인 | 비고 |
| --- | --- | --- | --- | --- |
| 프로젝트 기반 만들기 | 진행됨 | [build-project-foundation.md](./feature-plans/build-project-foundation.md) | 공통 기반 | Compose, Hilt, Navigation, CameraX 의존성 및 기본 앱 구조가 존재함 |
| 앱 진입 후 카메라 보기 | 완료 | [open-camera-preview.md](./feature-plans/open-camera-preview.md) | 카메라 프리뷰 | 카메라 화면 MVI, CameraX preview controller, `GLSurfaceView` 기반 preview 연결, 기본 필름 `Portra400` 적용 |
| 카메라 권한 처리 | 완료 | [handle-camera-permission.md](./feature-plans/handle-camera-permission.md) | 카메라 프리뷰 | 최초 진입 권한 요청, 권한 결과 처리, 거부/다시 묻지 않음 안내, 앱 설정 이동, 복귀 시 재확인 구현 |
| OpenGL 렌더링 파이프라인 만들기 | 완료 | [build-opengl-rendering-pipeline.md](./feature-plans/build-opengl-rendering-pipeline.md) | 카메라 프리뷰, 촬영 | `GLSurfaceView`, OES 텍스처, CameraX `SurfaceProvider`, vertex shader, 필름 fragment shader 연결, 원본 비율 유지 렌더링 적용 |
| 필름 GLSL 셰이더 구현하기 | 완료 | [implement-film-glsl-shaders.md](./feature-plans/implement-film-glsl-shaders.md) | 필름 선택, 촬영 | 필름 5종 fragment shader, 공통 GLSL 함수, shader asset 병합, 필름별 uniform config, 런타임 shader 교체 구현 |
| 필름 프리뷰 자산 만들기 | 완료 | [build-film-preview-assets.md](./feature-plans/build-film-preview-assets.md) | 필름 선택 | 필름 5종 drawable 썸네일, 필름 메타데이터, `FilmUiModel` 변환, 문자열/placeholder fallback 구현 |
| 필름 선택하기 | 완료 | [select-film.md](./feature-plans/select-film.md) | 필름 선택 | `GetFilmListUseCase`, `CameraState` 필름 목록/선택 상태, `OnSelectFilm`, 하단 `FilmStrip`/`FilmChip`, renderer 필름 전환 연결 |
| 카메라 조작하기 | 완료 | [control-camera.md](./feature-plans/control-camera.md) | 카메라 프리뷰 | 렌즈 방향 상태, 전/후면 전환, 탭 포커스, 포커스 링, 전환 중 중복 입력 방지 구현 |
| 셔터로 촬영하고 저장하기 | 완료 | [capture-and-save-photo.md](./feature-plans/capture-and-save-photo.md) | 촬영 | 셔터 버튼, GL 렌더링 결과 캡처, MediaStore `Pictures/Filmo` 저장, 저장 상태/재시도 UI 구현. 앱 내 목록 조회는 다음 갤러리 플랜에서 연결 |
| 촬영 사진 목록 보기 | 미시작 | [view-gallery-list.md](./feature-plans/view-gallery-list.md) | 갤러리 | 다음 구현 대상. 저장된 MediaStore 사진 목록 조회와 앱 내 갤러리 화면 연결 필요 |
| 사진 상세 보기와 삭제 | 미시작 | [view-and-delete-photo.md](./feature-plans/view-and-delete-photo.md) | 갤러리 | 갤러리 목록 이후 진행 |
| 성능, 라이프사이클, QA 정리 | 미시작 | [performance-lifecycle-qa.md](./feature-plans/performance-lifecycle-qa.md) | 공통 품질 | 주요 기능 구현 후 진행 |
| 문자열 리소스와 다국어 정리 | 부분 진행 | [localization-string-resources.md](./feature-plans/localization-string-resources.md) | 공통 기반 | 카메라 권한/프리뷰/기본 갤러리 라우트 관련 문자열은 `values`, `values-ko`에 추가됨 |

## 공통 작성 기준

- 각 플랜은 사용자 목표를 먼저 정의한다.
- 구현 범위는 이번 플랜에서 끝내야 하는 것과 후속 플랜으로 넘길 것을 구분할 수 있어야 한다.
- 파일 작업은 새로 만들거나 수정할 파일 경로와 역할을 명시한다.
- 상세 개발 절차는 작업자가 순서대로 따라 구현할 수 있을 정도로 구체적으로 작성한다.
- 상태/이벤트 계약은 MVI 상태, Intent, Effect, 주요 모델을 코드 형태로 명시한다.
- 화면이 필요한 플랜은 디자인 문서의 컴포넌트 이름을 기준으로 디자인 적용 방식을 명시한다.
- 검증 기준은 QA 또는 수동 테스트 기준으로 사용할 수 있어야 한다.

## 구현된 주요 파일

- `app/src/main/java/com/polaris/filmo/feature/camera/CameraScreen.kt`
- `app/src/main/java/com/polaris/filmo/feature/camera/CameraViewModel.kt`
- `app/src/main/java/com/polaris/filmo/feature/camera/CameraState.kt`
- `app/src/main/java/com/polaris/filmo/feature/camera/CameraIntent.kt`
- `app/src/main/java/com/polaris/filmo/feature/camera/CameraEffect.kt`
- `app/src/main/java/com/polaris/filmo/feature/camera/FilmUiModel.kt`
- `app/src/main/java/com/polaris/filmo/domain/model/CapturedPhoto.kt`
- `app/src/main/java/com/polaris/filmo/domain/repository/PhotoRepository.kt`
- `app/src/main/java/com/polaris/filmo/domain/usecase/GetFilmListUseCase.kt`
- `app/src/main/java/com/polaris/filmo/domain/usecase/SavePhotoUseCase.kt`
- `app/src/main/java/com/polaris/filmo/data/datasource/PhotoLocalDataSource.kt`
- `app/src/main/java/com/polaris/filmo/data/repository/PhotoRepositoryImpl.kt`
- `app/src/main/java/com/polaris/filmo/core/camera/CameraController.kt`
- `app/src/main/java/com/polaris/filmo/core/camera/CameraXController.kt`
- `app/src/main/java/com/polaris/filmo/core/camera/LensFacing.kt`
- `app/src/main/java/com/polaris/filmo/core/camera/CameraPermissionChecker.kt`
- `app/src/main/java/com/polaris/filmo/core/media/MediaStorePhotoDataSource.kt`
- `app/src/main/java/com/polaris/filmo/core/renderer/CameraPreviewView.kt`
- `app/src/main/java/com/polaris/filmo/core/renderer/FilmGLSurfaceView.kt`
- `app/src/main/java/com/polaris/filmo/core/renderer/FilmRenderer.kt`
- `app/src/main/java/com/polaris/filmo/core/renderer/FilmRendererController.kt`
- `app/src/main/java/com/polaris/filmo/core/renderer/FilmShaderConfig.kt`
- `app/src/main/java/com/polaris/filmo/core/renderer/ShaderProgram.kt`
- `app/src/main/java/com/polaris/filmo/core/renderer/ShaderAssetLoader.kt`
- `app/src/main/java/com/polaris/filmo/core/renderer/TextureSurfaceSource.kt`
- `app/src/main/java/com/polaris/filmo/core/renderer/RenderFrameCapturer.kt`
- `app/src/main/java/com/polaris/filmo/domain/model/FilmType.kt`
- `app/src/main/assets/shader/camera.vert`
- `app/src/main/assets/shader/base_camera.frag`
- `app/src/main/assets/shader/common_film.glsl`
- `app/src/main/assets/shader/portra.frag`
- `app/src/main/assets/shader/velvia.frag`
- `app/src/main/assets/shader/ilford.frag`
- `app/src/main/assets/shader/lomography.frag`
- `app/src/main/assets/shader/expired.frag`
- `app/src/main/res/drawable/film_preview_placeholder.xml`
- `app/src/main/res/drawable/film_portra_preview.xml`
- `app/src/main/res/drawable/film_velvia_preview.xml`
- `app/src/main/res/drawable/film_ilford_preview.xml`
- `app/src/main/res/drawable/film_lomography_preview.xml`
- `app/src/main/res/drawable/film_expired_preview.xml`

## 검증

- `:app:compileDebugKotlin` 성공
- 실제 기기/에뮬레이터에서 확인할 흐름:
  - 최초 실행 시 카메라 권한 요청 팝업 표시
  - 권한 허용 후 카메라 프리뷰 표시
  - 권한 거부 후 안내 패널 표시
  - 다시 묻지 않음 상태에서 설정 이동 버튼 표시
  - 설정에서 권한 허용 후 앱 복귀 시 프리뷰 시작
  - 카메라 프레임이 `GLSurfaceView`를 통해 실시간 표시
  - 카메라 프리뷰가 원본 비율을 유지하며 화면 안에 맞춰 표시
  - 기본 필름 `Portra400` shader가 프리뷰에 적용
  - renderer에 `FilmType` 변경 요청 시 필름별 shader program 교체
  - 필름 5종의 UI 모델이 이름, 설명, 썸네일, accent color를 제공
  - 썸네일 리소스 조회 실패 시 placeholder drawable 사용
  - 하단 필름 스트립에서 필름 5종 표시
  - 필름 선택 시 선택 상태와 프리뷰 shader 전환
  - 앱 pause/resume 후 GL preview 복구
  - 우상단 카메라 전환 버튼으로 전면/후면 카메라 전환
  - 카메라 전환 중 버튼 연속 입력 시 중복 세션 전환 방지
  - 프리뷰 탭 위치에 포커스 링 표시 및 CameraX 포커스 요청
  - 포커스 실패 시 앱 중단 없이 오류 메시지 표시
  - 셔터 버튼 클릭 시 현재 GL 렌더링 결과 캡처
  - 촬영/저장 중 셔터 연속 입력 시 중복 캡처/저장 방지
  - 캡처 결과가 JPEG로 변환되어 `Pictures/Filmo`에 저장
  - 저장 중/성공/실패 상태 UI 표시
  - 저장 실패 시 마지막 캡처 결과가 있으면 재시도 가능
  - 저장 결과가 현재 선택된 필름 효과를 포함

## 다음 작업

다음 구현 대상은 위 표에서 처음으로 완료되지 않은 [view-gallery-list.md](./feature-plans/view-gallery-list.md)이다. 현재 촬영 결과는 MediaStore `Pictures/Filmo`에 저장되고 `CapturedPhoto` 모델로 반환되지만, 앱 내 갤러리 목록 화면에서 저장된 사진을 조회하는 흐름은 아직 없다.

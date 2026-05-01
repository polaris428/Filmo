# Filmo 구현 진행 현황

이 문서는 `doc/plan/feature-plans`의 기능 플랜 기준으로 현재 구현 상태를 추적한다.

## 현재 진행 상태

| 번호 | 플랜 | 상태 | 비고 |
| --- | --- | --- | --- |
| 00 | 프로젝트 기반 만들기 | 진행됨 | Compose, Hilt, Navigation, CameraX 의존성 및 기본 앱 구조가 존재함 |
| 01 | 앱 진입 후 카메라 보기 | 완료 | 카메라 화면 MVI, CameraX preview controller, `GLSurfaceView` 기반 preview 연결, 기본 필름 `Portra400` 적용 |
| 02 | 카메라 권한 처리 | 완료 | 최초 진입 권한 요청, 권한 결과 처리, 거부/다시 묻지 않음 안내, 앱 설정 이동, 복귀 시 재확인 구현 |
| 03 | OpenGL 렌더링 파이프라인 만들기 | 완료 | `GLSurfaceView`, OES 텍스처, CameraX `SurfaceProvider`, vertex shader, 필름 fragment shader 연결, 원본 비율 유지 렌더링 적용 |
| 04 | 필름 GLSL 셰이더 구현하기 | 완료 | 필름 5종 fragment shader, 공통 GLSL 함수, shader asset 병합, 필름별 uniform config, 런타임 shader 교체 구현 |
| 05 | 필름 프리뷰 자산 만들기 | 미시작 | 다음 구현 대상 |
| 06 | 필름 선택하기 | 미시작 | `CameraState.selectedFilm`과 renderer 연동은 있으나, 사용자가 필름을 선택하는 UI/Intent는 아직 없음 |
| 07 | 카메라 조작하기 | 미시작 | 06 이후 진행 |
| 08 | 필터로 촬영하기 | 미시작 | `RenderFrameCapturer` 인터페이스는 있으나 FBO 캡처 구현은 아직 예약 상태 |
| 09 | 사진 저장하기 | 미시작 | 08 이후 진행 |
| 10 | 촬영 사진 목록 보기 | 미시작 | 09 이후 진행 |
| 11 | 사진 상세 보기와 삭제 | 미시작 | 10 이후 진행 |
| 12 | 성능, 라이프사이클, QA 정리 | 미시작 | 주요 기능 구현 후 진행 |
| 13 | 문자열 리소스와 다국어 정리 | 부분 진행 | 카메라 권한/프리뷰/기본 갤러리 라우트 관련 문자열은 `values`, `values-ko`에 추가됨 |

## 구현된 주요 파일

- `app/src/main/java/com/polaris/filmo/feature/camera/CameraScreen.kt`
- `app/src/main/java/com/polaris/filmo/feature/camera/CameraViewModel.kt`
- `app/src/main/java/com/polaris/filmo/feature/camera/CameraState.kt`
- `app/src/main/java/com/polaris/filmo/feature/camera/CameraIntent.kt`
- `app/src/main/java/com/polaris/filmo/feature/camera/CameraEffect.kt`
- `app/src/main/java/com/polaris/filmo/core/camera/CameraController.kt`
- `app/src/main/java/com/polaris/filmo/core/camera/CameraXController.kt`
- `app/src/main/java/com/polaris/filmo/core/camera/CameraPermissionChecker.kt`
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
  - 앱 pause/resume 후 GL preview 복구

## 다음 작업

다음 구현 대상은 `05-build-film-preview-assets.md`이다. 현재 필름 효과는 렌더러와 shader 레벨에 구현되어 있지만, 필름 선택 화면에서 사용할 프리뷰 자산과 실제 선택 UI는 아직 없다.

저장 이미지가 프리뷰와 같은 렌더링 경로를 사용해야 한다는 04번의 요구는 08번 `필터로 촬영하기`와 09번 `사진 저장하기`에서 FBO 캡처 및 저장 흐름을 구현할 때 이어서 검증해야 한다.

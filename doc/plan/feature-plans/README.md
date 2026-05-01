# 기능 단위 구현 플랜

이 폴더는 사용자 액션을 기준으로 구현 플랜을 정리한다. 하나의 기능이 크면 사용자가 실제로 수행하는 액션 단위로 나눈다.

## 작업 전 필수 문서

모든 기능 개발자는 개별 플랜을 보기 전에 [Filmo 베이스 개발 프롬프트](../../prompt/base-development-prompt.md)를 먼저 읽고 따라야 한다. Theme, 문자열 리소스, 다국어, MVI, 디자인 토큰 규칙은 개별 플랜보다 우선 적용한다.

## 플랜 목록

| 순서 | 액션 | 문서 | 관련 도메인 |
| --- | --- | --- | --- |
| 0 | 프로젝트 기반 만들기 | [00-build-project-foundation.md](./00-build-project-foundation.md) | 공통 기반 |
| 1 | 앱 진입 후 카메라 보기 | [01-open-camera-preview.md](./01-open-camera-preview.md) | 카메라 프리뷰 |
| 2 | 카메라 권한 처리 | [02-handle-camera-permission.md](./02-handle-camera-permission.md) | 카메라 프리뷰 |
| 3 | OpenGL 렌더링 파이프라인 만들기 | [03-build-opengl-rendering-pipeline.md](./03-build-opengl-rendering-pipeline.md) | 카메라 프리뷰, 촬영 |
| 4 | 필름 GLSL 셰이더 구현하기 | [04-implement-film-glsl-shaders.md](./04-implement-film-glsl-shaders.md) | 필름 선택, 촬영 |
| 5 | 필름 프리뷰 자산 만들기 | [05-build-film-preview-assets.md](./05-build-film-preview-assets.md) | 필름 선택 |
| 6 | 필름 선택하기 | [06-select-film.md](./06-select-film.md) | 필름 선택 |
| 7 | 카메라 조작하기 | [07-control-camera.md](./07-control-camera.md) | 카메라 프리뷰 |
| 8 | 셔터로 촬영하기 | [08-capture-photo.md](./08-capture-photo.md) | 촬영 |
| 9 | 사진 저장하기 | [09-save-photo.md](./09-save-photo.md) | 촬영 |
| 10 | 촬영 사진 목록 보기 | [10-view-gallery-list.md](./10-view-gallery-list.md) | 갤러리 |
| 11 | 사진 상세 보기와 삭제 | [11-view-and-delete-photo.md](./11-view-and-delete-photo.md) | 갤러리 |
| 12 | 성능, 라이프사이클, QA 정리 | [12-performance-lifecycle-qa.md](./12-performance-lifecycle-qa.md) | 공통 품질 |
| 13 | 문자열 리소스와 다국어 정리 | [13-localization-string-resources.md](./13-localization-string-resources.md) | 공통 기반 |

## 공통 작성 기준

- 각 플랜은 사용자 목표를 먼저 정의한다.
- 구현 범위는 이번 플랜에서 끝내야 하는 것과 후속 플랜으로 넘길 것을 구분할 수 있어야 한다.
- 파일 작업은 새로 만들거나 수정할 파일 경로와 역할을 명시한다.
- 상세 개발 절차는 작업자가 순서대로 따라 구현할 수 있을 정도로 구체적으로 작성한다.
- 상태/이벤트 계약은 MVI 상태, Intent, Effect, 주요 모델을 코드 형태로 명시한다.
- 화면이 필요한 플랜은 디자인 문서의 컴포넌트 이름을 기준으로 디자인 적용 방식을 명시한다.
- 검증 기준은 QA 또는 수동 테스트 기준으로 사용할 수 있어야 한다.

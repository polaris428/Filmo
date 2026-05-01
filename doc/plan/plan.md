# Filmo 문서 개요

Filmo는 필름 카메라 느낌의 실시간 프리뷰와 촬영 결과물을 제공하는 Android 앱이다.

## 문서 구성

- [아키텍처 문서](./architecture.md): 프로젝트 구조, 기술 스택, 렌더링/저장 파이프라인, 개발 순서
- [기능 명세 문서](./feature-spec.md): 필름 종류, 화면별 기능, 촬영/갤러리 요구사항, 셰이더 효과
- [플랜 README](./README.md): 기능 플랜 목록, 구현 상태, 다음 작업, 검증 내역
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


# 성능, 라이프사이클, QA 정리

## 목표

실시간 카메라 앱에서 흔히 발생하는 프레임 드랍, GL context 손실, 권한 복귀, 저장 실패, 화면 전환 문제를 최종적으로 점검한다.

## 구현 범위

- 카메라/GL lifecycle 처리
- 렌더링 성능 점검
- shader compile 실패 fallback
- 저장/갤러리 통합 검증
- 수동 QA 체크리스트 작성

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/core/renderer/FilmRenderer.kt` | FPS 로그, shader fallback 확인 |
| `app/core/camera/CameraController.kt` | pause/resume/release 처리 |
| `app/feature/camera/CameraScreen.kt` | lifecycle event 연결 |
| `app/feature/gallery/GalleryViewModel.kt` | 삭제/복귀 후 목록 refresh 확인 |
| `doc/qa/manual-checklist.md` | 수동 테스트 체크리스트 작성 |

## 상세 개발 절차

1. `CameraScreen`에서 lifecycle observer를 등록한다.
2. `ON_RESUME`에서는 권한 상태를 재확인하고 필요하면 프리뷰를 재시작한다.
3. `ON_PAUSE`에서는 카메라 세션을 멈추고 GL surface pause를 호출한다.
4. `ON_DESTROY`에서는 camera, surface, shader program, texture를 release한다.
5. shader compile 실패 시 기본 shader 또는 이전 shader로 fallback한다.
6. 60fps 목표를 기준으로 프레임 시간 로그를 debug build에서만 남긴다.
7. 필름 전환 시 1초 이상 UI thread가 멈추지 않는지 확인한다.
8. 촬영 후 저장, 갤러리 진입, 상세 보기, 삭제, 목록 복귀 흐름을 한 번에 검증한다.
9. 화면 회전, 홈 이동, 앱 복귀, 권한 설정 복귀 시나리오를 검증한다.
10. `doc/qa/manual-checklist.md`에 최종 체크리스트를 남긴다.

## 성능 기준

| 항목 | 기준 |
| --- | --- |
| 프리뷰 FPS | 가능하면 60fps, 최소 30fps 유지 |
| 필름 전환 | 1초 이상 정지 금지 |
| 촬영 중복 | 연속 터치에도 캡처/저장 1회 |
| shader 실패 | 앱 crash 없이 fallback |
| 앱 복귀 | 프리뷰 재시작 가능 |

## 검증 기준

- 홈으로 나갔다 돌아와도 프리뷰가 복구된다.
- 권한 설정에서 돌아온 뒤 상태가 갱신된다.
- shader compile 실패가 앱 crash로 이어지지 않는다.
- 필름 5종 전환 중 프리뷰가 검은 화면으로 고정되지 않는다.
- 촬영부터 삭제까지 전체 플로우가 한 번에 동작한다.


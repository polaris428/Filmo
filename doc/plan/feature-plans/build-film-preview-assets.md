# 필름 프리뷰 자산 만들기

## 목표

필름 선택 UI에서 사용자가 각 필름의 차이를 촬영 전에도 이해할 수 있도록 필름별 썸네일과 UI 표시 데이터를 만든다.

## 구현 범위

- 필름별 썸네일 asset
- `FilmType`과 썸네일 매핑
- 썸네일 로드 실패 fallback
- 필름 accent color와 설명 문구 정리

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/src/main/res/drawable/film_portra_preview.*` | Portra 썸네일 |
| `app/src/main/res/drawable/film_velvia_preview.*` | Velvia 썸네일 |
| `app/src/main/res/drawable/film_ilford_preview.*` | Ilford 썸네일 |
| `app/src/main/res/drawable/film_lomography_preview.*` | Lomography 썸네일 |
| `app/src/main/res/drawable/film_expired_preview.*` | Expired 썸네일 |
| `app/src/main/java/com/polaris/filmo/domain/model/FilmType.kt` | 썸네일 res name, 설명, accent token 연결 |
| `app/src/main/java/com/polaris/filmo/feature/camera/FilmUiModel.kt` | 썸네일 리소스 필드 정의 |

## 상세 개발 절차

1. 동일한 기준 이미지 하나를 준비한다.
   - 사람/하늘/녹색/그림자가 함께 들어간 샘플이 좋다.
   - 모든 필름 썸네일은 같은 원본을 사용한다.
2. `필름 GLSL 셰이더 구현하기` 플랜의 GLSL 파라미터를 기준으로 각 필름 느낌을 적용한 썸네일을 만든다.
3. 썸네일은 최소 112x128 이상으로 저장한다.
4. 파일명은 `film_{filmId}_preview` 형식을 따른다.
5. `FilmType`에 다음 필드를 추가한다.
   - `thumbnailResName`
   - `description`
   - `accentColorToken`
6. `FilmUiModel` 변환 시 drawable resource를 찾을 수 없으면 기본 placeholder를 사용한다.
7. 썸네일이 없는 상태에서도 필름 이름과 설명은 반드시 표시한다.

## 필름 표시 문구

| 필름 | 설명 | Accent |
| --- | --- | --- |
| Portra 400 | 따뜻한 인물 톤 | `film.portra` |
| Velvia 50 | 선명한 풍경 톤 | `film.velvia` |
| Ilford HP5 | 거친 흑백 톤 | `film.ilford` |
| Lomography | 강한 로모 톤 | `film.lomography` |
| Expired Film | 빛바랜 빈티지 톤 | `film.expired` |

## 디자인 적용

- 썸네일은 `PhotoThumbnail` 또는 `FilmChip` 내부 이미지 규칙을 따른다.
- 모든 썸네일은 같은 비율로 crop한다.
- 선택 상태는 썸네일 색감이 아니라 `FilmChip` 선택 표시로 구분한다.

## 검증 기준

- 필름 5종 모두 썸네일이 표시된다.
- 같은 원본 기준이라 필름별 색감 차이를 비교할 수 있다.
- 썸네일 로드 실패 시 앱이 깨지지 않는다.
- 필름 선택 UI에서 이름, 설명, 선택 상태가 모두 보인다.

## 구현 결과

- 필름 5종 썸네일은 동일한 vector 구도에 필름별 색감을 다르게 적용했다.
- `FilmType`은 `id`, fallback 표시 이름/설명, `thumbnailResName`, `accentColorToken`을 가진다.
- `FilmUiModel`은 리소스 문자열과 drawable을 runtime에 해석하고, 썸네일 조회 실패 시 `film_preview_placeholder`를 사용한다.
- 실제 선택 상태 표시는 `필름 선택하기` 플랜에서 구현한다.


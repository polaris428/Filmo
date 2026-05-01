# Filmo 기능 명세 문서

## 필름 종류 및 효과

| 필름 | 색감 | 노이즈 | 빛샘 | 특징 |
| --- | --- | --- | --- | --- |
| Kodak Portra 400 | 따뜻한 오렌지/베이지 | 중간 | 없음 | 인물 사진 느낌 |
| Fuji Velvia 50 | 채도 높은 그린/블루 | 낮음 | 없음 | 풍경 사진 느낌 |
| Ilford HP5 | 흑백, 높은 대비 | 높음 | 없음 | 클래식 흑백 |
| Lomography | 비네팅 강함, 색수차 | 높음 | 있음 | 로모 느낌 |
| Expired Film | 색바램, 불균일 | 매우 높음 | 있음 | 빈티지 느낌 |

## 카메라 프리뷰

- CameraX Preview를 GLSurfaceView에 연결한다.
- 실시간 셰이더를 적용해 프리뷰에서 필터 결과를 미리 볼 수 있어야 한다.
- 카메라 원본 비율을 유지해 프리뷰가 늘어나지 않아야 한다.
- 전면/후면 카메라 전환을 지원한다.
- 탭 포커스를 지원한다.

## 필름 선택

- 화면 하단에 가로 스크롤 필름 선택 UI를 제공한다.
- 필름 선택 시 프리뷰에 즉시 반영한다.
- 필름별 썸네일 미리보기를 제공한다.
- 필름 5종 선택 UI와 renderer shader 교체 경로가 구현되어 있다.

## 셔터 및 촬영

- 셔터를 누르면 현재 필터가 적용된 상태로 캡처한다.
- 필름 카메라 느낌의 진동과 플래시 피드백을 제공한다.
- 촬영 직후 저장 중/성공/실패 상태를 표시한다.
- 현재 GL 렌더링 결과를 캡처해 JPEG로 저장한다.
- 저장 위치는 기기 갤러리의 `Pictures/Filmo`다.
- 인화 애니메이션과 셔터음은 후속 UI 품질 정리에서 확장할 수 있다.

## 갤러리

- 앱 내에서 촬영한 사진 목록을 제공한다.
- 촬영 사진을 기기 갤러리에 저장한다.
- 사진 상세보기를 제공한다.
- 사진 삭제를 지원한다.
- 현재 기기 갤러리 저장은 구현되어 있고, 앱 내 목록/상세/삭제는 후속 갤러리 플랜에서 구현한다.

## GLSL 셰이더 명세

각 필름은 Fragment Shader 하나로 구현한다. 셰이더는 필름별로 필요한 효과를 조합해 처리한다.

### 색보정

RGB mixer 값으로 색을 변환한다. Portra 계열은 레드 채널을 올리고 블루 채널을 낮추는 방식으로 따뜻한 색감을 만든다.

```glsl
vec3 applyColorMatrix(vec3 color, vec3 redMix, vec3 greenMix, vec3 blueMix) {
    return vec3(
        dot(color, redMix),
        dot(color, greenMix),
        dot(color, blueMix)
    );
}
```

### 필름 노이즈

랜덤 노이즈를 생성해 필름 입자감을 표현한다.

```glsl
float noise(vec2 uv) {
    return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}
```

### 비네팅

가장자리를 어둡게 만들어 렌즈와 필름 특유의 분위기를 표현한다.

```glsl
float vignette(vec2 uv) {
    vec2 d = uv - 0.5;
    return 1.0 - dot(d, d) * 2.0;
}
```

### 빛샘

특정 영역에 오렌지/레드 계열 빛을 추가한다.

```glsl
vec3 lightLeak(vec2 uv, vec3 color) {
    float leak = smoothstep(0.8, 1.0, uv.x) * 0.4;
    return color + vec3(leak, leak * 0.3, 0.0);
}
```


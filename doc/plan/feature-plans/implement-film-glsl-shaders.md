# 필름 GLSL 셰이더 구현하기

## 목표

필름별 색감, 노이즈, 비네팅, 빛샘, 색수차를 GLSL Fragment Shader로 직접 구현해 Filmo의 핵심 필름 감성을 만든다.

## 구현 범위

- 필름 5종 Fragment Shader 작성
- 공통 GLSL 함수 설계
- 필름별 색보정 matrix와 파라미터 정의
- 노이즈, 비네팅, 빛샘, 색수차 구현
- 프리뷰와 저장 결과가 같은 셰이더를 사용하도록 정리

## 파일 작업

| 경로 | 작업 |
| --- | --- |
| `app/src/main/assets/shader/common_film.glsl` | 공통 함수 정의 |
| `app/src/main/assets/shader/portra.frag` | Kodak Portra 400 |
| `app/src/main/assets/shader/velvia.frag` | Fuji Velvia 50 |
| `app/src/main/assets/shader/ilford.frag` | Ilford HP5 |
| `app/src/main/assets/shader/lomography.frag` | Lomography |
| `app/src/main/assets/shader/expired.frag` | Expired Film |
| `app/src/main/java/com/polaris/filmo/core/renderer/FilmShaderConfig.kt` | 필름별 shader asset, uniform 기본값 정의 |
| `app/src/main/java/com/polaris/filmo/core/renderer/ShaderProgram.kt` | 공통 uniform 주입 API 보강 |

## 상세 개발 절차

1. 모든 필름 fragment shader는 동일한 입력 계약을 사용한다.
   - `uCameraTexture`
   - `uTime`
   - `uContrast`
   - `uSaturation`
   - `uNoiseStrength`
   - `uVignetteStrength`
   - `uLightLeakStrength`
   - `uChromaticAberrationStrength`
   - `vTexCoord`
2. `common_film.glsl`에 공통 함수를 정의한다.
   - `hashNoise(vec2 uv, float time)`
   - `applyColorMatrix(vec3 color, vec3 redMix, vec3 greenMix, vec3 blueMix)`
   - `adjustContrast(vec3 color, float contrast)`
   - `adjustSaturation(vec3 color, float saturation)`
   - `addFilmNoise(vec2 uv, vec3 color, float time, float strength)`
   - `applyVignette(vec2 uv, vec3 color, float strength)`
   - `applyLightLeak(vec2 uv, vec3 color, float strength)`
   - `chromaticAberrationOffset(vec2 uv, float amount)`
3. GLSL ES 2.0에서는 `#include`가 기본 지원되지 않으므로 `ShaderAssetLoader`가 `common_film.glsl` 내용을 fragment shader 앞에 붙여서 compile한다.
4. 각 필름 shader는 다음 순서로 색을 처리한다.
   - 카메라 텍스처 샘플링
   - 노출/대비 보정
   - 색보정 matrix 적용
   - 채도 조정
   - 필름 노이즈 추가
   - 비네팅 적용
   - 빛샘 또는 색수차 적용
   - `clamp(color, 0.0, 1.0)`
5. `FilmShaderConfig`에 필름별 파라미터를 정의한다.
6. `FilmRenderer.setFilm()`은 요청 필름을 갱신하고, GL frame에서 해당 config의 shader로 교체한 뒤 uniform 값을 주입한다.
7. `uTime`은 frame time 기준으로 계속 갱신해 노이즈가 완전히 고정되어 보이지 않게 한다.
8. 단, 노이즈가 지나치게 반짝이지 않도록 `floor(uTime * 12.0)`처럼 낮은 갱신 주기를 사용한다.
9. 저장 이미지도 프리뷰와 같은 렌더링 경로를 통과한 결과를 캡처한다.

## 공통 GLSL 함수 명세

```glsl
float hashNoise(vec2 uv, float seed) {
    return fract(sin(dot(uv + seed, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 applyColorMatrix(vec3 color, vec3 redMix, vec3 greenMix, vec3 blueMix) {
    return vec3(
        dot(color, redMix),
        dot(color, greenMix),
        dot(color, blueMix)
    );
}

vec3 adjustContrast(vec3 color, float contrast) {
    return (color - vec3(0.5)) * contrast + vec3(0.5);
}

vec3 adjustSaturation(vec3 color, float saturation) {
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    return mix(vec3(luma), color, saturation);
}

vec3 applyVignette(vec2 uv, vec3 color, float strength) {
    vec2 d = uv - vec2(0.5);
    float vig = smoothstep(0.85, 0.25, dot(d, d));
    return mix(color, color * vig, strength);
}

vec3 addFilmNoise(vec2 uv, vec3 color, float time, float strength) {
    float seed = floor(time * 12.0);
    float grain = hashNoise(uv * vec2(1280.0, 720.0), seed) - 0.5;
    return color + vec3(grain * strength);
}

vec3 applyLightLeak(vec2 uv, vec3 color, float strength) {
    float edgeLeak = smoothstep(0.58, 1.0, uv.x) * smoothstep(0.08, 0.76, uv.y);
    float topLeak = smoothstep(0.78, 1.0, uv.y) * smoothstep(0.10, 0.78, uv.x);
    vec3 warmLeak = vec3(1.0, 0.35, 0.08) * edgeLeak;
    vec3 amberLeak = vec3(1.0, 0.62, 0.22) * topLeak * 0.55;
    return color + (warmLeak + amberLeak) * strength;
}

vec2 chromaticAberrationOffset(vec2 uv, float amount) {
    vec2 direction = uv - vec2(0.5);
    return direction * amount;
}
```

## 필름별 파라미터

| 필름 | 대비 | 채도 | 노이즈 | 비네팅 | 빛샘 | 색수차 |
| --- | --- | --- | --- | --- | --- | --- |
| Portra 400 | 1.04 | 0.92 | 0.045 | 0.15 | 0.00 | 0.000 |
| Velvia 50 | 1.12 | 1.28 | 0.025 | 0.10 | 0.00 | 0.000 |
| Ilford HP5 | 1.22 | 0.00 | 0.085 | 0.22 | 0.00 | 0.000 |
| Lomography | 1.15 | 1.16 | 0.075 | 0.45 | 0.18 | 0.003 |
| Expired Film | 0.92 | 0.72 | 0.110 | 0.28 | 0.24 | 0.002 |

## 색보정 Matrix 기준

### Portra 400

따뜻한 피부톤을 위해 red를 올리고 blue를 낮춘다.

```glsl
color = applyColorMatrix(
    color,
    vec3(1.08, 0.04, 0.02),
    vec3(0.02, 1.00, 0.00),
    vec3(0.00, 0.02, 0.88)
);
```

### Velvia 50

풍경 느낌을 위해 green/blue를 강화한다.

```glsl
color = applyColorMatrix(
    color,
    vec3(0.96, 0.02, 0.00),
    vec3(0.00, 1.12, 0.04),
    vec3(0.02, 0.02, 1.10)
);
```

### Ilford HP5

흑백 변환 후 대비를 강하게 적용한다.

```glsl
float gray = dot(color, vec3(0.299, 0.587, 0.114));
color = vec3(gray);
```

### Lomography

과장된 색과 강한 주변부 어둡기를 적용한다.

```glsl
color = applyColorMatrix(
    color,
    vec3(1.15, 0.00, 0.04),
    vec3(0.04, 1.05, 0.00),
    vec3(0.00, 0.04, 1.18)
);
```

### Expired Film

색바램과 불균일한 녹색/노란색 편차를 만든다.

```glsl
color = applyColorMatrix(
    color,
    vec3(1.02, 0.04, 0.00),
    vec3(0.05, 0.96, 0.04),
    vec3(0.00, 0.02, 0.78)
);
```

## 필름별 구현 기준

### Portra 400

- 따뜻한 하이라이트
- 낮은 blue channel
- 부드러운 대비
- 중간 이하 노이즈
- 빛샘 없음

### Velvia 50

- 높은 채도
- green/blue 강화
- 낮은 노이즈
- 비네팅 약함
- 빛샘 없음

### Ilford HP5

- 완전 흑백
- 강한 대비
- 높은 노이즈
- 비네팅 중간
- 빛샘 없음

### Lomography

- 강한 비네팅
- 높은 채도
- red/blue 채널을 살짝 다른 uv에서 샘플링해 색수차 구현
- 오른쪽 또는 상단 가장자리에 약한 빛샘

### Expired Film

- 낮은 채도
- 색바램
- 시간/위치 기반 불균일 tint
- 높은 노이즈
- 불규칙한 빛샘

## 검증 기준

- 각 필름 shader가 compile/link 성공한다.
- 필름 5종의 색감 차이가 육안으로 명확히 구분된다.
- Portra와 Velvia는 빛샘이 없어야 한다.
- Ilford는 RGB 색이 남지 않는 흑백이어야 한다.
- Lomography와 Expired는 비네팅과 빛샘이 보여야 한다.
- 필름 선택 시 1초 이상 프리뷰가 멈추지 않는다.
- 저장 이미지와 프리뷰의 필름 효과가 동일한 렌더링 경로를 사용한다.


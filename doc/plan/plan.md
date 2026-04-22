필름 카메라 시뮬레이터 명세

프로젝트 개요
항목내용프로젝트명Filmo (가칭)플랫폼Android (minSdk 26)아키텍처Clean Architecture + MVI핵심 기술CameraX, OpenGL ES 2.0, GLSL서버없음 (로컬 전용)예상 기간4~5주

핵심 기술 스택
분류기술카메라CameraX (Preview, ImageCapture, ImageAnalysis)렌더링OpenGL ES 2.0, GLSurfaceView셰이더GLSL (Fragment Shader)이미지 저장MediaStore APIUIJetpack Compose아키텍처MVI + Clean Architecture의존성 주입Hilt

필름 종류 및 효과 명세
필름색감노이즈빛샘특징Kodak Portra 400따뜻한 오렌지/베이지중간없음인물 사진 느낌Fuji Velvia 50채도 높은 그린/블루낮음없음풍경 사진 느낌Ilford HP5흑백, 높은 대비높음없음클래식 흑백Lomography비네팅 강함, 색수차높음있음로모 느낌Expired Film색바램, 불균일매우 높음있음빈티지 느낌

기능 명세
1. 카메라 프리뷰

CameraX Preview를 GLSurfaceView에 연결
실시간 셰이더 적용 (프리뷰에서 필터 미리 보임)
전/후면 카메라 전환
탭 포커스

2. 필름 선택

하단 가로 스크롤 필름 선택 UI
필름 선택 시 프리뷰에 즉시 반영
필름별 썸네일 미리보기

3. 셔터 및 촬영

셔터 누르면 필터 적용된 상태로 캡처
필름 카메라 느낌의 셔터음 + 진동
촬영 직후 인화 애니메이션 (어둡다가 밝아지는 효과)

4. 갤러리

앱 내 촬영 사진 목록
기기 갤러리에 저장
사진 상세보기 + 삭제


GLSL 셰이더 명세
각 필름은 Fragment Shader 하나로 구현돼. 셰이더가 처리하는 효과 목록이야.
① 색보정 (Color Grading)
glsl// 색상 행렬로 RGB 값 변환
// Portra 예시: 레드 채널 올리고 블루 채널 낮추기
vec3 colorGrade(vec3 color) {
mat3 colorMatrix = mat3(
1.1, 0.0, 0.0,
0.05, 1.0, 0.0,
0.0, 0.0, 0.85
);
return colorMatrix * color;
}
② 필름 노이즈
glsl// 랜덤 노이즈 생성
float noise(vec2 uv) {
return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}
③ 비네팅
glsl// 가장자리 어둡게
float vignette(vec2 uv) {
vec2 d = uv - 0.5;
return 1.0 - dot(d, d) * 2.0;
}
④ 빛샘 (Light Leak)
glsl// 특정 영역에 오렌지/레드 빛 추가
vec3 lightLeak(vec2 uv, vec3 color) {
float leak = smoothstep(0.8, 1.0, uv.x) * 0.4;
return color + vec3(leak, leak * 0.3, 0.0);
}

레이어 구조
app/
├── domain/
│   ├── model/
│   │   ├── FilmType.kt         // 필름 종류 enum
│   │   └── CapturedPhoto.kt    // 촬영된 사진 모델
│   └── usecase/
│       ├── GetFilmListUseCase.kt
│       └── SavePhotoUseCase.kt
├── data/
│   └── repository/
│       └── PhotoRepositoryImpl.kt
├── presentation/
│   ├── camera/
│   │   ├── CameraScreen.kt
│   │   ├── CameraViewModel.kt
│   │   └── CameraState.kt
│   └── gallery/
│       ├── GalleryScreen.kt
│       └── GalleryViewModel.kt
└── gl/
├── FilmRenderer.kt         // GLSurfaceView.Renderer 구현
├── ShaderProgram.kt        // 셰이더 컴파일/링크
└── shader/
├── portra.frag
├── velvia.frag
├── ilford.frag
├── lomography.frag
└── expired.frag

개발 순서
주차내용1주차CameraX 프리뷰 + GLSurfaceView 연결2주차GLSL 셰이더 구현 (필름 5종)3주차실시간 렌더링 최적화 + 필름 선택 UI4주차촬영 + 저장 파이프라인 + 갤러리5주차인화 애니메이션 + 버그 수정 + README

포트폴리오 어필 포인트 요약

CameraX ImageAnalysis로 프레임 단위 스트림 처리
GLSL Fragment Shader 직접 작성 (라이브러리 미사용)
필름별 색보정 행렬 직접 설계
실시간 렌더링 성능 최적화 (60fps 유지)
GLSurfaceView + CameraX 연동 구조 설계
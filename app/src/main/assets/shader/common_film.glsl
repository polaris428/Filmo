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

vec3 addFilmNoise(vec2 uv, vec3 color, float time, float strength) {
    return color;
}

vec3 applyVignette(vec2 uv, vec3 color, float strength) {
    vec2 d = uv - vec2(0.5);
    float vig = smoothstep(0.85, 0.25, dot(d, d));
    return mix(color, color * vig, strength);
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

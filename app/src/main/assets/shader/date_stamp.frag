#extension GL_OES_EGL_image_external : require
precision highp float;

uniform samplerExternalOES uCameraTexture;
uniform float uTime;
uniform float uTemperature;
uniform float uTint;
uniform float uContrast;
uniform float uSaturation;
uniform float uNoiseStrength;
uniform float uVignetteStrength;
uniform float uLightLeakStrength;
uniform float uChromaticAberrationStrength;
uniform vec4 uDateDigitsA;
uniform vec4 uDateDigitsB;

varying vec2 vTexCoord;

float rect(vec2 p, vec2 center, vec2 halfSize) {
    vec2 d = abs(p - center) - halfSize;
    return 1.0 - step(0.0, max(d.x, d.y));
}

float segment(vec2 p, int id) {
    if (id == 0) return rect(p, vec2(0.50, 0.88), vec2(0.28, 0.055));
    if (id == 1) return rect(p, vec2(0.80, 0.66), vec2(0.055, 0.24));
    if (id == 2) return rect(p, vec2(0.80, 0.28), vec2(0.055, 0.24));
    if (id == 3) return rect(p, vec2(0.50, 0.08), vec2(0.28, 0.055));
    if (id == 4) return rect(p, vec2(0.20, 0.28), vec2(0.055, 0.24));
    if (id == 5) return rect(p, vec2(0.20, 0.66), vec2(0.055, 0.24));
    return rect(p, vec2(0.50, 0.48), vec2(0.26, 0.050));
}

float digitShape(vec2 p, float digitValue) {
    int d = int(digitValue + 0.5);
    float s = 0.0;
    if (d == 0) s = max(max(max(segment(p, 0), segment(p, 1)), max(segment(p, 2), segment(p, 3))), max(segment(p, 4), segment(p, 5)));
    if (d == 1) s = max(segment(p, 1), segment(p, 2));
    if (d == 2) s = max(max(max(segment(p, 0), segment(p, 1)), max(segment(p, 6), segment(p, 4))), segment(p, 3));
    if (d == 3) s = max(max(max(segment(p, 0), segment(p, 1)), max(segment(p, 6), segment(p, 2))), segment(p, 3));
    if (d == 4) s = max(max(segment(p, 5), segment(p, 6)), max(segment(p, 1), segment(p, 2)));
    if (d == 5) s = max(max(max(segment(p, 0), segment(p, 5)), max(segment(p, 6), segment(p, 2))), segment(p, 3));
    if (d == 6) s = max(max(max(segment(p, 0), segment(p, 5)), max(segment(p, 6), segment(p, 2))), max(segment(p, 3), segment(p, 4)));
    if (d == 7) s = max(max(segment(p, 0), segment(p, 1)), segment(p, 2));
    if (d == 8) s = max(max(max(segment(p, 0), segment(p, 1)), max(segment(p, 2), segment(p, 3))), max(max(segment(p, 4), segment(p, 5)), segment(p, 6)));
    if (d == 9) s = max(max(max(segment(p, 0), segment(p, 1)), max(segment(p, 2), segment(p, 3))), max(segment(p, 5), segment(p, 6)));
    return s;
}

float dotShape(vec2 p) {
    vec2 d = p - vec2(0.50, 0.08);
    return 1.0 - step(0.055, length(d));
}

float dateStampShape(vec2 uv) {
    vec2 stampUv = (uv - vec2(0.33, 0.065)) / vec2(0.58, 0.105);
    if (stampUv.x < 0.0 || stampUv.x > 1.0 || stampUv.y < 0.0 || stampUv.y > 1.0) return 0.0;

    float cell = 1.0 / 10.0;
    float mask = 0.0;
    for (int i = 0; i < 10; i++) {
        float left = float(i) * cell;
        float right = left + cell;
        if (stampUv.x >= left && stampUv.x < right) {
            vec2 p = vec2((stampUv.x - left) / cell, stampUv.y);
            if (i == 0) mask = digitShape(p, uDateDigitsA.x);
            if (i == 1) mask = digitShape(p, uDateDigitsA.y);
            if (i == 2) mask = digitShape(p, uDateDigitsA.z);
            if (i == 3) mask = digitShape(p, uDateDigitsA.w);
            if (i == 4) mask = dotShape(p);
            if (i == 5) mask = digitShape(p, uDateDigitsB.x);
            if (i == 6) mask = digitShape(p, uDateDigitsB.y);
            if (i == 7) mask = dotShape(p);
            if (i == 8) mask = digitShape(p, uDateDigitsB.z);
            if (i == 9) mask = digitShape(p, uDateDigitsB.w);
        }
    }
    float flicker = 0.90 + 0.10 * sin(uTime * 9.0 + stampUv.x * 20.0);
    return mask * flicker;
}

void main() {
    vec2 aberrationOffset = chromaticAberrationOffset(vTexCoord, uChromaticAberrationStrength);
    vec2 redUv = clamp(vTexCoord + aberrationOffset, vec2(0.0), vec2(1.0));
    vec2 blueUv = clamp(vTexCoord - aberrationOffset, vec2(0.0), vec2(1.0));
    vec3 color = vec3(
        texture2D(uCameraTexture, redUv).r,
        texture2D(uCameraTexture, vTexCoord).g,
        texture2D(uCameraTexture, blueUv).b
    );

    color = adjustContrast(color, uContrast);
    color = applyColorMatrix(
        color,
        vec3(1.06, 0.03, 0.01),
        vec3(0.02, 1.00, 0.01),
        vec3(0.00, 0.03, 0.86)
    );
    color = adjustTemperatureTint(color, uTemperature, uTint);
    color = adjustSaturation(color, uSaturation);
    color = addFilmNoise(vTexCoord, color, uTime, uNoiseStrength);
    color = applyVignette(vTexCoord, color, uVignetteStrength);

    float stamp = dateStampShape(vTexCoord);
    vec3 stampColor = vec3(1.0, 0.48, 0.05);
    color = mix(color, stampColor, stamp * 0.92);
    color += stampColor * stamp * 0.24;

    gl_FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}

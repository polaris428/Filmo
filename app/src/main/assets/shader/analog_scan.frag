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

varying vec2 vTexCoord;

float analogNoise(vec2 uv, float time) {
    return fract(sin(dot(vec2(uv.y * 173.0, time * 0.37), vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 wobbleUv = vTexCoord;
    float rowNoise = analogNoise(vec2(0.0, floor(vTexCoord.y * 180.0)), uTime);
    wobbleUv.x += (rowNoise - 0.5) * 0.006 * uNoiseStrength;

    vec2 aberrationOffset = chromaticAberrationOffset(wobbleUv, uChromaticAberrationStrength);
    vec2 redUv = clamp(wobbleUv + aberrationOffset, vec2(0.0), vec2(1.0));
    vec2 blueUv = clamp(wobbleUv - aberrationOffset, vec2(0.0), vec2(1.0));

    vec3 color = vec3(
        texture2D(uCameraTexture, redUv).r,
        texture2D(uCameraTexture, wobbleUv).g,
        texture2D(uCameraTexture, blueUv).b
    );

    color = adjustContrast(color, uContrast);
    color = applyColorMatrix(
        color,
        vec3(0.92, 0.04, 0.02),
        vec3(0.02, 1.06, 0.04),
        vec3(0.00, 0.04, 0.88)
    );
    color = adjustTemperatureTint(color, uTemperature, uTint);
    color = adjustSaturation(color, uSaturation);

    float fineScanline = sin(vTexCoord.y * 980.0) * 0.5 + 0.5;
    float scanlineDarken = 1.0 - fineScanline * 0.16;
    float rollingBar = smoothstep(0.08, 0.0, abs(fract(vTexCoord.y * 1.2 + uTime * 0.17) - 0.5));
    float dropout = step(0.985, analogNoise(vec2(vTexCoord.y, floor(uTime * 18.0)), uTime));

    color *= scanlineDarken;
    color += vec3(0.06, 0.10, 0.08) * rollingBar;
    color += vec3(dropout) * 0.16 * uNoiseStrength;
    color = addFilmNoise(vTexCoord, color, uTime, uNoiseStrength);
    color = applyVignette(vTexCoord, color, uVignetteStrength);
    color = applyLightLeak(vTexCoord, color, uLightLeakStrength);

    gl_FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}

#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES uCameraTexture;
uniform float uTime;
uniform float uContrast;
uniform float uSaturation;
uniform float uNoiseStrength;
uniform float uVignetteStrength;
uniform float uLightLeakStrength;
uniform float uChromaticAberrationStrength;

varying vec2 vTexCoord;

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
        vec3(1.15, 0.00, 0.04),
        vec3(0.04, 1.05, 0.00),
        vec3(0.00, 0.04, 1.18)
    );
    color = adjustSaturation(color, uSaturation);
    color = addFilmNoise(vTexCoord, color, uTime, uNoiseStrength);
    color = applyVignette(vTexCoord, color, uVignetteStrength);
    color = applyLightLeak(vTexCoord, color, uLightLeakStrength);
    gl_FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}

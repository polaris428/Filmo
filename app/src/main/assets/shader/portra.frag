#extension GL_OES_EGL_image_external : require
precision highp float;

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
    vec3 color = texture2D(uCameraTexture, vTexCoord).rgb;
    color = adjustContrast(color, uContrast);
    color = applyColorMatrix(
        color,
        vec3(1.08, 0.04, 0.02),
        vec3(0.02, 1.00, 0.00),
        vec3(0.00, 0.02, 0.88)
    );
    color = adjustSaturation(color, uSaturation);
    color = addFilmNoise(vTexCoord, color, uTime, uNoiseStrength);
    color = applyVignette(vTexCoord, color, uVignetteStrength);
    color = applyLightLeak(vTexCoord, color, uLightLeakStrength);
    gl_FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}

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
    float gray = dot(color, vec3(0.299, 0.587, 0.114));
    color = vec3(gray);
    color = adjustContrast(color, uContrast);
    color = adjustSaturation(color, uSaturation);
    color = addFilmNoise(vTexCoord, color, uTime, uNoiseStrength);
    color = applyVignette(vTexCoord, color, uVignetteStrength);
    color = applyLightLeak(vTexCoord, color, uLightLeakStrength);
    gl_FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}

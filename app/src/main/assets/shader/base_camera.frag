#extension GL_OES_EGL_image_external : require
precision highp float;

uniform samplerExternalOES uCameraTexture;
varying vec2 vTexCoord;

void main() {
    gl_FragColor = texture2D(uCameraTexture, vTexCoord);
}

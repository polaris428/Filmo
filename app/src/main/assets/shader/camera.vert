attribute vec4 aPosition;
attribute vec2 aTexCoord;

uniform mat4 uTextureTransform;
uniform vec2 uPositionScale;

varying vec2 vTexCoord;

void main() {
    gl_Position = vec4(aPosition.xy * uPositionScale, aPosition.z, aPosition.w);
    vec4 transformed = uTextureTransform * vec4(aTexCoord, 0.0, 1.0);
    vTexCoord = transformed.xy;
}

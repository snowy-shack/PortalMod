#version 130

in vec3 position;

varying vec2 uv;

void main() {
    gl_Position = vec4(position, 1.);
    uv = gl_MultiTexCoord0.xy;
}
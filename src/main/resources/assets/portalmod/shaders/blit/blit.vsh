#version 120

attribute vec3 position;
attribute vec2 uv;

varying vec2 coords;

void main() {
    coords = uv.xy;
    gl_Position = vec4(position, 1.);
}

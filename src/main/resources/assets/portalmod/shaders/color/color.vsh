#version 110

attribute vec3 position;
attribute vec2 uv;

void main() {
    gl_Position = vec4(position, 1.0);
}

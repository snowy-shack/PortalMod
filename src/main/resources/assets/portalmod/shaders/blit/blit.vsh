#version 120

attribute vec3 position;
varying vec2 coords;

void main() {
    coords = gl_MultiTexCoord0.xy;
    gl_Position = vec4(position, 1.);
}

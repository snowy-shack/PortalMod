#version 110

attribute vec4 position;
uniform mat4 modelViewProjection;
varying vec2 coords;

void main() {
    vec4 pos = (modelViewProjection * position);
    coords = pos.xy;
    gl_Position = pos;
}
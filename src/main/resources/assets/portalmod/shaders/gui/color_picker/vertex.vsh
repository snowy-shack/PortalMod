#version 120

attribute vec3 position;
attribute vec2 uv;

uniform mat4 modelView;
uniform mat4 projection;

varying vec2 coords;

void main() {
    vec4 pos = projection * modelView * vec4(position, 1);
    coords = uv.xy;
    gl_Position = pos;
}

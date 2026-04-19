#version 120

uniform mat4 modelView;
uniform mat4 projection;

attribute vec3 position;
varying vec2 coords;

void main() {
    vec4 pos = projection * modelView * vec4(position, 1);
    coords = gl_MultiTexCoord0.xy;
    gl_Position = pos;
}

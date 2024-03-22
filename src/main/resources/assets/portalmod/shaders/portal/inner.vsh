#version 110

attribute vec4 position;

uniform mat4 modelView;
uniform mat4 projection;

varying vec2 texCoord;

void main() {
    vec4 outPos = projection * modelView * position;
    gl_Position = outPos;
    texCoord = outPos.xy + 1.;
}
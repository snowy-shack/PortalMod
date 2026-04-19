#version 110

attribute vec4 position;

uniform mat4 modelViewProjection;
uniform int phase;

varying vec2 texCoord;

void main() {
    vec4 outPos = modelViewProjection * position;
    gl_Position = outPos;
    texCoord = 1. - gl_MultiTexCoord0.xy;
}
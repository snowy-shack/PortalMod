#version 110

attribute vec4 position;

uniform mat4 projection;

varying vec2 texCoord;

void main() {
    gl_Position = projection * position;
    texCoord = 1. - gl_MultiTexCoord0.xy;
}
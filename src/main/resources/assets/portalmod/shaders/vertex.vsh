#version 110

attribute vec4 Position;

uniform mat4 ModelMat;
uniform mat4 ProjMat;

uniform int phase;

varying vec2 texCoord;

void main() {
    vec4 outPos = ProjMat * ModelMat * Position;

    gl_Position = outPos;
    texCoord = gl_MultiTexCoord0.xy;
    texCoord.y = 1.0 - texCoord.y;
}
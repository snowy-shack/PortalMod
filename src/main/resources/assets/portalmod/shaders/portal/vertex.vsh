#version 130

attribute vec4 position;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform int clipPlaneEnabled;
uniform vec3 clipVec;
uniform vec3 clipPos;

varying vec2 texCoord;

void main() {
    vec4 worldPos = model * position;
    vec4 outPos = projection * (view * worldPos);
    texCoord = 1. - gl_MultiTexCoord0.xy;

    gl_Position = outPos;
    gl_ClipDistance[0] = (clipPlaneEnabled != 0) ? dot(worldPos.xyz - clipPos, clipVec) : 1.;
}
#version 120

attribute vec4 position;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform int clipPlaneEnabled;
uniform vec3 clipVec;
uniform vec3 clipPos;

varying vec2 texCoord;
varying float clipDistance;

void main() {
    vec4 worldPos = model * position;
    vec4 outPos = projection * (view * worldPos);
    texCoord = 1. - gl_MultiTexCoord0.xy;
    clipDistance = (clipPlaneEnabled != 0) ? dot(worldPos.xyz - clipPos, clipVec) : 1.;

    gl_Position = outPos;
}

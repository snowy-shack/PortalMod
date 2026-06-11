#version 120

attribute vec3 position;
attribute vec2 uv;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform int clipPlaneEnabled;
uniform vec3 clipVec;
uniform vec3 clipPos;

varying vec2 texCoord;
varying float clipDistance;

void main() {
    vec4 worldPos = model * vec4(position, 1.);
    vec4 outPos = projection * (view * worldPos);
    texCoord = 1. - uv.xy;
    clipDistance = (clipPlaneEnabled != 0) ? dot(worldPos.xyz - clipPos, clipVec) : 1.;

    gl_Position = outPos;
}

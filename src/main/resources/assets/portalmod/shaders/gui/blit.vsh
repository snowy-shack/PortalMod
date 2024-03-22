#version 130

uniform mat4 projection;

in vec3 position;
out vec2 coords;

void main() {
    vec4 pos = projection * vec4(position, 1.);
    coords = gl_MultiTexCoord0.xy;
    coords.y = 1. - coords.y;
    gl_Position = pos;
}
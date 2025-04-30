#version 130

in vec3 position;
out vec2 coords;

void main() {
    coords = gl_MultiTexCoord0.xy;
    gl_Position = vec4(position, 1.);
}
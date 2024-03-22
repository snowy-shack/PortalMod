#version 130

uniform sampler2D texture;

in vec2 coords;
out vec4 color;

void main() {
    color = texture2D(texture, coords);
    color.a = (1. - pow(abs(coords.x), 10.)) * (1. - pow(abs(coords.y), 10.));
}
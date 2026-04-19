#version 120

uniform sampler2D texture;

varying vec2 coords;

void main() {
    gl_FragColor = texture2D(texture, coords);
    gl_FragColor.a = (1. - pow(abs(coords.x), 10.)) * (1. - pow(abs(coords.y), 10.));
}

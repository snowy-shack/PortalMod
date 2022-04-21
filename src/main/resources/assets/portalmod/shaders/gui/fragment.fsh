#version 110

//uniform vec4 color;
varying vec2 coords;

void main() {
    float alpha = (1. - pow(abs(coords.x), 20.)) * (1. - pow(abs(coords.y), 20.));
    gl_FragColor = vec4(1, 1, 1, alpha);
}
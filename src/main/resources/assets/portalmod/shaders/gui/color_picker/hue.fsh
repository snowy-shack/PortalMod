#version 130

uniform float saturation;
uniform float value;

in vec2 coords;
out vec4 color;

vec3 hsv2rgb(float h, float s, float v);

void main() {
    float h = (1 - coords.y) * 360;
    color = vec4(hsv2rgb(h, saturation, value), 1);
}

vec3 hsv2rgb(float h, float s, float v) {
    float c = v * s;
    float x = c * (1 - abs(mod(h / 60, 2) - 1));
    float m = v - c;

    float rr = 0;
    float gg = 0;
    float bb = 0;

    if(h < 60) {
        rr = c;
        gg = x;
    } else if(h < 120) {
        rr = x;
        gg = c;
    } else if(h < 180) {
        gg = c;
        bb = x;
    } else if(h < 240) {
        gg = x;
        bb = c;
    } else if(h < 300) {
        bb = c;
        rr = x;
    } else {
        bb = x;
        rr = c;
    }

    return vec3(rr + m, gg + m, bb + m);
}
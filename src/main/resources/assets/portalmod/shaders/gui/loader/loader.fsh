#version 110

#define PI 3.14159265359
const vec3 bg     = vec3(51.  / 255., 54.  / 255., 62.  / 255.);
const vec3 orange = vec3(230. / 255., 127. / 255., 83.  / 255.);
const vec3 blue   = vec3(61.  / 255., 122. / 255., 219. / 255.);

uniform float millis;
uniform vec2 resolution;

varying vec2 uv;

void main() {
    float time = millis * 3. + 4.;
    float c = cos(time);
    float s = sin(time);

    vec2 coordCentered = vec2(ivec2((uv - .5) * resolution / 2.));
    float x = coordCentered.x * c - coordCentered.y * s;
    float y = coordCentered.x * s + coordCentered.y * c;

    float v = pow(x + y * s * .7, 2.) + pow(y - x * c * .7, 2.);
    float t = sin(millis * 3. - PI / 2. - .3) / 2. + .5;
    vec3 col = mix(blue, orange, t);

    if(v > 50. && v < 100.) {
        gl_FragColor = vec4(col, 1.0);
    } else {
        gl_FragColor = vec4(bg, 1.0);
    }
}
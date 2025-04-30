#version 130

#define PI 3.14159265359
const vec3 orange = vec3(1., .51, 0.23);
const vec3 blue = vec3(0.23, 0.54, 1.);

uniform float millis;
uniform ivec2 resolution;

out vec4 color;

const int newWidth = 1280;
ivec2 newResolution = ivec2(1280, float(resolution.y) / float(resolution.x) * 1280);

void main() {
    float time = millis * 3. + 4.;
    time += sin(time - .3) * .5;

    ivec2 coordCentered = ivec2((gl_FragCoord.xy - (resolution / 2.)) / resolution * newResolution / 5);
    float x = float(coordCentered.x);
    float y = float(coordCentered.y);
    float x2 = x * cos(time) - y * sin(time);
    float y2 = x * sin(time) + y * cos(time);
    x = x2;
    y = y2;

    float v = pow(x + y*sin(time)*.7, 2.) + pow(y - x*cos(time)*.7, 2.);

    float t = sin(millis * 3. - PI / 2. - .3) / 2. + .5;
    vec3 col = mix(blue, orange, t);

    if(v > 50. && v < 100.) {
        color = vec4(col, 1.0);
    } else {
        discard;
    }






//    ivec2 coordCentered = ivec2(coords - (resolution / 2.)) / 5;
//    float x = float(coordCentered.x);
//    float y = float(coordCentered.y);
//
//    float v = pow(x + y, 2.) + pow(y - x, 2.);
//
//    vec3 col = blue;
//
//    color = vec4(0.0);
//    if(v > 50. && v < 100.) {
//        color = vec4(col, 1.0);
//    }





//    color = vec4(vec3(gl_FragCoord.x * gl_FragCoord.y), 1.0);
}
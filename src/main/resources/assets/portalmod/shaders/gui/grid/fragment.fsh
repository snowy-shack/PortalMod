#version 110

uniform ivec2 res;
uniform int pitch;
uniform float a;
uniform float b;
uniform ivec2 offset;
varying vec2 coords;

float sdParabola(in vec2 pos, in float k) {
    pos.x = abs(pos.x);

    float ik = 1.0/k;
    float p = ik*(pos.y - 0.5*ik)/3.0;
    float q = 0.25*ik*ik*pos.x;

    float h = q*q - p*p*p;
    float r = sqrt(abs(h));

    float x = (h>0.0) ? pow(q+r,1.0/3.0) - pow(abs(q-r),1.0/3.0)*sign(r-q) : 2.0*cos(atan(r,q)/3.0)*sqrt(p);

    return length(pos-vec2(x,k*x*x)) * sign(pos.x-x);
}

void main() {
    gl_FragColor.a = (1. - pow(abs(coords.x), 10.)) * (1. - pow(abs(coords.y), 10.));
    vec2 cord = coords / 2. * vec2(res) - vec2(offset);

    if(floor(mod(cord.x, float(pitch))) == 0. || floor(mod(1. - cord.y, float(pitch))) == 0.)
        gl_FragColor.xyz = vec3(.3);
    else
        gl_FragColor.xyz = vec3(0.);

    if(floor(1. - cord.y) == 0.)
        gl_FragColor.xyz = vec3(.5, .2, .2);
    else if(floor(cord.x) == 0.)
        gl_FragColor.xyz = vec3(.2, .5, .2);

    if(cord.x > 1. || (cord.x > 0. && cord.y > 0.)) {
        float d = sdParabola(coords - vec2(-b / a / vec2(res).x * float(pitch), -b*b / a / vec2(res).y / 2. * float(pitch)) - vec2(offset) / vec2(res).xy * 2., a * vec2(res).x / float(pitch) * .89);
        gl_FragColor.xyz = mix(gl_FragColor.xyz, vec3(1.), 1. - smoothstep(0., .01, abs(d)));
    }
}
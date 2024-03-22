#version 110

uniform ivec2 res;
uniform int pitch;
uniform float a;
uniform float b;
uniform float height;
uniform float middle;
uniform float target;
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

//float dLine(vec2 pt1, vec2 pt2, vec2 pt3) {
//   vec2 v1 = pt2 - pt1;
//   vec2 v2 = pt1 - pt3;
//   vec2 v3 = vec2(v1.y, -v1.x);
//   return abs(dot(v2, normalize(v3)));
//}

float dLine( in vec2 p, vec2 a, vec2 b ) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp( dot(pa,ba)/dot(ba,ba), 0., 1. );
    return length( pa - ba*h );
}

void main() {
    gl_FragColor.a = (1. - pow(abs(coords.x), 10.)) * (1. - pow(abs(coords.y), 10.));
    vec2 cord = coords / 2. * vec2(res) - vec2(offset);
    
    if(floor(mod(cord.x, float(pitch))) == 0. || floor(mod(1. - cord.y, float(pitch))) == 0.)
        gl_FragColor.xyz = vec3(.2);
    else
        gl_FragColor.xyz = vec3(0.);

    if(floor(1. - cord.y) == 0.)
        gl_FragColor.xyz = vec3(.5, .2, .2);
    else if(floor(cord.x) == 0.)
        gl_FragColor.xyz = vec3(.2, .5, .2);
    
    //if(distance < 4.7)
        //return;

    //float x = cord.x;
    //float y = a * x * x + b * x;
    //float arcLength = sqrt(x * x + 16. * y * y) / 2. + x * x / 8. * y * log((4. * y + sqrt(x * x + 16. * y * y)) / x);

    if(a == a) {
        if(cord.x > 1. || (cord.x > 0. && cord.y > 0.)) {
            float d = sdParabola(coords - vec2(-b / a / vec2(res).x * float(pitch), -b*b / a / vec2(res).y / 2. * float(pitch)) - vec2(offset) / vec2(res).xy * 2., a * vec2(res).x / float(pitch) * .89);
            gl_FragColor.xyz = mix(gl_FragColor.xyz, (cord.x / float(pitch) < target) ? vec3(1.) : vec3(.5), 1. - smoothstep(0., .01, abs(d)));
        }

        float distance = length(cord - float(pitch) * vec2(middle, a * middle * middle + b * middle));
        gl_FragColor.xyz = mix(gl_FragColor.xyz, vec3(1., 1., 0.), smoothstep(0., 1., float(pitch) / 4. - distance));
    } else {
        float d = dLine(coords - vec2(offset) / vec2(res).xy * 2., vec2(0.), vec2(0., height) / vec2(res.xy) * 2. * float(pitch));
        gl_FragColor.xyz = mix(gl_FragColor.xyz, vec3(1.), 1. - smoothstep(0., .01, abs(d)));
        
        float distance = length(cord - float(pitch) * vec2(middle, height));
        gl_FragColor.xyz = mix(gl_FragColor.xyz, vec3(1., 1., 0.), smoothstep(0., 1., float(pitch) / 4. - distance));
    }
}
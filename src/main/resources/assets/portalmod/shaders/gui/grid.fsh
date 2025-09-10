#version 110

uniform ivec2 res;
uniform int pitch;
uniform float a;
uniform float b;
uniform float height;
uniform float middle;
uniform float target;
uniform ivec2 offset;
uniform sampler2D atlas;
uniform vec2 atlasSize;
varying vec2 coords;
const vec4 yellowSpr = vec4(0,  179, 12, 191);
const vec4 targetSpr = vec4(13, 179, 23, 189);

float sdParabola(in vec2 pos, in float k) {
    pos.x = abs(pos.x);

    float ik = 1.0 / k;
    float p = ik * (pos.y - 0.5 * ik) / 3.0;
    float q = 0.25 * ik * ik * pos.x;

    float h = q*q - p*p*p;
    float r = sqrt(abs(h));

    float x = (h > 0.0)
            ? pow(q + r, 1.0/3.0) - pow(abs(q - r), 1.0/3.0) * sign(r - q)
            : 2.0 * cos(atan(r, q) / 3.0) * sqrt(p);

    return length(pos - vec2(x, k * x * x)) * sign(pos.x - x);
}

float dLine( in vec2 p, vec2 a, vec2 b ) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp( dot(pa,ba) / dot(ba,ba), 0., 1. );
    return length( pa - ba*h );
}

vec3 drawSprite(vec2 screenPos, vec4 sprRect, vec2 center, vec3 baseColor) {
    vec2 sizePx = sprRect.zw - sprRect.xy;

    if (sizePx.x <= 0.0 || sizePx.y <= 0.0) return baseColor;

    vec2 uv0 = sprRect.xy / atlasSize;
    vec2 uv1 = sprRect.zw / atlasSize;

    vec2 uvLocal = (screenPos - center) / sizePx + vec2(0.5, 0.5);

    if (uvLocal.x < 0.0 || uvLocal.x > 1.0 || uvLocal.y < 0.0 || uvLocal.y > 1.0) return baseColor;

    float u = mix(uv0.x, uv1.x,       uvLocal.x);
    float v = mix(uv0.y, uv1.y, 1.0 - uvLocal.y);
    vec2 uv = vec2(u, v);

    vec4 color = texture2D(atlas, uv);
    return mix(baseColor, color.rgb, color.a);
}

void main() {
    gl_FragColor.a = (1.0 - pow(abs(coords.x), 10.0)) * (1.0 - pow(abs(coords.y), 10.0));

    vec2 cord = coords / 2.0 * vec2(res) - vec2(offset);

    if (floor(mod(cord.x, float(pitch))) == 0. || floor(mod(1.0 - cord.y, float(pitch))) == 0.)
        gl_FragColor.xyz = vec3(.2);
    else
        gl_FragColor.xyz = vec3(0.0);

    if (floor(1.0 - cord.y) == 0.0)
        gl_FragColor.xyz = vec3(.5, .2, .2);
    else if (floor(cord.x) == 0.0)
        gl_FragColor.xyz = vec3(.2, .5, .2);

    if (a == a) { // if a == NaN
        if (cord.x > 1.0 || (cord.x > 0.0 && cord.y > 0.0)) {
            float d = sdParabola(coords - vec2(-b / a / vec2(res).x * float(pitch),
            -b*b / a / vec2(res).y / 2.0 * float(pitch))
            - vec2(offset) / vec2(res).xy * 2.0,
            a * vec2(res).x / float(pitch) * 0.89);
            gl_FragColor.xyz = mix(gl_FragColor.xyz,
            (cord.x / float(pitch) < target) ? vec3(1.0) : vec3(0.5),
            1.0 - smoothstep(0.0, 0.01, abs(d)));
        }

        vec2 yellowCenter = float(pitch) * vec2(middle, a * middle * middle + b * middle);
        gl_FragColor.rgb = drawSprite(cord, yellowSpr, yellowCenter, gl_FragColor.rgb);

        vec2 targetCenter = float(pitch) * vec2(target, a * target * target + b * target);
        gl_FragColor.rgb = drawSprite(cord, targetSpr, targetCenter, gl_FragColor.rgb);

    } else {
        float d = dLine(coords - vec2(offset) / vec2(res).xy * 2.0,
        vec2(0.0), vec2(0.0, height) / vec2(res.xy) * 2.0 * float(pitch));
        gl_FragColor.xyz = mix(gl_FragColor.xyz, vec3(1.0), 1.0 - smoothstep(0.0, 0.01, abs(d)));

        vec2 yellowCenter = float(pitch) * vec2(middle, height);
        gl_FragColor.rgb = drawSprite(cord, yellowSpr, yellowCenter, gl_FragColor.rgb);

        vec2 targetCenter = float(pitch) * vec2(target, height);
        gl_FragColor.rgb = drawSprite(cord, targetSpr, targetCenter, gl_FragColor.rgb);
    }
}
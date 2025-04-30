#version 110

uniform sampler2D mask;
uniform vec4 color;
uniform int phase;

varying vec2 texCoord;

void main() {
    gl_FragColor = vec4(color.rgb, 1.);
    gl_FragDepth = gl_FragCoord.z - 0.00001 * gl_FragCoord.w;

    if(texture2D(mask, texCoord).rgb == vec3(0.))
        discard;
}
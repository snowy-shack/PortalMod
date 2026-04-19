#version 110

uniform sampler2D texture;
uniform float intensity;

varying vec2 texCoord;
varying float clipDistance;

void main() {
    if(clipDistance < 0.)
        discard;

    gl_FragDepth = gl_FragCoord.z - 0.00001 * gl_FragCoord.w;
    gl_FragColor = texture2D(texture, texCoord);

    if(gl_FrontFacing)
        gl_FragColor.a *= smoothstep(0., 1., (intensity - 2.) / 2.);
}

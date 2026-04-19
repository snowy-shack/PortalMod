#version 110

uniform sampler2D texture;

varying vec2 texCoord;
varying float clipDistance;

void main() {
    if(clipDistance < 0.)
        discard;

    gl_FragColor = texture2D(texture, texCoord);

    if(gl_FragCoord.z < .95) {
        gl_FragDepth = gl_FragCoord.z * 0.99;
    } else if(gl_FragCoord.z < .99) {
        gl_FragDepth = gl_FragCoord.z - 0.0001;
    } else {
        gl_FragDepth = gl_FragCoord.z;
    }
}

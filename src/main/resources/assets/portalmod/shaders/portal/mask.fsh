#version 110

uniform sampler2D texture;

varying vec2 texCoord;

void main() {
    gl_FragColor = texture2D(texture, texCoord);

    if(gl_FragCoord.z < .95) {
        gl_FragDepth = gl_FragCoord.z - 0.1;
    } else if(gl_FragCoord.z < .99) {
        gl_FragDepth = max(gl_FragCoord.z - 0.01 * gl_FragCoord.w, 0.);
    } else {
        gl_FragDepth = gl_FragCoord.z;
    }
}
#version 110

uniform sampler2D texture;
uniform vec4 color;
uniform int phase;

varying vec2 texCoord;

void main() {
//    if(phase == 0 && texture2D(texture, texCoord).rgb == vec3(1, 1, 1))
//        discard;
//    gl_FragColor = vec4(0, 1, texCoord.x, 1);
    gl_FragColor = vec4(color.xyz, 1);

    if(phase == 0) {
        if(texture2D(texture, texCoord).rgb == vec3(1, 1, 1))
            discard;
        gl_FragDepth = gl_FragCoord.z;
//        gl_FragColor = texture2D(texture, texCoord);
    } else if(phase == 1) {
        gl_FragDepth = 1.0;
    } else if(phase == 2) {
        gl_FragDepth = gl_FragCoord.z;
    } else if(phase == 3) {
        gl_FragDepth = gl_FragCoord.z - .0001;
    } else if(phase == 4) {
        gl_FragDepth = 0.0;
        gl_FragColor = texture2D(texture, texCoord);
//        gl_FragColor.a *= .8;
    } else if(phase == 5) {
        gl_FragDepth = gl_FragCoord.z;
        gl_FragColor = texture2D(texture, texCoord);
    }
}

//if(phase == 0) {
//    if(texture2D(texture, texCoord).rgb == vec3(1, 1, 1))
//    discard;
//    gl_FragDepth = gl_FragCoord.z;
//    gl_FragColor = vec4(0);
//} else if(phase == 1) {
//    gl_FragDepth = 1.0;
//    gl_FragColor = vec4(color.xyz, 1);
//} else if(phase == 2) {
//    gl_FragDepth = gl_FragCoord.z;
//    gl_FragColor = vec4(0);
//} else if(phase == 3) {
//    gl_FragDepth = 0.0;
//    gl_FragColor = texture2D(texture, texCoord);
//}
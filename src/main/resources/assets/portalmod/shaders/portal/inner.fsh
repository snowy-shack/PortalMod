#version 110

uniform sampler2D depthTexture;
uniform ivec2 resolution;

varying vec2 texCoord;

void main() {
//    if(texture2D(depthTexture, gl_FragCoord.xy / vec2(resolution)).r < gl_FragCoord.z - 0.00001)
//        discard;
    gl_FragDepth = gl_FragCoord.z - 0.00001;
//    gl_FragDepth = 1.;
    gl_FragColor = vec4(0, 1, 0, 1);

//    gl_FragColor = texture2D(depthTexture, gl_FragCoord.xy / vec2(resolution));
//    gl_FragColor = vec4(vec3(texCoord.x * texCoord.y), 1.);
//    gl_FragColor = vec4(vec3(texCoord.x > .5 && texCoord.y > .5 ? 1. : 0.), 1.);
}
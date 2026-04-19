#version 110

uniform sampler2D texture;
uniform int frameCount;
uniform int frameIndex;

varying vec2 texCoord;
varying float clipDistance;

void main() {
    if(clipDistance < 0.)
        discard;

    vec2 uv = texCoord;
    uv.y /= float(frameCount);
    uv.x = 1. - uv.x;

    uv.y += float(frameIndex) / float(frameCount);
    gl_FragColor = texture2D(texture, uv);
}

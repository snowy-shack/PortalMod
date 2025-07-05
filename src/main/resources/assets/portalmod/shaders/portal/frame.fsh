#version 110

uniform sampler2D texture;
uniform int frameCount;
uniform float frameIndex;

varying vec2 texCoord;

void main() {
    int currentFrame = int(frameIndex);
    int nextFrame = currentFrame + 1;
    float interpolation = fract(frameIndex);

    vec2 currentFrameCoord = texCoord;
    currentFrameCoord.y /= float(frameCount);
    currentFrameCoord.x = 1. - currentFrameCoord.x;
    vec2 nextFrameCoord = currentFrameCoord;

    currentFrameCoord.y += float(currentFrame) / float(frameCount);
    nextFrameCoord.y += float(nextFrame) / float(frameCount);

    vec4 portalFragment = texture2D(texture, currentFrameCoord) * (1. - interpolation)
                        + texture2D(texture, nextFrameCoord) * interpolation;

    gl_FragColor = portalFragment;
//    gl_FragDepth = gl_FragCoord.z - 0.00002 * gl_FragCoord.w;
}
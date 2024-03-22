#version 110

//uniform vec4 color;
varying vec2 coords;

void main() {
//    if(length(coords) < 100.) {
//        gl_FragColor = vec4(1, 1, 0, 1);
//        return;
//    }
//    
//    float alpha = (1. - pow(abs(coords.x), 20.)) * (1. - pow(abs(coords.y), 20.));
//    gl_FragColor = vec4(1, 1, 1, alpha);
    gl_FragColor = vec4(1, 1, 0, 1);
}
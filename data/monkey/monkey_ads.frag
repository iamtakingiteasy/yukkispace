#version 120

varying vec3  trans_frontColor;
varying vec3  trans_backColor;

void main(void) {
  if (gl_FrontFacing) {
    gl_FragColor = vec4(trans_frontColor, 1.0);
  } else {
    gl_FragColor = vec4(trans_backColor, 1.0);
  }
}
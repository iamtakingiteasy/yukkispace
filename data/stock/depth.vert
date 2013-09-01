#version 120

attribute vec4 in_position;

uniform mat4 MVP;

void main(void) {
  gl_Position = MVP * in_position;
}
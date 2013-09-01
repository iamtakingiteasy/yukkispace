#version 120

attribute vec4  in_position;
attribute vec3  in_normal;
attribute vec2  in_texture;
attribute float in_material;

varying vec3  trans_normal;
varying vec3  trans_position;

varying vec3  trans_ambient;
varying vec3  trans_diffuse;
varying vec3  trans_specular;
varying float trans_shininess;

uniform mat4 MVP;

uniform mat4 M;
uniform mat4 V;
uniform mat4 P;
uniform mat3 VI;

uniform sampler1D tex_materials;
uniform float texoff;
uniform float texstep;


void main(void) {
  gl_Position = MVP * in_position;


  trans_normal    = normalize(VI * in_normal);
  trans_position  = (V * M * in_position).xyz;

  trans_ambient   = texture1D(tex_materials, texoff * in_material + texstep * 0).rgb;
  trans_diffuse   = texture1D(tex_materials, texoff * in_material + texstep * 1).rgb;
  trans_specular  = texture1D(tex_materials, texoff * in_material + texstep * 2).rgb;
  trans_shininess = texture1D(tex_materials, texoff * in_material + texstep * 3).x;
}
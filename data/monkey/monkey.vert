#version 120

struct light_t {
  bool enabled; // if light is enabled
  mat4 MVP;

  // attenuation
  vec3 intensity;

  float fade_const;
  float fade_linear;
  float fade_quadratic;

  float spot_exp;
  float spot_cut;

  // colors
  vec3 ambient;
  vec3 diffuse;
  vec3 specular;

  // geometry
  vec3 direction;
  vec4 position;
};

const int maximum_lights = 8;

attribute vec4  in_position;
attribute vec3  in_normal;
attribute vec2  in_texture;
attribute float in_material;

varying vec3  trans_normal;
varying vec3  trans_position;
varying vec4  trans_shadowuv[maximum_lights];

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
uniform light_t lights[maximum_lights];

void main(void) {
  gl_Position = MVP * in_position;


  trans_normal    = normalize(VI * in_normal);
  trans_position  = (V * M * in_position).xyz;

  trans_ambient   = texture1D(tex_materials, texoff * in_material + texstep * 0).rgb;
  trans_diffuse   = texture1D(tex_materials, texoff * in_material + texstep * 1).rgb;
  trans_specular  = texture1D(tex_materials, texoff * in_material + texstep * 2).rgb;
  trans_shininess = texture1D(tex_materials, texoff * in_material + texstep * 3).x;

  for (int i = 0; i < maximum_lights; i++) {
    if (lights[i].enabled) {
      trans_shadowuv[i] = lights[i].MVP * in_position;
    }
  }
}
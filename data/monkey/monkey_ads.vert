#version 120

struct light_t {
  bool enabled;
  mat4 MT;
  mat4 MR;
  mat4 V;
  mat4 P;

  vec3 intensity;

  float fade_const;
  float fade_linear;
  float fade_quadratic;

  float spot_exp;
  float spot_cut;

  vec3 ambient;
  vec3 diffuse;
  vec3 specular;

  vec3 direction;
  vec4 position;
};

const int maximum_lights = 8;


attribute vec4  in_position;
attribute vec3  in_normal;
attribute vec2  in_texture;
attribute float in_material;

varying vec3 trans_frontColor;
varying vec3 trans_backColor;

uniform mat4 MVP;

uniform mat4 M;
uniform mat4 V;
uniform mat4 P;
uniform mat3 VI;

uniform sampler1D tex_materials;
uniform float texoff;
uniform float texstep;

uniform sampler2D tex_depthmap1;
uniform sampler2D tex_depthmap2;
uniform sampler2D tex_depthmap3;
uniform sampler2D tex_depthmap4;
uniform sampler2D tex_depthmap5;
uniform sampler2D tex_depthmap6;
uniform sampler2D tex_depthmap7;
uniform sampler2D tex_depthmap8;
uniform light_t lights[maximum_lights];

float radians(float degrees) {
  return degrees * 3.14159 / 180.0;
}

vec3 ads_model(vec3 tnorm, vec3 position, light_t light, vec3 mat_ambient, vec3 mat_diffuse, vec3 mat_specular, float mat_shininess) {
  vec3 s = normalize(vec3(V * light.MT * light.position) - position);

  vec3 light_direction = vec3(light.V * light.MR * vec4(light.direction,0.0));

  float angle  = acos(dot(-s, light_direction));
  float cutoff = radians(light.spot_cut/2.0);

  vec3 ambient = light.intensity * light.ambient * mat_ambient;
  if (angle < cutoff) {
    float spotFactor = pow(dot(-s, light_direction), light.spot_exp);
    vec3 v = normalize(-position);
    vec3 h = normalize(v+s);
    vec3 diffuse  = light.diffuse  * mat_diffuse  * max(0.0, dot(s, tnorm));
    vec3 specular = light.specular * mat_specular * pow(max(0.0, dot(h, tnorm)), mat_shininess);
    return ambient + light.intensity * spotFactor * (diffuse + specular);
  } else {
    return ambient;
  }
}


void main(void) {
  gl_Position = MVP * in_position;


  vec3 normal    = normalize(VI * in_normal);
  vec3 position  = (V * M * in_position).xyz;

  vec3 ambient   = texture1D(tex_materials, texoff * in_material + texstep * 0).rgb;
  vec3 diffuse   = texture1D(tex_materials, texoff * in_material + texstep * 1).rgb;
  vec3 specular  = texture1D(tex_materials, texoff * in_material + texstep * 2).rgb;
  float shininess = texture1D(tex_materials, texoff * in_material + texstep * 3).x;

  trans_frontColor = vec3(0.0);
  trans_backColor = vec3(0.0);

  for (int i = 0; i < maximum_lights; i++) {
    if (lights[i].enabled) {
      trans_frontColor += ads_model(normal, position, lights[i], ambient, diffuse, specular, shininess);
      trans_backColor += ads_model(-normal, position, lights[i], ambient, diffuse, specular, shininess);
    }
  }
}
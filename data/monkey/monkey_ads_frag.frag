#version 120

struct light_t {
  bool enabled; // if light is enabled
  mat4 MT;      // Model translation component
  mat4 MR;      // Model rotation component
  mat4 V;       // View
  mat4 P;       // Projection (unused)

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

// geometry
varying vec3  trans_normal;
varying vec3  trans_position;

// material colors
varying vec3  trans_ambient;
varying vec3  trans_diffuse;
varying vec3  trans_specular;
varying float trans_shininess;

uniform mat4 M;
uniform mat4 V;

uniform light_t lights[maximum_lights];

float radians(float degrees) {
  return degrees * 3.14159 / 180.0;
}

vec3 ads_model(vec3 tnorm, light_t light) {
  // direction from vertex to light position
  vec3 s = normalize(vec3(V * light.MT * light.position) - trans_position);

  // direction of spotlight
  vec3 light_direction = vec3(light.V * light.MR * vec4(light.direction,0.0));

  // angles
  float angle  = acos(dot(-s, light_direction));
  float cutoff = radians(light.spot_cut/2.0);

  // ambient compoennt
  vec3 ambient = light.intensity * light.ambient * trans_ambient;

  // spotlight
  if (angle < cutoff) {
    float spotFactor = pow(dot(-s, light_direction), light.spot_exp);
    vec3 v = normalize(-trans_position);
    vec3 h = normalize(v+s);
    vec3 diffuse  = light.diffuse  * trans_diffuse  * max(0.0, dot(s, tnorm));
    vec3 specular = light.specular * trans_specular * pow(max(0.0, dot(h, tnorm)), trans_shininess);
    return ambient + light.intensity * spotFactor * (diffuse + specular);
  } else {
    return ambient;
  }
}

void main(void) {
  vec3 tnorm;

  if (gl_FrontFacing) {
    tnorm = trans_normal;
  } else {
    tnorm = -trans_normal;
  }

  vec3 color = vec3(0.0);

  for (int i = 0; i < maximum_lights; i++) {
    if (lights[i].enabled || true) {
      color += ads_model(tnorm, lights[i]);
    }
  }

  // ... and so on, very slow on number of lights > 1

  gl_FragColor = vec4(color, 1.0);
}